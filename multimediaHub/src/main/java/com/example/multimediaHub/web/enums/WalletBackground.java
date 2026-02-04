package com.example.multimediaHub.web.enums;

import java.util.Random;

public enum WalletBackground {

    MONEY1("money1"),
    MONEY2("money2"),
    MONEY3("money3"),
    MONEY4("money4"),
    MONEY5("money5"),
    MONEY6("money6");

    private static final WalletBackground[] VALUES = values();
    private static final Random RANDOM = new Random();

    private final String imageName;

    WalletBackground(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public static WalletBackground random() {
        return VALUES[RANDOM.nextInt(VALUES.length)];
    }
}
