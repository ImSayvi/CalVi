package com.calvi.data;

import java.util.ArrayList;
import java.util.List;

import com.calvi.model.Note;
import com.calvi.model.Task;

public class AppData {
    private List<Note> notes = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Task> getTasks() {
        return tasks;
    }
    
}
