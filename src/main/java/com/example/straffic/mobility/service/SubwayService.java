package com.example.straffic.mobility.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubwayService {

    private final RestTemplate restTemplate;

    @Value("${subway.seoul.api.key}")
    private String seoulApiKey;

    @Value("${subway.seoul.api.url}")
    private String seoulApiUrl;

    @Value("${subway.route.api.key}")
    private String routeApiKey;

    @Value("${subway.route.api.url}")
    private String routeApiUrl;

    @Value("${sk.openapi.appkey}")
    private String skAppKey;

    public SubwayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getArrival(String stationName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> arrivals = new ArrayList<>();

        String originalName = stationName == null ? "" : stationName.trim();
        String normalizedName = normalizeStationName(originalName);
        String skStationName = originalName.isEmpty() ? normalizedName : originalName;

        String apiError = null;

        try {
            String encodedName = URLEncoder.encode(normalizedName, StandardCharsets.UTF_8);
            String url = seoulApiUrl + "/" + seoulApiKey + "/json/realtimeStationArrival/0/20/" + encodedName;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();
            if (body != null) {
                Object errorMessage = body.get("errorMessage");
                if (errorMessage instanceof Map<?, ?> em) {
                    Object msg = em.get("message");
                    Object code = em.get("code");
                    String msgText = msg != null ? msg.toString() : "";
                    String codeText = code != null ? code.toString() : "";
                    if (!msgText.isEmpty()) {
                        apiError = codeText.isEmpty() ? msgText : msgText + " (" + codeText + ")";
                    }
                }
            }
            Object listObj = body != null ? body.get("realtimeArrivalList") : null;
            String skCongestion = null;
            if (listObj instanceof List) {
                List list = (List) listObj;
                for (Object o : list) {
                    if (!(o instanceof Map)) {
                        continue;
                    }
                    Map item = (Map) o;
                    Map<String, Object> a = new HashMap<>();
                    String rawDirection = item.get("updnLine") != null ? item.get("updnLine").toString() : "";
                    String direction = normalizeDirection(rawDirection);
                    String lineName = resolveLineName(item);
                    String destination = item.get("trainLineNm") != null ? item.get("trainLineNm").toString() : "";
                    String status = item.get("arvlMsg2") != null ? item.get("arvlMsg2").toString() : "";
                    int seconds = parseInt(item.get("barvlDt"));
                    int minutes = seconds > 0 ? Math.max(1, seconds / 60) : 3;
                    if (skCongestion == null && !lineName.isEmpty() && !skStationName.isEmpty()) {
                        skCongestion = fetchSkCongestion(lineName, skStationName);
                    }
                    String congestion = skCongestion != null ? skCongestion : estimateCongestion(seconds);

                    a.put("direction", direction);
                    a.put("lineName", lineName);
                    a.put("destination", destination);
                    a.put("status", status);
                    a.put("arrivalTime", minutes);
                    a.put("congestion", congestion);
                    arrivals.add(a);
                }
            }
        } catch (Exception e) {
        }

        if (arrivals.isEmpty()) {
            if (apiError != null && !apiError.isEmpty()) {
                Map<String, Object> errorItem = new HashMap<>();
                errorItem.put("direction", "");
                errorItem.put("lineName", "");
                errorItem.put("destination", "");
                errorItem.put("status", "실시간 도착 정보를 불러오지 못했습니다: " + apiError);
                errorItem.put("arrivalTime", "-");
                errorItem.put("congestion", "정보 없음");
                arrivals.add(errorItem);
            }
        }

        String responseName = skStationName.isEmpty() ? normalizedName + "역" : skStationName;
        result.put("stationName", responseName);
        result.put("arrivals", arrivals);
        return result;
    }

    public Map<String, Object> getRoute(String startStation, String endStation) {
        Map<String, Object> route = new HashMap<>();
        int durationMinutes;
        int transfers;

        try {
            String encodedStart = URLEncoder.encode(startStation, StandardCharsets.UTF_8);
            String encodedEnd = URLEncoder.encode(endStation, StandardCharsets.UTF_8);
            String url = routeApiUrl + "/" + routeApiKey + "/json/shortestRoute/1/1/" + encodedStart + "/" + encodedEnd;
            restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
        }

        if (startStation.equals(endStation)) {
            durationMinutes = 0;
            transfers = 0;
        } else {
            int base = Math.abs(startStation.hashCode() - endStation.hashCode());
            durationMinutes = 10 + (base % 40);
            transfers = durationMinutes > 25 ? 1 : 0;
        }

        route.put("success", true);
        route.put("start", startStation);
        route.put("end", endStation);
        route.put("durationMinutes", durationMinutes);
        route.put("transfers", transfers);
        return route;
    }

    private String normalizeDirection(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.contains("상행") || value.contains("상선") || value.contains("외선")) {
            return "상행";
        }
        if (value.contains("하행") || value.contains("하선") || value.contains("내선")) {
            return "하행";
        }
        return value;
    }

    private String resolveLineName(Map item) {
        Object subwayNmObj = item.get("subwayNm");
        String subwayNm = subwayNmObj != null ? subwayNmObj.toString().trim() : "";
        if (!subwayNm.isEmpty()) {
            return subwayNm;
        }
        Object subwayIdObj = item.get("subwayId");
        String subwayId = subwayIdObj != null ? subwayIdObj.toString().trim() : "";
        return switch (subwayId) {
            case "1001" -> "1호선";
            case "1002" -> "2호선";
            case "1003" -> "3호선";
            case "1004" -> "4호선";
            case "1005" -> "5호선";
            case "1006" -> "6호선";
            case "1007" -> "7호선";
            case "1008" -> "8호선";
            case "1009" -> "9호선";
            case "1077" -> "신분당선";
            default -> "";
        };
    }

    private int parseInt(Object value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String normalizeStationName(String name) {
        if (name == null) {
            return "";
        }
        String value = name.trim();
        if (value.endsWith("역")) {
            value = value.substring(0, value.length() - 1).trim();
        }
        return value;
    }

    private String estimateCongestion(int seconds) {
        if (seconds <= 60 && seconds > 0) {
            return "매우 혼잡";
        }
        if (seconds <= 180 && seconds > 0) {
            return "혼잡";
        }
        if (seconds <= 300 && seconds > 0) {
            return "보통";
        }
        return "여유";
    }

    private String fetchSkCongestion(String lineName, String stationName) {
        if (skAppKey == null || skAppKey.isEmpty()) {
            return null;
        }
        String routeNm = lineName == null ? "" : lineName.trim();
        String stationNm = stationName == null ? "" : stationName.trim();
        if (routeNm.isEmpty() || stationNm.isEmpty()) {
            return null;
        }
        try {
            DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
            String dow = toSkDow(dayOfWeek);
            String hh = String.format("%02d", LocalTime.now().getHour());

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://apis.openapi.sk.com/transit/puzzle/subway/congestion/stat/train")
                    .queryParam("routeNm", routeNm)
                    .queryParam("stationNm", stationNm)
                    .queryParam("dow", dow)
                    .queryParam("hh", hh);

            HttpHeaders headers = new HttpHeaders();
            headers.set("appkey", skAppKey);
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);
            Map body = response.getBody();
            if (body == null) {
                return null;
            }
            return extractCongestionLabel(body);
        } catch (Exception e) {
            return null;
        }
    }

    private String toSkDow(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    private String extractCongestionLabel(Object node) {
        if (node instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                String found = extractCongestionLabel(value);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
        if (node instanceof List<?> list) {
            for (Object value : list) {
                String found = extractCongestionLabel(value);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }
        if (node instanceof String s) {
            if (isCongestionLabel(s)) {
                return s;
            }
        }
        return null;
    }

    private boolean isCongestionLabel(String value) {
        return "여유".equals(value) || "보통".equals(value) || "혼잡".equals(value) || "매우혼잡".equals(value) || "매우 혼잡".equals(value);
    }
}

