package com.moeasy.scheduler.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentencesListDto {
    @JsonAlias({"insights", "sentences"})
    private List<SentenceItemDto> sentences;
}