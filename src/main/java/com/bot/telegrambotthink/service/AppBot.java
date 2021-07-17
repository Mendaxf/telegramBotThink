package com.bot.telegrambotthink.service;

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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import ru.tinkoff.invest.openapi.model.rest.PortfolioPosition;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

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

    private OpenApi api;

    private final String INFO_LABEL = "Для чего бот?";
    private final String ACCESS_LABEL = "Добавить токен";
    private final String POTFEL = "Портфель";
    private final String CURRENCY = "Валюта";


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

    private SendMessage returnCommandResponse(String text, User user) throws TelegramApiException, ExecutionException, InterruptedException {
        if(text.equals(COMMANDS.START.getCommand())){ return  startCommamd(user.getFirstName()); }
        if(text.equals(COMMANDS.INFO.getCommand())){ return  infoCommamd(); }
        if(text.equals(COMMANDS.ACCESS.getCommand())){ return  tokenCommamd(); }
        if(text.equals(POTFEL)){ return  portfelCommamd(); }
        if(text.equals(CURRENCY)){ return  currencyCommamd(); }
        return notFoundCommand();
    }

    private SendMessage portfelCommamd() throws ExecutionException, InterruptedException {
        SendMessage message = new SendMessage();
        String msg = "В вашем портфеле:\n";
        Portfolio portfolio = api.getPortfolioContext().getPortfolio(null).get();
        log.info(portfolio.toString());
        for (PortfolioPosition p: portfolio.getPositions()) {
            msg +=  p.getName() + " tiket " + p.getTicker() +"\n"+
                    " в колличестве " + p.getBalance().doubleValue() +"\n";
        }
                    message.setText(msg);
        return message;
    }

    private  SendMessage currencyCommamd(){
        SendMessage message = new SendMessage();
        message.setText("Раздел в разработке");
        return message;
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
            log.info("Создаём подключение... ");
            api = new OkHttpOpenApi(tinkConfig.getTtoken(),false);
            message.setText("Подключились к Tinkoff Инвестиции");
            message.setReplyMarkup(customKeyboard());
            return message;
        } catch (Exception ex) {
            log.error("Что-то пошло не так.", ex);
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

    public ReplyKeyboardMarkup customKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Портфель");
        row.add("Валюта");
        keyboard.add(row);
        row = new KeyboardRow();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return  keyboardMarkup;
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
