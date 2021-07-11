package com.bot.telegrambotthink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class AppBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        update.getUpdateId();
        SendMessage.SendMessageBuilder builder = SendMessage.builder();
        String msg;
        String chatId;
        if(update.getMessage() != null){
            chatId = update.getMessage().getChatId().toString();
            builder.chatId(chatId);
            msg = update.getMessage().getText();
        }else {
            chatId = update.getChannelPost().getChatId().toString();
            builder.chatId(chatId);
            msg = update.getChannelPost().getText();
        }
        if(msg.contains("/start")){
            builder.text("Hello");
            try {
                execute(builder.build());
            }catch (TelegramApiException e){
                log.info(e.getMessage().toString());
            }
        }
    }
}
