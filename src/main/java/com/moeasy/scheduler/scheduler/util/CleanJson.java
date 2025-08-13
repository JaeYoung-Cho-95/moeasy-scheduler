package com.moeasy.scheduler.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CleanJson {
    private CleanJson() {}

    // 주관식 제거하고 객관식만 남기는 유틸
    // - 정리된 형태([{question, topChoices, count}] 또는 {question, answers}]) 입력 시: count가 있는 항목만 추려 반환
    // - 원본 집계 형태([{ "질문": { "선택지": 숫자, "others": [...] } }, ...]) 입력 시: 숫자 카운트가 있는 문항만 최빈 선택지(topChoices)와 count로 변환
    // 출력 형태: [{ "question": "...", "topChoices": ["..."], "count": N }]
    public static String toObjectiveOnlyJson(String resultsJson, ObjectMapper objectMapper) throws IOException {
        if (resultsJson == null || resultsJson.isBlank()) {
            return "[]";
        }

        // 1) 먼저 정리된 형태 감지 시도
        try {
            List<Map<String, Object>> arr = objectMapper.readValue(resultsJson, new TypeReference<List<Map<String, Object>>>() {});
            if (!arr.isEmpty() && arr.get(0) != null && arr.get(0).containsKey("question")) {
                List<Map<String, Object>> out = new ArrayList<>();
                for (Map<String, Object> row : arr) {
                    if (row == null) continue;
                    if (row.containsKey("count")) {
                        Map<String, Object> o = new LinkedHashMap<>();
                        o.put("question", Objects.toString(row.getOrDefault("question", "")));
                        o.put("topChoices", toStringList(row.get("topChoices")));
                        Object cnt = row.get("count");
                        int c = (cnt instanceof Number n) ? n.intValue() : 0;
                        o.put("count", c);
                        out.add(o);
                    }
                }
                return objectMapper.writeValueAsString(out);
            }
        } catch (Exception ignore) {
            // 정리된 형태가 아니면 원본 형태로 계속 처리
        }

        // 2) 원본 집계 형태 처리
        TypeReference<List<Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        List<Map<String, Map<String, Object>>> aggregates = objectMapper.readValue(resultsJson, typeRef);

        List<Map<String, Object>> out = new ArrayList<>();

        for (Map<String, Map<String, Object>> perQuestion : aggregates) {
            if (perQuestion == null || perQuestion.isEmpty()) continue;

            String question = perQuestion.keySet().iterator().next();
            Map<String, Object> answers = perQuestion.get(question);
            if (answers == null || answers.isEmpty()) continue;

            Long maxVal = null;
            for (Map.Entry<String, Object> e : answers.entrySet()) {
                String k = e.getKey();
                if ("others".equals(k)) continue;
                Object v = e.getValue();
                if (v instanceof Number n) {
                    long lv = n.longValue();
                    if (maxVal == null || lv > maxVal) maxVal = lv;
                }
            }

            // 숫자가 하나도 없으면(주관식) 스킵
            if (maxVal == null) continue;

            List<String> topChoices = new ArrayList<>();
            for (Map.Entry<String, Object> e : answers.entrySet()) {
                String k = e.getKey();
                if ("others".equals(k)) continue;
                Object v = e.getValue();
                if (v instanceof Number n && n.longValue() == maxVal) {
                    topChoices.add(k);
                }
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("question", question);
            row.put("topChoices", topChoices);
            row.put("count", maxVal.intValue());
            out.add(row);
        }

        return objectMapper.writeValueAsString(out);
    }


    public static String toObjectiveWithPercent(String resultsJson, int totalCount, ObjectMapper objectMapper) throws IOException {
        if (resultsJson == null || resultsJson.isBlank()) {
            return "[]";
        }

        String normalized = resultsJson;

        List<Map<String, Object>> rows;
        try {
            rows = objectMapper.readValue(normalized, new TypeReference<List<Map<String, Object>>>() {});
            boolean looksLikeObjective = !rows.isEmpty()
                    && rows.get(0) != null
                    && rows.get(0).containsKey("question")
                    && (rows.get(0).containsKey("topChoices") || rows.get(0).containsKey("count"));

            if (!looksLikeObjective) {
                normalized = toObjectiveOnlyJson(resultsJson, objectMapper);
                rows = objectMapper.readValue(normalized, new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            normalized = toObjectiveOnlyJson(resultsJson, objectMapper);
            rows = objectMapper.readValue(normalized, new TypeReference<List<Map<String, Object>>>() {});
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row == null) continue;
            if (!row.containsKey("count")) continue;

            String question = Objects.toString(row.get("question"), "");
            List<String> topChoices = toStringList(row.get("topChoices"));
            int count = toInt(row.get("count"));

            double percent = calcPercent(count, totalCount);

            Map<String, Object> outRow = new LinkedHashMap<>();
            outRow.put("question", question);

            // 배열을 벗겨서 단일 문자열로 저장 (여러 개면 첫 번째 사용)
            String topchoice = topChoices.isEmpty() ? "" : topChoices.get(0);
            outRow.put("topChoices", topchoice);

            outRow.put("choicePercent", percent);
            out.add(outRow);
        }

        return objectMapper.writeValueAsString(out);
    }

    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private static double calcPercent(int count, int total) {
        if (total <= 0) return 0.0;
        BigDecimal c = BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100));
        BigDecimal t = BigDecimal.valueOf(total);
        return c.divide(t, 1, RoundingMode.HALF_UP).doubleValue(); // 소수 1자리 반올림
    }

    private static List<String> toStringList(Object obj) {
        if (!(obj instanceof List<?> list)) return List.of();
        List<String> out = new ArrayList<>(list.size());
        for (Object v : list) out.add(String.valueOf(v));
        return out;
    }
}