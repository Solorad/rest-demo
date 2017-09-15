package com.example.morenkov.exception;

public class UserNotFoundException  extends Exception  {

    public UserNotFoundException(String message) {
        super(message, null, false, false);
    }
}
