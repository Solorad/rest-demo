package com.example.morenkov.exception;

public class AccountStoreException extends Exception {
    public AccountStoreException() {
        super();
    }

    public AccountStoreException(String message) {
        super(message);
    }

    public AccountStoreException(Exception e) {
        super(e);
    }
}
