package org.example.toshkszariza.service;

/** Foydalanuvchiga xavfsiz ko'rsatish mumkin bo'lgan biznes xatosi. */
public class BotBusinessException extends RuntimeException {
    public BotBusinessException(String message) {
        super(message);
    }
}
