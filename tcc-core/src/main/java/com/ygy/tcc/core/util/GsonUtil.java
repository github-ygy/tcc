package com.ygy.tcc.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class GsonUtil {

    private static Gson BASE_GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static String toJson(Object obj) {
        return BASE_GSON.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> clazz) {
        return BASE_GSON.fromJson(str, clazz);
    }

}
