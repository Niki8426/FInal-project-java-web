package com.example.gift_svc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Тази анотация гарантира, че ако забравя Handler-а,
// Spring пак ще върне 404 вместо 500.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class GiftNotFoundException extends RuntimeException {

    public GiftNotFoundException(String message) {

        super(message);
    }
}