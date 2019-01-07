package com.dawninfotek.logx.extension.log4j2;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;


public class JsonUtils {

    private static Gson GSON = new Gson();

    public static Gson getGson() {
        return GSON;
    }

    public static CustomMessage generateCustomMessage(String message) {
        try {
            return getGson().fromJson(message, CustomMessage.class);
        } catch (JsonParseException ex) {
            return null;
        }
    }
}