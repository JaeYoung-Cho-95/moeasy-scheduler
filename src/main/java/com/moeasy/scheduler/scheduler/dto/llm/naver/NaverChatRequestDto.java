package com.moeasy.scheduler.scheduler.dto.llm.naver;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NaverChatRequestDto {
    private List<MessageDto> messages;
    private double topP;
    private int topK;
    private int maxTokens;
    private double temperature;
    private List<String> stop;

}