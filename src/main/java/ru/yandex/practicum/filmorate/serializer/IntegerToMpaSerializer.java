//package ru.yandex.practicum.filmorate.serializer;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonSerializer;
//import com.fasterxml.jackson.databind.SerializerProvider;
//
//import java.io.IOException;
//import java.util.Map;
//
//public class IntegerToMpaSerializer extends JsonSerializer<Integer> {
//    @Override
//    public void serialize(Integer mpaId, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        Map<String, Integer> mpaObject = Map.of("id", mpaId);
//        gen.writeObject(mpaObject);
//    }
//}
