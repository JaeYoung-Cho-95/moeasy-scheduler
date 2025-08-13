package com.moeasy.scheduler.scheduler.dto.llm.naver;

import lombok.Data;

@Data
public class NaverChatResponseDto {
    private Status status;
    private Result result;

    @Data
    public static class Status {
        private String code;
        private String message;
    }

    @Data
    public static class Result {
        // 응답용 메시지 DTO를 사용하도록 변경
        private ResponseMessage message;
        // JSON 필드명과 일치시킴 (stopReason -> finishReason)
        private String finishReason;
        // JSON에 있는 필드 추가
        private long created;
        private long seed;
        private Usage usage;
    }

    /**
     * API 응답의 message 부분을 위한 내부 클래스
     */
    @Data
    public static class ResponseMessage {
        private String role;
        // content 필드를 String으로 변경
        private String content;
    }

    /**
     * API 응답의 usage 부분을 위한 내부 클래스
     */
    @Data
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }

    /**
     * API 응답에서 AI가 생성한 content 문자열을 반환합니다.
     * 이 content는 JSON 형식의 문자열일 수 있습니다.
     * @return AI가 생성한 메시지 내용 (String)
     */
    public String getFirstTextMessage() {
        if (this.result != null && this.result.getMessage() != null && this.result.getMessage().getContent() != null) {
            // content가 String이므로 바로 반환
            return this.result.getMessage().getContent();
        }
        return "";
    }
}

