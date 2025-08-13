package com.moeasy.scheduler.scheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;

public interface NaverCloudStudio {
    /**
     * 단발성 질문
     */
    <T> T chat(String systemPrompt, String userPrompt, Class<T> responseType);

    /**
     * 단발성 질문 - 제네릭 컬렉션/맵 등 복합 타입 대응
     */
    <T> T chat(String systemPrompt, String userPrompt, TypeReference<T> typeReference);

    <T> T chatHcx007(String systemPrompt, String userPrompt, TypeReference<T> typeReference);


    /**
     * 여러 번의 질문과 답변을 통해 연속적인 대화
     */
//    ChatResponseDto chat(ChatRequestDto request);

    /**
     * load prompt txtfile
     */
    String getPromptWithFilePath(String promptFilePath);

    /**
     * 응답이 이상하게 올 때 정리
     */
    default String cleanResponse(String response) {
        return response
                .replaceAll("(?i)```json", "")
                .replaceAll("(?i)```", "")
                .trim();
    }
}
