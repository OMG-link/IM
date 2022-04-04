package com.omg_link.im.core.config;

public class ConfigSetFailedException extends Throwable{
    public enum Reason{
        InvalidUrl,InvalidPort,UsernameTooLong
    }

    private final Reason state;

    public ConfigSetFailedException(Reason state){
        super(state.toString());
        this.state = state;
    }

    public Reason getReason() {
        return state;
    }

}
