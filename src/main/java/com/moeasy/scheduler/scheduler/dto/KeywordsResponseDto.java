package com.moeasy.scheduler.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KeywordsResponseDto {
    private List<KeywordsItemsDto> positives;
    private List<KeywordsItemsDto> negatives;
}