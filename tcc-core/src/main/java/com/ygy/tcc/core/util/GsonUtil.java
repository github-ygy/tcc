package com.ygy.tcc.core.util;

import com.google.common.collect.Lists;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;


public class GsonUtil {

    private static final JsonParser PARSER = new JsonParser();

    private static final Gson BASE_GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static String toJson(Object obj) {
        return BASE_GSON.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> clazz) {
        return BASE_GSON.fromJson(str, clazz);
    }

    public static <T> T fromJson(JsonElement json, Class<T> clazz) {
        return BASE_GSON.fromJson(json, clazz);
    }

    public static <T> List<T> parseList(String str, Class<T> clazz) {
        List<T> ret = Lists.newArrayList();
        JsonArray array = parseJsonArray(str);
        if (array != null) {
            Iterator<JsonElement> var4 = array.iterator();

            while(var4.hasNext()) {
                JsonElement ele = var4.next();
                if (!(ele instanceof JsonNull)) {
                    ret.add(fromJson(ele, clazz));
                }
            }
        }

        return ret;
    }

    public static JsonArray parseJsonArray(String src) {
        return StringUtils.isBlank(src) ? new JsonArray() : getParser().parse(src).getAsJsonArray();
    }

    public static JsonParser getParser() {
        return PARSER;
    }

}
