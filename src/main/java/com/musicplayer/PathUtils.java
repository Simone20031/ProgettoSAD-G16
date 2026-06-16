package com.musicplayer;

import java.nio.file.Path;

public class PathUtils {
    public static String filenameFromPath(String p) {
        if (p == null) return "";
        try { return Path.of(p).getFileName().toString(); } catch (Exception e) { return p; }
    }

    public static String getLibraryPath() {
        return System.getProperty("musicplayer.libdir", "Libreria");
    }
}
