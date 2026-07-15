package com.calvi.data;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DataStore {
    private static final String FILE_PATH = "data/calvi-data.json";

    public static void save(AppData data) throws IOException {
        new File("data").mkdirs();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); //żeby priogram wiedział jak daty do jsona zapisać
        mapper.enable(SerializationFeature.INDENT_OUTPUT); //wcięcia w jsonie

        mapper.writeValue(new File(FILE_PATH), data);
    }

    public static AppData load() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new AppData();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // jeśli w pliku pojawi się klucz, którego dana klasa już nie ma (np. po zmianie kodu),
        // nie wywalaj całego wczytywania - po prostu go zignoruj
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(file, AppData.class);
    }
}
