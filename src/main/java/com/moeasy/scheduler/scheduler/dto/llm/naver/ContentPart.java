package com.moeasy.scheduler.scheduler.dto.llm.naver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class ContentPart {
    private String type;
    private String text;
    @JsonProperty("image_url")
    private String imageUrl;
}
