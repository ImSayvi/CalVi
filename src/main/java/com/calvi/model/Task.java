package com.calvi.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private LocalDate date;
    private LocalTime time;
    private String desc;
    private boolean done = false;
    private Duration duration;
    private EntryType type;


    //duration - nie jest wymagane, ale jego brak nie będzie z góry zakładał, że to wydarzenie;

    public Task() {
        // potrzebne Jacksonowi do stworzenia obiektu przy wczytywaniu z JSON-a, przed ustawieniem pól przez settery
    }

    public Task(String title, LocalDate date, LocalTime time, String desc, Duration duration, EntryType type) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.type = type;
        this.duration = duration;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isDone() {
        return done;
    }

    public Duration getDuration() {
        return duration;
    }

    public UUID getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public EntryType getType() {
        return type;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    @Override
    public String toString() {
    return "Task\n" +
            "id = " + id + "\n" +
            "title = " + title + "\n" +
            "date = " + date + "\n" +
            "time = " + time + "\n" +
            "desc = " + desc + "\n" +
            "done = " + done + "\n" +
            "duration = " + duration + "\n" +
            "type = " + type + "\n";
    }
}
