package com.bot.telegrambotthink.service;

public enum COMMANDS {
    INFO("/info"),
    ACCESS("/token"),
    START("/start");
    private String command;
    COMMANDS(String command){ this.command = command; }
    public String getCommand(){ return command; }
}
