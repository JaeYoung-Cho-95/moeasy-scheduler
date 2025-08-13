package com.moeasy.scheduler.scheduler.dto.llm;

import java.util.List;
import lombok.Getter;

public class ChatResponseDto {
    private String id;
    private List<ChatChoice> choices;
    private long created;
    private String model;

    @Getter
    public static class ChatChoice {
        private int index;
        private ChatMessageDto message;
        private String finishReason;
    }
}
