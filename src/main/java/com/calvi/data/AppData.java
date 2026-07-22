package com.calvi.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.calvi.model.DailyTask;
import com.calvi.model.Note;
import com.calvi.model.Task;
import com.calvi.model.WeatherLocation;

public class AppData {
    private List<Note> notes = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<DailyTask> dailyTasks = new ArrayList<>();
    private LocalDate lastDailyReset; // ostatni dzień, dla którego wyzerowano zadania dzienne - patrz Main.start()
    private WeatherLocation weatherLocation = new WeatherLocation();

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void setWeatherLocation(WeatherLocation weatherLocation) {
        this.weatherLocation = weatherLocation;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setDailyTasks(List<DailyTask> dailyTasks) {
        this.dailyTasks = dailyTasks;
    }

    public void setLastDailyReset(LocalDate lastDailyReset) {
        this.lastDailyReset = lastDailyReset;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<DailyTask> getDailyTasks() {
        return dailyTasks;
    }

    public LocalDate getLastDailyReset() {
        return lastDailyReset;
    }

    public WeatherLocation getWeatherLocation() {
        return weatherLocation;
    }

}
