package com.webmanager.exception;

public class UserNotFound extends RuntimeException{
    public UserNotFound(String message){
        super(message);
    }
}
