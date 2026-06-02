package com.example.multimediaHub.service;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.MediaHome;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
public class MediaItemService {

    // Логер компонент за записване на важни софтуерни събития в конзолата или лог файловете (например кога се чете от база и кога се чисти кеш).
    private static final Logger log = LoggerFactory.getLogger(MediaItemService.class);

    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;

    // Конструктор за Dependency Injection: Програмата автоматично инжектира репозиторитата за работа с MySQL таблиците.
    public MediaItemService(MediaItemRepository mediaItemRepository,
                            UserRepository userRepository) {
        this.mediaItemRepository = mediaItemRepository;
        this.userRepository = userRepository;
    }

    // ===== CACHED METHODS =====

    // @Cacheable: Спестява тежки заявки към базата данни. При първо викане взема данните от MySQL и ги записва
    // в кеш паметта 'mediaCache' под ключ съответния тип (AUDIO/VIDEO). При следващи повиквания със същия тип,
    // методът изобщо НЕ се изпълнява, а данните се връщат директно от бързата памет на сървъра.
    @Cacheable(value = "mediaCache", key = "#type")
    public List<MediaItem> getAllItemsByType(MediaType type) {
        log.info(">>> Fetching all items for type: {} from DATABASE (Cache was empty)", type);
        return mediaItemRepository.findAllByTypeOrderByYearDesc(type);
    }

    // ===== ADMIN / SAVE METHODS (EVICT CACHE) =====

    // @CacheEvict: Тъй като се добавя нов продукт, старите данни в кеша вече не са актуални.
    // Тази анотация напълно изтрива (изчиства) съдържанието на 'mediaCache' (allEntries = true),
    // за да може при следващото отваряне на сайта да се заредят пресните данни от базата.
    @CacheEvict(value = "mediaCache", allEntries = true)
    public void addMedia(String title, String youtubeVideoId, MediaType type, BigDecimal price,
                         Integer year, String genre, String imageUrl, String description) {
        log.info(">>> Adding new MediaItem and EVICTING cache");
        MediaItem item = new MediaItem();
        item.setTitle(title);
        item.setYoutubeVideoId(youtubeVideoId);
        item.setType(type);
        item.setPrice(price);
        item.setYear(year);
        item.setGenre(genre);
        item.setImageUrl(imageUrl);
        item.setDescription(description);

        mediaItemRepository.save(item);
    }

    // @CacheEvict: Използва се при редакция или запис на съществуваща медия.
    // Изчиства целия кеш, за да предотврати показването на остарели цени или заглавия по страниците.
    @CacheEvict(value = "mediaCache", allEntries = true)
    public void saveMedia(MediaItem mediaItem) {
        log.info(">>> Saving MediaItem and EVICTING cache");
        mediaItemRepository.save(mediaItem);
    }

    // ===== MARKET LOGIC =====

    // Метод getMarketItems: Извлича списък от продукти, достъпни за купуване на пазара (Marketplace).
    // Първо събира ID-тата на вече купените от потребителя медии чрез Stream API, за да не му ги показва пак.
    // Ако потребителят няма нищо купено, интелигентно извиква кеширания ни метод, за да не товари базата.
    public List<MediaItem> getMarketItems(User user, MediaType type) {
        List<UUID> ownedIds = user.getOwnedMedia()
                .stream()
                .map(MediaItem::getId)
                .toList();

        if (ownedIds.isEmpty()) {
            // Използваме кеширания метод тук!
            return getAllItemsByType(type);
        }

        return mediaItemRepository.findMarketItems(type, ownedIds);
    }

    // ===== BUY LOGIC =====

    // @Transactional: Отваря транзакция към базата данни. Процесът по купуване изисква пълна сигурност —
    // ако парите се изтеглят, но софтуерът забие преди продуктът да се добави в профила,
    // Spring прави автоматичен rollback и възстановява първоначалното състояние на портфейла.
    @Transactional
    public boolean buyMedia(User user, UUID mediaId) {
        // Търси медията по ID. Ако липсва, хвърля изключение.
        MediaItem media = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        // Проверява дали потребителят вече не я притежава, за да предотврати двойно плащане.
        boolean alreadyOwned = user.getOwnedMedia()
                .stream()
                .anyMatch(m -> m.getId().equals(mediaId));

        if (alreadyOwned) {
            return false;
        }

        // Проверява дали сумата в портфейла на потребителя е достатъчна за покриване на цената.
        if (user.getBalance().compareTo(media.getPrice()) < 0) {
            return false;
        }

        // Изважда цената от баланса на потребителя и я вкарва в колекцията му от купени продукти.
        user.setBalance(user.getBalance().subtract(media.getPrice()));
        user.getOwnedMedia().add(media);

        // Записва промените по потребителския профил обратно в таблицата "users" на MySQL.
        userRepository.save(user);
        return true;
    }

    // ===== HOME & HELPERS =====

    // Метод getUserMusicForHome: Филтрира купената медия на потребителя, взема само песните (MUSIC)
    // и чрез map ги преобразува в олекотени DTO обекти (MediaHome), удобни за показване на началната страница.
    public List<MediaHome> getUserMusicForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MUSIC)
                .map(this::toHomeDto)
                .toList();
    }

    // Метод getUserMoviesForHome: Филтрира притежаваните продукти и извлича само филмите (MOVIE),
    // като отново ги пакетира в леки DTO структури за Thymeleaf екрана.
    public List<MediaHome> getUserMoviesForHome(User user) {
        return user.getOwnedMedia()
                .stream()
                .filter(m -> m.getType() == MediaType.MOVIE)
                .map(this::toHomeDto)
                .toList();
    }

    // private метод toHomeDto: Помощна софтуерна функция вътре в класа.
    // Преобразува тежкия Entity обект с всичките му цени и описания в лека MediaHome структура (само с ID, заглавие и YouTube код).
    private MediaHome toHomeDto(MediaItem media) {
        return new MediaHome(
                media.getId(),
                media.getTitle(),
                media.getYoutubeVideoId()
        );
    }

    // Метод getById: Намира конкретна медия по нейното UUID. Ако не съществува в MySQL, директно хвърля грешка.
    public MediaItem getById(UUID id) {
        return mediaItemRepository.findById(id).orElseThrow();
    }

    // Метод getActiveMedia: Извлича маркирания като активен/текущ продукт в системата (например главното видео на сайта).
    // Ако няма такъв заложен в базата, връща чисто null, за да предпази системата от грешки.
    public MediaItem getActiveMedia() {
        return mediaItemRepository.findFirstByCurrentTrue().orElse(null);
    }

    // @Transactional: Подсигурява транзакционна цялост при модификация на междинната свързваща таблица.
    // Тъй като премахваме елемент от списък, управляван от Hibernate, методът гарантира синхронизацията с базата.
    @Transactional
    public void removeFromPlaylist(User user, UUID mediaId) {
        // 1. Намираме медията, която потребителят иска да премахне
        MediaItem mediaToRemove = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        // 2. Премахваме я от списъка на потребителя
        // Hibernate автоматично ще изтрие записа само от свързващата таблица (users_owned_media)
        user.getOwnedMedia().removeIf(m -> m.getId().equals(mediaId));

        // 3. Записваме промяната в потребителя
        userRepository.save(user);

        log.info("User {} removed {} from their playlist", user.getUsername(), mediaToRemove.getTitle());
    }
}