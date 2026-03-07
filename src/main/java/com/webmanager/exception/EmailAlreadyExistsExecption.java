package com.webmanager.exception;

public class EmailAlreadyExistsExecption extends RuntimeException{
    public EmailAlreadyExistsExecption (String message){
        super(message);
    }
}
