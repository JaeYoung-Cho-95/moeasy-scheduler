package com.moeasy.scheduler.scheduler.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class SaveDataDto {
    private String subject;
    private List<GraphItemDto> graphs;
    private List<String> keywords;
    private String content;
    private List<SentenceItemDto> sentences;
    private Integer totalCount;

    @Builder
    public SaveDataDto(
            String subject,
            List<GraphItemDto> graphs,
            List<String> keywords,
            String content,
            List<SentenceItemDto> sentences,
            Integer totalCount
    ) {
        this.subject = subject;
        this.graphs = graphs;
        this.keywords = keywords;
        this.content = content;
        this.sentences = sentences;
        this.totalCount = totalCount;
    }
}
