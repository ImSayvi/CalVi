package com.calvi.weather;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Open-Meteo - darmowe API pogodowe bez klucza/rejestracji (https://open-meteo.com/)
public class WeatherService {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // zwraca null, jeśli nie znaleziono żadnego miasta o podanej nazwie
    public static GeocodeResult geocodeCity(String cityName) throws IOException, InterruptedException {
        String url = "https://geocoding-api.open-meteo.com/v1/search?count=1&language=pl&format=json&name="
                + URLEncoder.encode(cityName, StandardCharsets.UTF_8);

        JsonNode results = get(url).get("results");
        if (results == null || results.isEmpty()) {
            return null;
        }

        JsonNode first = results.get(0);
        return new GeocodeResult(first.get("name").asText(), first.get("latitude").asDouble(), first.get("longitude").asDouble());
    }

    public static List<DailyWeather> fetchForecast(double latitude, double longitude) throws IOException, InterruptedException {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude
                + "&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=auto";

        JsonNode daily = get(url).get("daily");
        JsonNode dates = daily.get("time");
        JsonNode codes = daily.get("weathercode");
        JsonNode maxTemps = daily.get("temperature_2m_max");
        JsonNode minTemps = daily.get("temperature_2m_min");

        List<DailyWeather> forecast = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            forecast.add(new DailyWeather(
                    LocalDate.parse(dates.get(i).asText()),
                    codes.get(i).asInt(),
                    maxTemps.get(i).asDouble(),
                    minTemps.get(i).asDouble()
            ));
        }
        return forecast;
    }

    // osobne od fetchForecast() - widok dnia potrzebuje tylko wschodu/zachodu, bez śmiecenia
    // widoku tygodnia dodatkowymi polami, których i tak by nie pokazywał
    public static Map<LocalDate, SunTimes> fetchSunTimes(double latitude, double longitude) throws IOException, InterruptedException {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude
                + "&daily=sunrise,sunset&timezone=auto";

        JsonNode daily = get(url).get("daily");
        JsonNode dates = daily.get("time");
        JsonNode sunrises = daily.get("sunrise");
        JsonNode sunsets = daily.get("sunset");

        Map<LocalDate, SunTimes> sunTimesByDate = new HashMap<>();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = LocalDate.parse(dates.get(i).asText());
            // "timezone=auto" - Open-Meteo już zwraca to w czasie lokalnym, bez przesunięcia strefy w stringu
            sunTimesByDate.put(date, new SunTimes(
                    date,
                    LocalDateTime.parse(sunrises.get(i).asText()).toLocalTime(),
                    LocalDateTime.parse(sunsets.get(i).asText()).toLocalTime()
            ));
        }
        return sunTimesByDate;
    }

    // WMO weather code -> emoji, patrz https://open-meteo.com/en/docs (tabela "WMO Weather interpretation codes")
    public static String iconForCode(int weatherCode) {
        if (weatherCode == 0) return "☀️";
        if (weatherCode <= 3) return "⛅";
        if (weatherCode == 45 || weatherCode == 48) return "🌫️";
        if (weatherCode >= 51 && weatherCode <= 67) return "🌧️";
        if (weatherCode >= 71 && weatherCode <= 77) return "❄️";
        if (weatherCode >= 80 && weatherCode <= 82) return "🌦️";
        if (weatherCode >= 95) return "⛈️";
        return "🌡️";
    }

    private static JsonNode get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body());
    }
}
