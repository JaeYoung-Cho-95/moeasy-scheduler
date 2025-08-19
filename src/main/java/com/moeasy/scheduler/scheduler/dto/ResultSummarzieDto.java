package com.moeasy.scheduler.scheduler.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResultSummarzieDto {
  private String content;
  private List<String> keywords;
  private List<SentenceItemDto> sentences;
}