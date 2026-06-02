package com.musicplayer.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MetadataService: responsabile esclusivamente della persistenza CSV.
 */
public class MetadataService {

    public static void estraiMetadati(File file, java.util.function.Consumer<String[]> callback) {
        String[] result = new String[4];
        result[0] = stripExtension(file.getName());
        result[1] = "";
        result[2] = "";
        result[3] = "";
        callback.accept(result);
    }

    public static void appendMetadataRow(SongMetadata meta) throws IOException {
        if (meta == null) throw new IllegalArgumentException("Metadata nullo");
        Path metaFile = resolveMetaFile();
        Files.writeString(metaFile, toCSVLine(meta) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static void caricaMappaDalCSV(Map<String, SongMetadata> mappa) {
        mappa.clear();
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile)) return;
        try {
            for (String line : Files.readAllLines(metaFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                SongMetadata m = fromCSVLine(line);
                if (m != null && !m.filename.isBlank()) {
                    mappa.put(m.filename, m);
                }
            }
        } catch (IOException ignored) {}
    }

    public static void aggiornaMetadata(String filename, SongMetadata meta) {
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile)) return;
        try {
            List<String> out = rewriteExcluding(metaFile, filename);
            out.add(toCSVLine(meta));
            Files.write(metaFile, out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {}
    }
    
    // Usato per la modifica di un brano
    public static void aggiornaMetadata(String filename, com.musicplayer.Brano brano) {
        if (brano == null) throw new IllegalArgumentException("Brano nullo");
        aggiornaMetadata(filename, new SongMetadata(brano));
    }
    // Usato per l'eliminazione di un brano
    public static void removeMetadataRow(String filename) throws IOException {
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile)) return;
        List<String> out = rewriteExcluding(metaFile, filename);
        Files.write(metaFile, out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static com.musicplayer.Brano toBrano(SongMetadata m) {
        int anno = 0;
        try {
            if (m.year != null && !m.year.isBlank()) anno = Integer.parseInt(m.year.trim());
        } catch (NumberFormatException ignored) {}

        int durata = 0;
        try {
            if (m.duration != null && !m.duration.isBlank()) durata = Integer.parseInt(m.duration.trim());
        } catch (NumberFormatException ignored) {}

        return new com.musicplayer.Brano(
                m.filename,
                m.title,
                m.author,
                m.genre,
                anno,
                m.filename,
                durata,
                com.musicplayer.Tag.fromString(m.tag)
        );
    }

    public static String stripExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }

    private static Path resolveMetaFile() {
        Path libDir = Path.of(System.getProperty("user.dir"), "Libreria");
        try { if (!Files.exists(libDir)) Files.createDirectories(libDir); }
        catch (IOException ignored) {}
        return libDir.resolve("metadata.csv");
    }

    private static String toCSVLine(SongMetadata m) {
        return String.join(";",
                safe(m.filename),
                safe(m.title),
                safe(m.author),
                m.year     == null ? "" : m.year.trim(),
                m.duration == null ? "" : m.duration.trim(),
                safe(m.genre),
                m.tag      == null ? "" : m.tag.trim(),
                String.valueOf(Math.max(0, m.playCount))
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace(";", " ");
    }

    private static SongMetadata fromCSVLine(String line) {
        String[] p = line.split(";", -1);
        try {
            String filename  = p.length > 0 ? p[0] : "";
            String title     = p.length > 1 ? p[1] : "";
            String author    = p.length > 2 ? p[2] : "";
            String year      = p.length > 3 ? p[3] : "";
            String duration  = p.length > 4 ? p[4] : "";
            String genre     = p.length > 5 ? p[5] : "";
            String tag       = p.length > 6 ? p[6] : "";
            int playCount = 0;
            try { playCount = p.length > 7 ? Integer.parseInt(p[7].trim()) : 0; }
            catch (NumberFormatException ignored) {}
            return new SongMetadata(filename, title, author, year, duration, genre, tag, playCount);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> rewriteExcluding(Path metaFile, String excludeFilename)
            throws IOException {
        List<String> out = new ArrayList<>();
        for (String line : Files.readAllLines(metaFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] p = line.split(";", -1);
            String fn = p.length > 0 ? p[0] : "";
            if (!fn.equals(excludeFilename)) out.add(line);
        }
        return out;
    }
}
