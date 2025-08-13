package com.moeasy.scheduler.scheduler.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GraphItemDto {

  private String title;
  private String type;
  private Map<String, Integer> contents;
}
