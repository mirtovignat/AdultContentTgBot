package com.example.demo.handler.admin;

import com.example.demo.state.AdminState;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AdminSession {
    private AdminState state = AdminState.NONE;
    private String tempCode;
    private Long tempBonus;
    private LocalDateTime tempExpiresAt;
    private Long tempTargetChatId;

}