package com.example.multimediaHub.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.*;

// Класът тества CacheConfig — конфигурационния компонент, който отговаря за кеширането в приложението.
// Тъй като това е чист Unit (единичен) тест, ние не вдигаме целия тежък Spring контекст (@SpringBootTest),
// а директно тестваме поведението на Java метода за създаване на обекта.
class CacheConfigTest {

    // @Test: Маркира метода катоJUnit 5 тестов метод, който уеб бекенд разработчиците пускат за проверка на конфигурацията.
    @Test
    void testCacheManagerBeanCreation() {
        // Arrange (Подготовка):
        // Инстанцираме ръчно нашия конфигурационен клас, точно както Spring би го направил при стартиране на софтуера.
        CacheConfig cacheConfig = new CacheConfig();

        // Act (Действие):
        // Извикваме фабричния метод, който отговаря за генерирането на Spring компонента (Bean) за управление на кеша.
        CacheManager cacheManager = cacheConfig.cacheManager();

        // Assert (Проверка):
        // assertNotNull: Уверяваме се софтуерно, че мениджърът на кеша е успешно създаден и обектът не е празен (null).
        assertNotNull(cacheManager, "CacheManager бинът не трябва да е null");

        // assertTrue(... instanceof ...): Проверяваме дали върнатият обект е точно от типа ConcurrentMapCacheManager.
        // Този тип мениджър е вграден в Spring и използва ConcurrentHashMap в оперативната памет (RAM) за съхранение на бързи данни.
        assertTrue(cacheManager instanceof ConcurrentMapCacheManager, "Трябва да е инстанция на ConcurrentMapCacheManager");

        // Проверяваме дали кешът с име "mediaCache" е регистриран:
        // .getCache("mediaCache"): Опитваме се да достъпим конкретната кеш памет по нейното стринг име.
        // assertNotNull: Гарантира софтуерно, че кеш контейнерът с това име съществува и е правилно конфигуриран.
        // Той е критичен за бизнес логиката (например за кеширане на тежки заявки за песни или филми в MediaItemService).
        assertNotNull(cacheManager.getCache("mediaCache"), "Кешът 'mediaCache' трябва да е наличен");
    }
}