package com.bot.telegrambotthink.service;

import com.bot.telegrambotthink.model.User;
import com.bot.telegrambotthink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService() throws NoSuchPaddingException, NoSuchAlgorithmException {
    }

    public boolean userAdd(String chatId) {
        User userFromBd = userRepository.findByUserid(chatId);
        if(userFromBd != null) return false;
        userRepository.save(new User(chatId, null));
        return true;
    }
    public String getToken(String chatId){
        User userFromBd = userRepository.findByUserid(chatId);
//        if(userFromBd == null) return ; Todo exeptions
        return userFromBd.getToken();
    }


    public void userDelete(String chatId){
        User userFromBd = userRepository.findByUserid(chatId);
        if(userFromBd == null) return;
        userRepository.delete(userFromBd);
    }

    public void saveToken(String chatId,String token){
        User userFromBd = userRepository.findByUserid(chatId);
        if(userFromBd == null) return;
        userFromBd.setToken(token);
    }
}
