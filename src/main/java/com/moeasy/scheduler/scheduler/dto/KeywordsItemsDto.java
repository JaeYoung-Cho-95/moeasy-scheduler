package com.moeasy.scheduler.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KeywordsItemsDto {
    private String hashtag;
    private String label;
    private String weight;
    private String objective_frac;
    private String subjective_frac;
    private List<Map<String, String>> evidence;
}