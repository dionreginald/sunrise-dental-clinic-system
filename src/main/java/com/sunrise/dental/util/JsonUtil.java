package com.sunrise.dental.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Shared Gson instance for all servlets.
 *
 * By default, Gson tries to serialize java.time.LocalDate / LocalTime by
 * reflecting into their private internal fields (e.g. LocalTime#hour) —
 * this fails on modern JDKs because the java.time module doesn't allow
 * that kind of reflective access ("InaccessibleObjectException" /
 * "Failed making field accessible").
 *
 * The fix is to register adapters that tell Gson to convert these types
 * to/from plain ISO strings ("2026-08-20", "14:30") instead of reflecting
 * into them. Every servlet should use JsonUtil.GSON instead of creating
 * its own `new Gson()`, so this fix applies everywhere consistently.
 */
public class JsonUtil {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (date, type, ctx) -> new JsonPrimitive(date.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (time, type, ctx) -> new JsonPrimitive(time.toString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, type, ctx) -> LocalTime.parse(json.getAsString()))
            .create();

    private JsonUtil() {
        // static-only utility class, never instantiated
    }
}
