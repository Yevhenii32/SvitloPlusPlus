package com.noideasolutions.svitlo.exception;

/**
 * Базовий виняток для всього проєкту Svitlo++.
 * Наслідуємо RuntimeException, щоб не змушувати розробників
 * прописувати throws у кожному методі (Unchecked exception).
 */
public class SvitloException extends RuntimeException {
    public SvitloException(String message) {
        super(message);
    }

    public SvitloException(String message, Throwable cause) {
        super(message, cause);
    }
}