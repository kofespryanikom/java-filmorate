package ru.yandex.practicum.filmorate.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class MinutesToDurationDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        int minutes = jsonParser.getIntValue();
        return Duration.ofMinutes(minutes);
    }
}