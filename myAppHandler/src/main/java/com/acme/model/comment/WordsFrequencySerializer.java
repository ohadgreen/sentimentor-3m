package com.acme.model.comment;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordsFrequencySerializer extends JsonSerializer<LinkedHashMap<String, Integer>> {

    @Override
    public void serialize(LinkedHashMap<String, Integer> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    try {
                        gen.writeNumberField(entry.getKey(), entry.getValue());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        gen.writeEndObject();
    }
}
