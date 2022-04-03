package com.omg_link.im.core.config;

public class ConfigSetFailedException extends Throwable{
    public ConfigSetFailedException(String reason){
        super(reason);
    }
}
