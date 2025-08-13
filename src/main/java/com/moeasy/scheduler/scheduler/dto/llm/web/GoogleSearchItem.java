package com.moeasy.scheduler.scheduler.dto.llm.web;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoogleSearchItem {
    private String title;
    private String link;
    private String snippet;
}