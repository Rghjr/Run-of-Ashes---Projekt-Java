package com.runofashes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Random RNG = new Random();

    public static List<GameEvent> loadEvents(String filename) throws Exception {
        InputStream is = EventLoader.class
                .getResourceAsStream("/com/runofashes/" + filename);
        if (is == null) throw new IllegalArgumentException("Nie znaleziono: " + filename);
        return MAPPER.readValue(is, new TypeReference<List<GameEvent>>() {});
    }

    public static Map<String, List<String>> loadEndings() throws Exception {
        InputStream is = EventLoader.class
                .getResourceAsStream("/com/runofashes/endings.json");
        if (is == null) throw new IllegalArgumentException("Nie znaleziono: endings.json");
        return MAPPER.readValue(is, new TypeReference<Map<String, List<String>>>() {});
    }

    public static String pickEnding(Map<String, List<String>> endings, String stat) {
        List<String> options = endings.getOrDefault(stat, List.of("Koniec."));
        return options.get(RNG.nextInt(options.size()));
    }
}