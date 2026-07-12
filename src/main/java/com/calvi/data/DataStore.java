package com.calvi.data;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DataStore {
    private static final String FILE_PATH = "data/calvi-data.json";

    public static void save(AppData data) throws IOException {
        new File("data").mkdirs();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(new File(FILE_PATH), data);
    }

    public static AppData load() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new AppData();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper.readValue(file, AppData.class);
    }
}
