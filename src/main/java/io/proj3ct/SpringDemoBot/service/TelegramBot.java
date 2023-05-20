package io.proj3ct.SpringDemoBot.service;


import io.proj3ct.SpringDemoBot.config.BotConfig;
import io.proj3ct.SpringDemoBot.model.Currency;
import io.proj3ct.SpringDemoBot.model.MessageModel;
import io.proj3ct.SpringDemoBot.model.User;
import io.proj3ct.SpringDemoBot.repository.MessageRepository;
import io.proj3ct.SpringDemoBot.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        Currency currencyModel = new Currency();
        String currency = "";


        if(update.hasMessage() && update.getMessage().hasText()){
            String message = update.getMessage().getText();
//            if(update.getMessage().hasVoice()){
//                String fileId = update.getMessage().getVoice().getFileId();
//                String text = convertVoiceToText(fileId);
//            }
            long chatId = update.getMessage().getChatId();


            switch (message){
                case "/start" :
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    break;

                default:  try {
                    currency = CurrencyService.convert(message);
                    saveMessage(update.getMessage());
                    sendMessage(chatId, currency);


                } catch (IOException e) {
                    sendMessage(chatId, "We have not found such a currency." + "\n" +
                            "Enter the currency whose official exchange rate" + "\n" +
                            "For example: 5$");
                } catch (ParseException e) {
                    throw new RuntimeException("Unable to parse date");
                }
            }
        }
    }

//    private String convertVoiceToText(String fileId) {
//        try {
//            // Download the voice message file
//            GetFile getFileRequest = new GetFile().setFileId(fileId);
//            File voiceFile = execute(getFileRequest);
//            String voiceFilePath = voiceFile.getFilePath();
//            Path file = Files.createTempFile("voice", ".oga");
//            getFile(voiceFilePath, file.toFile());
//
//            // Convert voice to text using DeepSpeech STT API
//            String apiUrl = "https://api.deepspeech.org/decoders/free";
//            HttpPost httpPost = new HttpPost(apiUrl);
//            FileEntity fileEntity = new FileEntity(file.toFile(), ContentType.DEFAULT_BINARY);
//            httpPost.setEntity(fileEntity);
//
//            try (CloseableHttpClient httpClient = HttpClients.createDefault();
//                 HttpResponse response = httpClient.execute(httpPost)) {
//                HttpEntity responseEntity = response.getEntity();
//                if (responseEntity != null) {
//                    String jsonResponse = EntityUtils.toString(responseEntity);
//                    DeepSpeechResponse deepSpeechResponse = new Gson().fromJson(jsonResponse, DeepSpeechResponse.class);
//                    return deepSpeechResponse.text;
//                }
//            }
//        } catch (IOException | ApiException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//    private static class DeepSpeechResponse {
//        String text;
//    }

    private void saveMessage(Message message) {
        MessageModel messageModel = new MessageModel();
        messageModel.setContent(message.getText());
        User user = userRepository.findUserByChatId(message.getChatId());
        messageModel.setUser(user);
        messageModel.setWrittenDate(new Timestamp(System.currentTimeMillis()));
        messageRepository.save(messageModel);
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()){
            Long chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

        }
    }

    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + ", nice to meet you!" + "\n" +
                "Enter the currency " + "\n" +
                "For example: 5$ or 5000тенге";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        } catch (TelegramApiException e){

        }
    }
}
