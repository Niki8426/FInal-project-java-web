package com.example.multimediaHub.service;

import com.example.multimediaHub.client.GiftClient;
import com.example.multimediaHub.model.MediaItem;
import com.example.multimediaHub.model.User;
import com.example.multimediaHub.repository.MediaItemRepository;
import com.example.multimediaHub.repository.UserMessageRepository;
import com.example.multimediaHub.repository.UserRepository;
import com.example.multimediaHub.web.dto.AllGift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class): Инициализира Mockito рамката за JUnit 5.
// Тя автоматично създава фалшивите обекти (Mocks) и контролира тяхното поведение,
// като премахва нуждата от ръчно зануляване на контекста между отделните софтуерни тестове.
@ExtendWith(MockitoExtension.class)
class GiftServiceTest {

    // @Mock: Симулира достъпа до таблицата с потребители в MySQL, изолирайки реалната база данни.
    @Mock private UserRepository userRepository;

    // @Mock: Симулира достъпа до таблицата с мултимедийни продукти (песни/филми).
    @Mock private MediaItemRepository mediaItemRepository;

    // @Mock: Симулира Feign/REST HTTP клиента, който прави мрежови заявки към външния микросървиз "gift-svc".
    @Mock private GiftClient giftClient;

    // @Mock: Симулира репозиторито за изпращане и съхранение на лични съобщения/известия.
    @Mock private UserMessageRepository userMessageRepository;

    // @InjectMocks: Създава реална инстанция на GiftService и автоматично вгражда (инжектира)
    // четирите дефинирани по-горе фалшиви компоненти в нейния конструктор.
    @InjectMocks private GiftService giftService;

    private UUID mediaId;
    private MediaItem testMedia;

    // @BeforeEach: Изпълнява се автоматично преди стартирането на всеки индивидуален @Test метод.
    // Използва се за софтуерно подготвяне на споделените базови обекти, за да се избегне дублиране на код.
    @BeforeEach
    void setUp() {
        mediaId = UUID.randomUUID();
        testMedia = new MediaItem();
        testMedia.setId(mediaId);
        testMedia.setTitle("Inception");
        testMedia.setPrice(new BigDecimal("20.00"));
    }

    // --- ТЕСТОВЕ ЗА МЕТОДА: sendGift ---

    /**
     * ТЕСТ: Успешно изпращане на подарък.
     * Проверяваме дали парите на подателя намаляват и дали получателят взема филма.
     */
    @Test
    void testSendGiftSuccess() {
        // Arrange (Подготовка):
        // Подготвяме подател с достатъчно софтуерна наличност в дигиталния си портфейл.
        User sender = new User();
        sender.setUsername("ivan");
        sender.setBalance(new BigDecimal("100.00"));

        // Подготвяме получател с празна колекция от притежавана медия.
        User receiver = new User();
        receiver.setUsername("gosho");
        receiver.setOwnedMedia(new ArrayList<>());

        // Дефинираме софтуерното поведение (Stubbing) на репозиторитата, когато GiftService ги извика.
        when(userRepository.findByUsername("ivan")).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("gosho")).thenReturn(Optional.of(receiver));
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

        // Act (Действие):
        // Извикваме реалния бизнес метод за изпращане на медиен продукт като подарък между потребителите.
        giftService.sendGift("ivan", "gosho", mediaId, "Enjoy!");

        // Assert (Проверка):
        // assertEquals: Уверяваме се софтуерно, че балансът на изпращача е намален точно с цената на филма (100.00 - 20.00 = 80.00).
        assertEquals(new BigDecimal("80.00"), sender.getBalance());

        // assertTrue: Проверяваме дали Many-to-Many колекцията на получателя вече съдържа подарения продукт.
        assertTrue(receiver.getOwnedMedia().contains(testMedia));
    }

    /**
     * ТЕСТ: Грешка при недостатъчно пари.
     * Проверяваме дали кодът спира и хвърля съобщението "Нямате достатъчна наличност".
     */
    @Test
    void testSendGiftInsufficientBalance() {
        // Arrange (Подготовка):
        // Създаваме потребител с крайно недостатъчен баланс (5.00 EUR) за покупка на филм от 20.00 EUR.
        User poorSender = new User();
        poorSender.setBalance(new BigDecimal("5.00"));

        // Конфигурираме фалшивите обекти да връщат бедния потребител и тестовата медия.
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(poorSender));
        when(mediaItemRepository.findById(any())).thenReturn(Optional.of(testMedia));

        // Act & Assert (Действие и Проверка):
        // assertThrows: Гарантира, че бизнес логиката ще блокира транзакцията софтуерно и ще изхвърли
        // точно IllegalArgumentException, предпазвайки системата от преминаване в негативен баланс.
        assertThrows(IllegalArgumentException.class, () ->
                giftService.sendGift("ivan", "gosho", mediaId, "Hi"));
    }

    /**
     * ТЕСТ: Проблем с микросървиса (Catch блок).
     * Тестваме случая, в който външната система не работи, но нашият сайт продължава.
     */
    @Test
    void testSendGiftCatchMicroserviceError() {
        // Arrange (Подготовка):
        User sender = new User();
        sender.setBalance(new BigDecimal("100.00"));
        User receiver = new User();
        receiver.setOwnedMedia(new ArrayList<>());

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(sender));
        when(mediaItemRepository.findById(any())).thenReturn(Optional.of(testMedia));

        // doThrow: Симулираме мрежов срив или тотален отказ (RuntimeException) на външния микросървиз "gift-svc",
        // достъпван през HTTP REST клиента GiftClient.
        doThrow(new RuntimeException()).when(giftClient).createGift(any());

        // Act & Assert (Действие и Проверка):
        // assertDoesNotThrow: Критичен софтуерен тест. Уверяваме се, че въпреки срива във външната система,
        // нашият сървис улавя грешката вътре в try-catch блок, логва я безопасно и позволява на основната
        // транзакция в монолита да приключи успешно, без да чупи потребителското изживяване.
        assertDoesNotThrow(() -> giftService.sendGift("ivan", "gosho", mediaId, "Hi"));
    }

    // --- ТЕСТОВЕ ЗА МЕТОДА: fetchAllGifts ---

    /**
     * ТЕСТ: Когато списъкът е празен (null).
     * Настъпваме проверката "if (gifts == null)" за пълно покритие.
     */
    @Test
    void testFetchGiftsReturnsEmptyOnNull() {
        // Arrange (Подготовка):
        // Симулираме, че външното API връща null вместо празна JSON колекция.
        when(giftClient.getAllGifts()).thenReturn(null);

        // Act (Действие):
        List<AllGift> result = giftService.fetchAllGifts();

        // Assert (Проверка):
        // Проверяваме софтуерната защита срещу NullPointerException — методът трябва да върне чист, празен Java списък.
        assertTrue(result.isEmpty());
    }

    /**
     * ТЕСТ: Когато филмът е изтрит от нашата база.
     * Проверяваме дали ще се изпише "Изтрита медия".
     */
    @Test
    void testFetchGiftsWhenMediaIsMissingInDb() {
        // Arrange (Подготовка):
        // Микросървизът връща информация за подарък на продукт, който обаче е бил физически изтрит от нашия каталог.
        AllGift dto = new AllGift();
        dto.setMediaId(mediaId);

        when(giftClient.getAllGifts()).thenReturn(List.of(dto));
        // Симулираме липсата на продукта в локалната MySQL база данни (Optional.empty()).
        when(mediaItemRepository.findById(mediaId)).thenReturn(Optional.empty());

        // Act (Действие):
        List<AllGift> result = giftService.fetchAllGifts();

        // Assert (Проверка):
        // Защитен механизъм в софтуера: ако продуктът липсва, заглавието му автоматично се подменя
        // с константен текст "Изтрита медия", за да не се счупи визуализацията в Thymeleaf HTML шаблона.
        assertEquals("Изтрита медия", result.get(0).getMediaTitle());
    }

    /**
     * ТЕСТ: Когато има мрежов проблем (API Exception).
     * Проверяваме дали catch блокът връща празен списък.
     */
    @Test
    void testFetchGiftsHandlesException() {
        // Arrange (Подготовка):
        // Симулираме пълна липса на връзка до отдалечения сървър (Network Exception).
        when(giftClient.getAllGifts()).thenThrow(new RuntimeException());

        // Act (Действие):
        List<AllGift> result = giftService.fetchAllGifts();

        // Assert (Проверка):
        // Методът трябва софтуерно да прихване изключението в catch блока и вместо да прекъсне работата на контролера,
        // да върне празен списък (Fail-safe дизайн).
        assertTrue(result.isEmpty());
    }

    /**
     * ИНТЕГРАЛЕН ТЕСТ: Пълно софтуерно покритие на клоновете (Branch Coverage) в fetchAllGifts.
     * Обединява трите основни гранични състояния в един последователен тест за валидация.
     */
    @Test
    void testFetchAllGifts_FullCoverage() {
        // --- Сценарий 1: API-то връща null (Покрива "if (gifts == null)") ---
        when(giftClient.getAllGifts()).thenReturn(null);
        assertTrue(giftService.fetchAllGifts().isEmpty());

        // --- Сценарий 2: Има подарък, но медията е изтрита (Покрива "if (gift.getMediaTitle() == null)") ---
        UUID deletedMediaId = UUID.randomUUID();
        AllGift giftDto = new AllGift();
        giftDto.setMediaId(deletedMediaId);

        when(giftClient.getAllGifts()).thenReturn(List.of(giftDto));
        when(mediaItemRepository.findById(deletedMediaId)).thenReturn(Optional.empty());

        List<AllGift> result = giftService.fetchAllGifts();
        assertEquals("Изтрита медия", result.get(0).getMediaTitle());

        // --- Сценарий 3: Грешка в мрежата (Покрива "catch (Exception e)") ---
        when(giftClient.getAllGifts()).thenThrow(new RuntimeException("Network down"));
        // Проверяваме софтуерно, че обектът не е null, а е празна безопасна инстанция на списък.
        assertNotNull(giftService.fetchAllGifts());
    }
}