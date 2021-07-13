package com.bot.telegrambotthink.service;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Slf4j
@Component
public class AppBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private TinkConfig tinkConfig;

    private final String INFO_LABEL = "Для чего бот?";
    private final String ACCESS_LABEL = "Добавить токен";


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
            String chat_id = update.getMessage().getChatId().toString();
            try {
                    SendMessage message = returnCommandResponse(message_text, update.getMessage().getFrom());
                    message.enableHtml(true);
                    message.setParseMode(ParseMode.HTML);
                    message.setChatId(chat_id);
                    execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
                SendMessage message = notFoundCommand();
                message.setChatId(chat_id);
            }
        }else if(update.hasCallbackQuery()){
            try {
                SendMessage message = returnCommandResponse(update.getCallbackQuery().getData(),
                        update.getCallbackQuery().getFrom());
                message.enableHtml(true);
                message.setParseMode(ParseMode.HTML);
                message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                execute(message);
            } catch (TelegramApiException e) {
                log.error("", e);
            }
        }

    }

    private SendMessage returnCommandResponse(String text, User user) throws TelegramApiException{
        if(text.equals(COMMANDS.START.getCommand())){ return  startCommamd(user.getFirstName()); }
        if(text.equals(COMMANDS.INFO.getCommand())){ return  infoCommamd(); }
        if(text.equals(COMMANDS.ACCESS.getCommand())){ return  tokenCommamd(); }
        return notFoundCommand();
    }

    private SendMessage infoCommamd() {
        SendMessage message = new SendMessage();
        message.setText("Бот позволяет отслеживать ценные бумаги");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage startCommamd(String firstName) {
        SendMessage message = new SendMessage();
        message.setText("Привет!"+ firstName + " выбери нужную команду");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage tokenCommamd() {
        SendMessage message = new SendMessage();
        try {

            message.setText("Подключились к Tinkoff Инвестиции");
            return message;
        }catch (Exception e){
            message.setText("Не удалось подключится к Tinkoff Инвестиции. Проверьте токен или попробуйте позже");
            return message;
        }
    }

    private SendMessage notFoundCommand() {
        SendMessage message = new SendMessage();
        message.setText("Вы вели не корректную команду");
        message.setReplyMarkup(getKeyboard());
        return  message;
    }

    private InlineKeyboardMarkup getKeyboard(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(INFO_LABEL);
        inlineKeyboardButton.setCallbackData(COMMANDS.INFO.getCommand());
        InlineKeyboardButton inlineKeyboardButtonAccess = new InlineKeyboardButton();
        inlineKeyboardButtonAccess.setText(ACCESS_LABEL);
        inlineKeyboardButtonAccess.setCallbackData(COMMANDS.ACCESS.getCommand());

        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton);
        keyboardButtonsRow1.add(inlineKeyboardButtonAccess);

        keyboardButtons.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        return inlineKeyboardMarkup;
    }

}
