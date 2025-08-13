package com.moeasy.scheduler.scheduler.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatRequestDto {
    private String userMessage;
}
