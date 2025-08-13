package com.moeasy.scheduler.scheduler.dto.llm.naver;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NaverChatHcx007RequestDto {
    private List<MessageDto> messages;
    private double topP;
    private int topK;
    private Map<String, String> thinking;
    private int maxCompletionTokens;
    private double temperature;
    private List<String> stop;
}