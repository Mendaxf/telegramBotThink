package com.bot.telegrambotthink.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class User {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "UserId", nullable = false)
    private String Userid;

    @Column(name = "token")
    private String token;


    public User(String userid, String Token){
        this.Userid = userid;
        this.token = Token;
    }
}
