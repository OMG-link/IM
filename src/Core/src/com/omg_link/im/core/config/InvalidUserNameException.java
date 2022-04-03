package com.omg_link.im.core.config;

public class InvalidUserNameException extends ConfigSetFailedException {
    public InvalidUserNameException(){
        super(String.format("Your name should be no longer than %d characters.",Config.nickMaxLength));
    }
}