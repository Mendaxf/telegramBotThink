package com.bot.telegrambotthink.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class BotConfig {
    @Value("${token}")
    private String token;
    @Value("${name}")
    private String name;
}
