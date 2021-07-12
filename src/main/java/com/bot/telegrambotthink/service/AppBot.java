package com.bot.telegrambotthink.service;

import lombok.SneakyThrows;
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
    @Autowired
    private TinkConfig tinkConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            if(message_text.equals("/start")){
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId().toString());
                message.setText(update.getMessage().getText());
                String firstName = update.getMessage().getFrom().getFirstName();
                message.setText("Hello! "+firstName +" Please send me Tinkoff token");
                try {
                    execute(message);
                    message.setText("");
                }catch (TelegramApiException e){
                    log.info(e.getMessage());
                }
            }
        }

    }
}
