package com.example.multimediaHub.service;

import com.example.multimediaHub.client.GiftClient;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.model.UserMessage;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.AllGift;
import com.example.multimediaHub.web.dto.CreateGiftRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// @Service: Регистрира този клас като компонент за бизнес логика (сървис) в Spring контекстера.
// Благодарение на това, Spring знае за него и ни позволява да го инжектираме в контролери или други компоненти.
@Service
public class GiftService {

    // Полета за зависимостите (компонентите), нужни на този сървис за изпълнение на логиката му.
    // Тъй като са маркирани с 'final', те задължително трябва да се заложат през конструктора и не могат да се променят след това.
    private final UserRepository userRepository;
    private final MediaItemRepository mediaItemRepository;
    private final GiftClient giftClient;
    private final UserMessageRepository userMessageRepository;

    // Конструктор на класа: Използва се за Dependency Injection (инжектиране на зависимости).
    // Когато Spring стартира, той намира правилните имплементации на репозиторитата и HTTP клиента
    // и ги подава тук наготово, за да може GiftService да бачка с тях.
    public GiftService(UserRepository userRepository,
                       MediaItemRepository mediaItemRepository,
                       GiftClient giftClient,
                       UserMessageRepository userMessageRepository) {
        this.userRepository = userRepository;
        this.mediaItemRepository = mediaItemRepository;
        this.giftClient = giftClient;
        this.userMessageRepository = userMessageRepository;
    }

    // @Transactional: Отваря база данни транзакция за целия метод. Ако по време на изпълнението
    // се появи изключение (RuntimeException), Spring автоматично ще направи rollback (ще отмени всички промени).
    // Това гарантира, че ако парите се изтеглят, но подаръкът не се запише, базата няма да остане в грешно състояние.
    @Transactional
    public void sendGift(String senderUsername, String receiverUsername, UUID mediaId, String message) {

        // Търси изпращача в базата по неговия username. Ако липсва, хвърля грешка и прекратява транзакцията.
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("Изпращачът не е намерен"));

        // Търси получателя в базата по неговия username. Ако липсва, хвърля грешка.
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("Получателят не е намерен"));

        // Търси медийния продукт (филм/песен) по неговото UUID. Ако не бъде намерен, хвърля грешка.
        MediaItem mediaItem = mediaItemRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Медията не е намерена"));

        // 1. Проверка на баланса
        // Използва се методът compareTo на BigDecimal. Ако балансът на изпращача е по-малък от цената,
        // се хвърля софтуерна грешка "Нямате достатъчна наличност".
        if (sender.getBalance().compareTo(mediaItem.getPrice()) < 0) {
            throw new IllegalArgumentException("Нямате достатъчна наличност");
        }

        // 2. Транзакция в монолита
        // Изважда цената на продукта от текущия баланс на изпращача и актуализира обекта.
        sender.setBalance(sender.getBalance().subtract(mediaItem.getPrice()));

        // Добавя закупената медия в списъка 'ownedMedia' на получателя (релацията Много към Много).
        receiver.getOwnedMedia().add(mediaItem);

        // Запазва актуализираните данни за изпращача (с новия баланс) в таблицата "users".
        userRepository.save(sender);

        // Запазва актуализираните данни за получателя (с добавената медия) в таблицата "users".
        userRepository.save(receiver);

        // 3. Съобщение (Система за известия)
        // Създава нов обект от тип UserMessage, който служи за вътрешно известие/писмо в системата.
        UserMessage userMessage = new UserMessage();

        // Задава на кое потребителско ID трябва да се покаже това съобщение във входящата кутия.
        userMessage.setReceiver(receiver);

        // Генерира текста на писмото, като извиква помощния метод buildMessage.
        userMessage.setContent(buildMessage(sender, mediaItem, message));

        // Записва съобщението физически в таблицата "user_messages" в MySQL.
        userMessageRepository.save(userMessage);


        // try-catch блок за мрежова защита: Обвиваме повикването към външната система.
        // Ако микросървисът gift-svc е спрян или даде мрежова грешка, уловяме изключението тук.
        // По този начин локалната транзакция в монолита завършва успешно и подаръкът се получава, без да гърми целия сайт.
        try {
            // Използва HTTP клиента (giftClient), за да изпрати POST заявка към отдалечения микросървис gift-svc.
            // Подава нов DTO обект с пълната информация за извършения подарък, за да се заведе в неговата база данни.
            giftClient.createGift(new CreateGiftRequest(
                    sender.getUsername(),
                    receiver.getUsername(),
                    mediaItem.getId(),
                    userMessage.getContent()
            ));
        } catch (Exception e) {
            // Отпечатва предупреждение в системната конзола на сървъра, че микросървисът не е достъпен.
            System.err.println("⚠️ Микросървисът gift-svc не е достъпен. Подаръкът е записан само локално.");
        }
    }

    // private метод buildMessage: Помощна софтуерна функция вътре в класа за сглобяване на текст.
    // Приема обектите на изпращача, медията и пожеланието, и връща форматиран String,
    // който описва кой какво е подарил и какво е написал.
    private String buildMessage(User sender, MediaItem mediaItem, String userMessage) {
        return String.format("От: %s | Медия: %s | Съобщение: %s",
                sender.getUsername(), mediaItem.getTitle(), userMessage);
    }

    // Метод fetchAllGifts: Използва се за извличане и показване на историята на всички направени подаръци.
    // Прави интеграция между отдалечени данни (от gift-svc) и локални данни (имената на медиите от монолита).
    public List<AllGift> fetchAllGifts() {
        try {
            // Прави HTTP GET заявка през giftClient до микросървиса, за да дръпне пълния списък с подаръци.
            List<AllGift> gifts = giftClient.getAllGifts();

            // Защита: Ако микросървисът върне null обект, веднага прекъсваме и връщаме празен ArrayList, за да няма NullPointerException.
            if (gifts == null) return new ArrayList<>();

            // Въртим цикъл през всеки един получен от микросървиса подарък (DTO).
            for (AllGift gift : gifts) {
                // Тъй като микросървисът пази само ID на медията, тук отиваме до локалното ни MediaItemRepository,
                // за да намерим реалния запис на филма/песента по това ID.
                mediaItemRepository.findById(gift.getMediaId()).ifPresent(media -> {
                    // Ако медията съществува в локалната база, взимаме истинското заглавие и го наливаме в DTO обекта за предния панел (UI).
                    gift.setMediaTitle(media.getTitle());
                });

                // Защита: Ако медията е била изтрита от администратор в монолита, но записът за подаръка все още стои в микросървиса,
                // заглавието ще остане null. В такъв случай ръчно залагаме текст "Изтрита медия", за да има какво да се покаже на екрана.
                if (gift.getMediaTitle() == null) {
                    gift.setMediaTitle("Изтрита медия");
                }
            }
            // Връщаме пълния и обогатен списък с подаръци, готов за рендериране на HTML екрана.
            return gifts;
        } catch (Exception e) {
            // Ако gift-svc е спрян или мрежата се срине по време на четенето, улавяме грешката.
            // Записваме съобщение в лога на сървъра, за да знае програмистът какво се случва.
            System.err.println("❌ Грешка при извличане на подаръци: " + e.getMessage());

            // Връщаме чист празен списък. По този начин уеб страницата ще се зареди нормално (просто таблицата с подаръци ще е празна),
            // вместо потребителят да получи голяма системна грешка (Whitelabel Error Page).
            return new ArrayList<>();
        }
    }


}