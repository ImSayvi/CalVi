package com.calvi.weather;

import java.time.LocalDate;

public record DailyWeather(LocalDate date, int weatherCode, double tempMax, double tempMin) {
}
