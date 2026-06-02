package com.example.multimediaHub.web.enums;

import java.util.Random;

// Този клас е изброим тип (enum). Неговата роля в софтуера е да дефинира твърдо зададен
// набор от константи – в случая 6 различни имена на фонови изображения за дигиталния портфейл (Wallet).
// Енъмите гарантират типова сигурност, предотвратявайки използването на несъществуващи имена на картинки в сайта.
public enum WalletBackground {

    // Списък с дефинираните константи и техните съответстващи стрингови стойности.
    MONEY1("money1"),
    MONEY2("money2"),
    MONEY3("money3"),
    MONEY4("money4"),
    MONEY5("money5"),
    MONEY6("money6");

    // "private static final WalletBackground[] VALUES": Статично поле, което кешира масива от всички налични константи (чрез метода .values()).
    // Това е софтуерна оптимизация — спестява повторното генериране на нов масив при всяко извикване на случаен фон.
    private static final WalletBackground[] VALUES = values();

    // "private static final Random RANDOM": Инстанция на софтуерния генератор на случайни числа,
    // използвана за разбъркване на фоновете. Маркирана е като статична, за да не се пресъздава нов обект Random в паметта.
    private static final Random RANDOM = new Random();

    // Вътрешно финално поле, което пази реалното текстово име на CSS/HTML файла или картинката.
    private final String imageName;

    // Конструктор на енъма: Извиква се автоматично при стартиране на приложението за всяка една от константите отгоре.
    // Задава съответната стрингова стойност (напр. "money1") в полето 'imageName'.
    WalletBackground(String imageName) {
        this.imageName = imageName;
    }

    // Метод getImageName: Публичен гетер, който позволява на Front-end слоя (Thymeleaf или API)
    // да извлече стринговото име на картинката, за да го рендерира правилно като фоново изображение в браузъра.
    public String getImageName() {
        return imageName;
    }

    // Статичен метод random(): Критичен метод за бизнес логиката на екрана.
    // Използва RANDOM.nextInt(), за да избере случаен индекс между 0 и дължината на масива (в случая от 0 до 5).
    // След това връща случайния WalletBackground елемент от кеширания масив VALUES.
    // Така при всяко отваряне на портфейла потребителят вижда различна картинка на заден план.
    public static WalletBackground random() {
        return VALUES[RANDOM.nextInt(VALUES.length)];
    }
}