package com.moeasy.scheduler.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.scheduler.scheduler.dto.GraphItemDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurveyUtils {

  private static final String Q_AGE = "귀하의 연령대를 선택해 주세요.";
  private static final String Q_GENDER = "귀하의 성별을 선택해 주세요.";

  public static String summarizeMaxChoices(String resultsJson, ObjectMapper objectMapper)
      throws IOException {
    if (resultsJson == null || resultsJson.isBlank()) {
      return resultsJson;
    }

    TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {
    };
    List<Map<String, Map<String, Object>>> aggregates = objectMapper.readValue(resultsJson,
        typeRef);

    List<Map<String, Map<String, Object>>> summarized = new ArrayList<>();

    for (Map<String, Map<String, Object>> perQuestion : aggregates) {
      // 각 perQuestion에는 단 하나의 키(질문)만 있다고 가정
      if (perQuestion.isEmpty()) {
        summarized.add(perQuestion);
        continue;
      }

      String question = perQuestion.keySet().iterator().next();
      Map<String, Object> answers = perQuestion.get(question);
      if (answers == null || answers.isEmpty()) {
        summarized.add(perQuestion);
        continue;
      }

      // 최대값 계산(others, 숫자 아님 값 제외)
      Long maxVal = null;
      for (Map.Entry<String, Object> e : answers.entrySet()) {
        String key = e.getKey();
        Object val = e.getValue();
          if ("others".equals(key)) {
              continue;
          }
        if (val instanceof Number n) {
          long v = n.longValue();
          if (maxVal == null || v > maxVal) {
            maxVal = v;
          }
        }
      }

      // 최대값이 없으면(숫자 항목 없음) 원본 유지
      if (maxVal == null) {
        summarized.add(perQuestion);
        continue;
      }

      // 최대값만 남긴 맵 구성(others는 있으면 유지)
      Map<String, Object> filteredAnswers = new LinkedHashMap<>();
      if (answers.containsKey("others")) {
        filteredAnswers.put("others", answers.get("others"));
      }
      for (Map.Entry<String, Object> e : answers.entrySet()) {
        String key = e.getKey();
        Object val = e.getValue();
          if ("others".equals(key)) {
              continue;
          }
        if (val instanceof Number n && n.longValue() == maxVal) {
          filteredAnswers.put(key, n);
        }
      }

      Map<String, Map<String, Object>> one = new LinkedHashMap<>();
      one.put(question, filteredAnswers);
      summarized.add(one);
    }

    return objectMapper.writeValueAsString(summarized);
  }

  public static GraphItemDto extractAge(String resultsJson, ObjectMapper objectMapper)
      throws IOException {
    List<String> ageOrder = Arrays.asList("18–24세", "25–34세", "35–44세", "45–54세", "55세 이상");
    Map<String, Integer> contents = new LinkedHashMap<>();
      for (String k : ageOrder) {
          contents.put(k, 0);
      }

    if (resultsJson != null && !resultsJson.isBlank()) {
      TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {
      };
      List<Map<String, Map<String, Object>>> rows = objectMapper.readValue(resultsJson, typeRef);

      for (Map<String, Map<String, Object>> perQuestion : rows) {
          if (perQuestion == null || perQuestion.isEmpty()) {
              continue;
          }
          if (!perQuestion.containsKey(Q_AGE)) {
              continue;
          }

        Map<String, Object> answers = perQuestion.get(Q_AGE);
          if (answers == null) {
              break;
          }

        for (String k : ageOrder) {
          contents.put(k, toInt(answers.get(k)));
        }
        break; // 연령대 문항만 처리
      }
    }

    return GraphItemDto.builder()
        .title("age")
        .type("bar")
        .contents(contents)
        .build();
  }

  public static GraphItemDto extractGender(String resultsJson, ObjectMapper objectMapper)
      throws IOException {
    Map<String, Integer> contents = new LinkedHashMap<>();
    List<String> keys = Arrays.asList("남성", "여성", "응답 거부", "해당 없음(논바이너리 등)");
      for (String k : keys) {
          contents.put(k, 0);
      }

    TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {
    };
    List<Map<String, Map<String, Object>>> rows = objectMapper.readValue(resultsJson, typeRef);

    for (Map<String, Map<String, Object>> perQuestion : rows) {
        if (perQuestion == null || perQuestion.isEmpty()) {
            continue;
        }
        if (!perQuestion.containsKey(Q_GENDER)) {
            continue;
        }

      Map<String, Object> answers = perQuestion.get(Q_GENDER);
        if (answers == null) {
            break;
        }

      for (String k : keys) {
        contents.put(k, toInt(answers.get(k)));
      }
      break; // 성별 문항만 처리하면 종료
    }

    return GraphItemDto.builder()
        .title("gender")
        .type("circle")
        .contents(contents)
        .build();
  }

  public static List<GraphItemDto> extractGraphsData(String resultsJson, ObjectMapper objectMapper)
      throws IOException {
    List<GraphItemDto> graphItemDtoList = new ArrayList<>();

    graphItemDtoList.add(extractAge(resultsJson, objectMapper));
    graphItemDtoList.add(extractGender(resultsJson, objectMapper));
    return graphItemDtoList;
  }

  public static String excludeAgeAndGender(String resultsJson, ObjectMapper objectMapper)
      throws IOException {
    return excludeQuestions(resultsJson, objectMapper, Set.of(Q_AGE, Q_GENDER));
  }

  public static String excludeQuestions(String resultsJson, ObjectMapper objectMapper,
      Collection<String> questionsToExclude) throws IOException {
      if (resultsJson == null || resultsJson.isBlank()) {
          return resultsJson;
      }

    TypeReference<List<Map<String, Object>>> ref = new TypeReference<>() {
    };
    List<Map<String, Object>> rows = objectMapper.readValue(resultsJson, ref);

    List<Map<String, Object>> out = new ArrayList<>();
    for (Map<String, Object> perQuestion : rows) {
        if (perQuestion == null || perQuestion.isEmpty()) {
            continue;
        }
      String question = perQuestion.keySet().iterator().next();
        if (questionsToExclude.contains(question)) {
            continue;
        }
      out.add(perQuestion);
    }
    return objectMapper.writeValueAsString(out);
  }

  private static int toInt(Object v) {
      if (v instanceof Number n) {
          return n.intValue();
      }
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (Exception ignore) {
      return 0;
    }
  }
}