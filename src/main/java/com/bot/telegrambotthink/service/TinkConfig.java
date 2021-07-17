package com.bot.telegrambotthink.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
public class TinkConfig {
    @Value("${ttoken}")
    private String Ttoken;
}
