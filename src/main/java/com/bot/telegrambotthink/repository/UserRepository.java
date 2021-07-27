package com.bot.telegrambotthink.repository;

import com.bot.telegrambotthink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    String getUsersByUserid();
    String getUsersByToken();
    User findByUserid(String chatId);
}