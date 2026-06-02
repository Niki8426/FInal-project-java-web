package com.example.multimediaHub.repository;

import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Тази анотация регистрира интерфейса като репозитори в Spring контейнера.
// Тя отговаря за директната софтуерна връзка с таблицата "media_items" в MySQL и ни спестява писането на тежки SQL заявки.
@Repository
public interface MediaItemRepository extends JpaRepository<MediaItem, UUID> {

    // С този метод търсим в базата и взимаме първия намерен запис, на който флагът "current" е вдигнат на true.
    // Използваме го, за да изкараме някое конкретно заглавие на преден план (например заглавното видео на началния екран).
    Optional<MediaItem> findFirstByCurrentTrue();

    // Този метод автоматично генерира софтуерна заявка, която изтегля всички медийни продукти от точно определен тип (AUDIO или VIDEO),
    // като веднага ги подрежда по година на издаване от най-новите към най-старите.
    List<MediaItem> findAllByTypeOrderByYearDesc(MediaType type);

    // Това е дефолтен Java метод, който служи за защита в сървис слоя. Ако потребителят е нов и няма абсолютно нищо купено все още (списъкът е празен),
    // методът директно му връща целия каталог от този тип, вместо да гърми с празна грешка или грешен SQL.
    default List<MediaItem> findSafeMarketItems(MediaType type, List<UUID> ownedIds) {
        if (ownedIds == null || ownedIds.isEmpty()) {
            return findAllByTypeOrderByYearDesc(type);
        }
        return findMarketItems(type, ownedIds);
    }

    // Тук използваме ръчна JPQL заявка с анотацията @Query. Нейната задача е да извади продуктите за пазара (Marketplace),
    // като филтрира медиите по тип и изрично изключва (NOT IN) тези ID-та, които потребителят вече си е купил, за да не му ги предлагаме пак.
    @Query("SELECT m FROM MediaItem m WHERE m.type = :type AND m.id NOT IN :ownedIds ORDER BY m.year DESC")
    List<MediaItem> findMarketItems(@Param("type") MediaType type, @Param("ownedIds") List<UUID> ownedIds);



    // Този метод приема цял списък от UUID ключове и извлича наведнъж от базата всички филми или песни,
    // които отговарят на тези идентификатори (използва се често за зареждане на количка или списък с любими).
    List<MediaItem> findAllByIdIn(List<UUID> ids);
}