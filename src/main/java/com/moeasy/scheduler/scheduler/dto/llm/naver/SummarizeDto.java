package com.moeasy.scheduler.scheduler.dto.llm.naver;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummarizeDto {
    private boolean searchWeb;

    private String properNoun;

    private List<String> category;
}