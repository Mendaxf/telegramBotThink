package com.bot.telegrambotthink.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OpenApiConfig;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

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
            builder.text("Hello! Please send me Tinkoff token");
            try {
                execute(builder.build());
            }catch (TelegramApiException e){
                log.info(e.getMessage().toString());
            }
        }
        msg = update.getMessage().getText();
        log.info(msg);

        try {
            OpenApi api = new OkHttpOpenApi(tinkConfig.getTtoken(),true);
            log.info("Подключаемся");
            if(api.isSandboxMode()) api.getSandboxContext().performRegistration(new SandboxRegisterRequest()).join();

            final var portfolioCurrencies = api.getPortfolioContext().getPortfolioCurrencies(null).join();
            log.info(portfolioCurrencies.toString());
            builder.text(portfolioCurrencies.toString());
            execute(builder.build());

        }catch (Exception ex){
            log.info(ex.getMessage());
        }


//        tinkConfig.setTtoken(msg);
//        AppTinkoff appTinkoff = new AppTinkoff(tinkConfig.getTtoken(),true);
//
//        log.info(appTinkoff.getPortfolioContext().toString());
       // execute(builder.build());
    }
}
