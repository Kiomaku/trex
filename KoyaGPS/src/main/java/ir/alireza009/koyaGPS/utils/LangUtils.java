package ir.alireza009.koyaGPS.utils;

import ir.alireza009.koyaGPS.KoyaGPS;

public class LangUtils {
    public static String getMessage(String path) {
        return Utils.colorizeWithoutPrefix(KoyaGPS.getLangFileManager().getConfig().getString(path, "&7[Unknown Message]"));
    }
}

