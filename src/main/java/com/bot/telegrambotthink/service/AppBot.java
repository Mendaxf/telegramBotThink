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
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import ru.tinkoff.invest.openapi.model.rest.PortfolioPosition;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


@Slf4j
@Component
public class AppBot extends TelegramLongPollingBot {
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private TinkConfig tinkConfig;

    private OpenApi api;
    private boolean search = false;


    private final String INFO_LABEL = "Для чего бот?";
    private final String ACCESS_LABEL = "Запустить";
    private final String POTFEL = "Портфель";
    private final String CURRENCY = "Маркет\nНайти акции";


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
        if(text.equals(COMMANDS.START.getCommand())){ return  startCommand(user); }
        if(text.equals(COMMANDS.INFO.getCommand())){ return  infoCommand(); }
        if(text.equals(COMMANDS.ACCESS.getCommand())){ return  tokenCommand(); }
        if(text.equals(POTFEL)){ return  portfelCommand(); }
        if(text.equals(CURRENCY)){ return  currencyCommand(); }
        if(search) return  currencyCommand1(text);
        else return notFoundCommand();
    }

    private SendMessage portfelCommand() throws ExecutionException, InterruptedException {
        LocalDateTime dt = LocalDateTime.now();
        SendMessage message = new SendMessage();
        String msg = "&#128188 Портфель: \n" +
                     "&#128338 "+dt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))+" \r\n";
        Portfolio portfolio = api.getPortfolioContext().getPortfolio(null).get();
        log.info(portfolio.toString());
        for (PortfolioPosition p: portfolio.getPositions()) {
            msg +=  plusOrMinus(p.getExpectedYield().getValue().toString())+
                    "<b>"+p.getName() + " (" + p.getTicker() +") </b>"+
                    p.getBalance().intValue() +"ШТ.\n" +
                    "Состояние: " + p.getExpectedYield().getValue()+"\n" +
                    "Стоимость: " + ((p.getAveragePositionPrice().getValue().floatValue() * p.getBalance().intValue()) +
                    p.getExpectedYield().getValue().floatValue())+getCoin(p.getAveragePositionPrice().getCurrency().toString())+"\n";
        }
        message.setText(msg);
        return message;
    }


    private  SendMessage currencyCommand(){
        SendMessage message = new SendMessage();
        search = true;
        message.setText("Введите тикет для поиска");
        return message;
    }

    private  SendMessage currencyCommand1(String tiket){
        SendMessage message = new SendMessage();
        search = false;
        try{
            log.info(api.getMarketContext().searchMarketInstrumentsByTicker(tiket.toUpperCase(Locale.ROOT)).get().toString());
            MarketInstrumentList rest = api.getMarketContext().searchMarketInstrumentsByTicker(tiket).get();
            String msg = "По тикету: " + tiket + " найдено\n";
            for(MarketInstrument r : rest.getInstruments()){
                msg += getType(r.getType().toString()) +"\n<b>" + r.getName() + "(" + r.getTicker() + ")</b>\nдоступно в лоте " + r.getLot() +"\nпокупка в валюте: "+ getCoin(r.getCurrency().toString());
            }
            message.setText(msg);
            log.info(rest.getInstruments().toString());
            return message;
        }catch (Exception ex){
            message.setText("Не корректная команда");
            return message;
        }

    }

    private SendMessage infoCommand() {
        SendMessage message = new SendMessage();
        message.setText("Бот для сбора информации о активах в порфеле, поиск активов по тикету. Бот использует OpenAPI Тинькофф инвестиции");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage startCommand(User usr) {
        SendMessage message = new SendMessage();
        message.setText("Привет! "+ (usr.getFirstName() != null ? usr.getFirstName() : usr.getUserName()) + " выбери нужную команду: ");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private SendMessage tokenCommand() {
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

    private ReplyKeyboardMarkup customKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(POTFEL);
        row.add(CURRENCY);
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

    private SendMessage notFoundCommand() {
        SendMessage message = new SendMessage();
        message.setText("Вы ввели не корректную команду");
        message.setReplyMarkup(getKeyboard());
        return  message;
    }

    private String getCoin(String coin){
        switch(coin){
            case "RUB": return "&#8381";
            case "USD": return "&#36";
            case "EUR": return "&#8364";
            default: return coin;
        }
    }

    private String getType(String type){
        switch(type){
            case "Stock": return "Акции&#128200";
            case "Etf": return "Фонд";
            default: return type;
        }
    }
    private String plusOrMinus(String number){
        if (number.contains("-"))
            return "&#11015 ";
        return "&#11014 ";
    }
}
