package com.omg_link.im.core.config;

public class ConfigSetFailedException extends Throwable{
    public enum Reason{
        InvalidUrl,InvalidPort,UsernameTooLong
    }

    private final Reason reason;

    public ConfigSetFailedException(Reason reason){
        super(reason.toString());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

}
