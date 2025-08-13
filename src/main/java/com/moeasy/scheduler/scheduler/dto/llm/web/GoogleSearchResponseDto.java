package com.moeasy.scheduler.scheduler.dto.llm.web;


import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoogleSearchResponseDto {
    private List<GoogleSearchItem> items;
}
