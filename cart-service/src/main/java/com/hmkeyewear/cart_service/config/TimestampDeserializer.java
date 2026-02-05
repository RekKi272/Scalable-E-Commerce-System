package com.hmkeyewear.cart_service.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.Timestamp;

import java.io.IOException;

public class TimestampDeserializer extends JsonDeserializer<Timestamp> {

    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = p.getCodec().readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        long seconds = node.get("seconds").asLong();
        int nanos = node.get("nanos").asInt();

        return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
    }
}
