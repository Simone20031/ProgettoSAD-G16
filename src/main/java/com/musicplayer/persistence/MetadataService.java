package com.musicplayer.persistence;




import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MetadataService: responsabile esclusivamente della persistenza CSV.
 */
public class MetadataService {

    // Manteniamo una reference statica al player temporaneo per evitare
    // che il Garbage Collector di JavaFX lo distrugga prima che scatti l'evento
    // onReady.
    private static javafx.scene.media.MediaPlayer tempExtractorPlayer;

    public static void estraiMetadati(File file, java.util.function.Consumer<String[]> callback) {
        String[] result = new String[4];
        result[0] = stripExtension(file.getName());
        result[1] = "";
        result[2] = "";
        result[3] = "0";

        try {
            javafx.scene.media.Media m = new javafx.scene.media.Media(file.toURI().toString());
            tempExtractorPlayer = new javafx.scene.media.MediaPlayer(m);
            tempExtractorPlayer.setOnReady(() -> {
                int sec = (int) m.getDuration().toSeconds();
                result[3] = String.valueOf(sec);
                tempExtractorPlayer.dispose();
                tempExtractorPlayer = null;
                callback.accept(result);
            });
            tempExtractorPlayer.setOnError(() -> {
                tempExtractorPlayer.dispose();
                tempExtractorPlayer = null;
                callback.accept(result);
            });
        } catch (Exception e) {
            if (tempExtractorPlayer != null) {
                tempExtractorPlayer.dispose();
                tempExtractorPlayer = null;
            }
            callback.accept(result);
        }
    }

    public static void appendMetadataRow(SongMetadata meta) throws IOException {
        if (meta == null)
            throw new IllegalArgumentException("Metadata nullo");
        Path metaFile = resolveMetaFile();
        Files.writeString(metaFile, toCSVLine(meta) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static void caricaMappaDalCSV(Map<String, SongMetadata> mappa) {
        mappa.clear();
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile))
            return;
        try {
            for (String line : Files.readAllLines(metaFile, StandardCharsets.UTF_8)) {
                if (line.isBlank())
                    continue;
                SongMetadata m = fromCSVLine(line);
                if (m != null && !m.filename.isBlank()) {
                    mappa.put(m.filename, m);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore in caricaMappaDalCSV: " + e.getMessage());
        }
    }

    public static void aggiornaMetadata(String filename, SongMetadata meta) {
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile))
            return;
        try {
            List<String> out = rewriteExcluding(metaFile, filename);
            out.add(toCSVLine(meta));
            Files.write(metaFile, out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Errore in aggiornaMetadata: " + e.getMessage());
        }
    }

    // Usato per la modifica di un brano
    public static void aggiornaMetadata(String filename, com.musicplayer.model.Brano brano) {
        if (brano == null)
            throw new IllegalArgumentException("Brano nullo");
        aggiornaMetadata(filename, new SongMetadata(brano));
    }

    // Usato per l'eliminazione di un brano
    public static void removeMetadataRow(String filename) throws IOException {
        Path metaFile = resolveMetaFile();
        if (!Files.exists(metaFile))
            return;
        List<String> out = rewriteExcluding(metaFile, filename);
        Files.write(metaFile, out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static com.musicplayer.model.Brano toBrano(SongMetadata m) {
        int anno = 0;
        try {
            if (m.year != null && !m.year.isBlank())
                anno = Integer.parseInt(m.year.trim());
        } catch (NumberFormatException ignored) {
        }

        int durata = 0;
        try {
            if (m.duration != null && !m.duration.isBlank())
                durata = Integer.parseInt(m.duration.trim());
        } catch (NumberFormatException ignored) {
        }

        return new com.musicplayer.model.Brano(
                m.filename,
                m.title,
                m.author,
                m.genre,
                anno,
                m.filename,
                durata,
                com.musicplayer.model.Tag.fromString(m.tag));
    }

    public static String stripExtension(String name) {
        if (name == null)
            return "";
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }

    private static Path resolveMetaFile() {
        Path libDir = Path.of(System.getProperty("user.dir"), "Libreria");
        try {
            if (!Files.exists(libDir))
                Files.createDirectories(libDir);
        } catch (IOException e) {
            System.err.println("Errore in resolveMetaFile: " + e.getMessage());
        }
        return libDir.resolve("metadata.csv");
    }

    private static String toCSVLine(SongMetadata m) {
        return String.join(";",
                safe(m.filename),
                safe(m.title),
                safe(m.author),
                m.year == null ? "" : m.year.trim(),
                m.duration == null ? "" : m.duration.trim(),
                safe(m.genre),
                m.tag == null ? "" : m.tag.trim(),
                String.valueOf(Math.max(0, m.playCount)));
    }

    private static String safe(String s) {
        if (s != null && s.contains(";")) {
            System.err.println("Warning: Il separatore ';' è stato trovato nel dato '" + s + "'. Sarà sostituito con uno spazio.");
        }
        return s == null ? "" : s.replace(";", " ");
    }

    private static SongMetadata fromCSVLine(String line) {
        String[] p = line.split(";", -1);
        try {
            String filename = p.length > 0 ? p[0] : "";
            String title = p.length > 1 ? p[1] : "";
            String author = p.length > 2 ? p[2] : "";
            String year = p.length > 3 ? p[3] : "";
            String duration = p.length > 4 ? p[4] : "";
            String genre = p.length > 5 ? p[5] : "";
            String tag = p.length > 6 ? p[6] : "";
            int playCount = 0;
            try {
                playCount = p.length > 7 ? Integer.parseInt(p[7].trim()) : 0;
            } catch (NumberFormatException ignored) {
            }
            return new SongMetadata(filename, title, author, year, duration, genre, tag, playCount);
        } catch (Exception e) {
            System.err.println("Errore imprevisto parsing CSV line: " + e.getMessage());
            return null;
        }
    }

    private static List<String> rewriteExcluding(Path metaFile, String excludeFilename)
            throws IOException {
        List<String> out = new ArrayList<>();
        for (String line : Files.readAllLines(metaFile, StandardCharsets.UTF_8)) {
            if (line.isBlank())
                continue;
            String[] p = line.split(";", -1);
            String fn = p.length > 0 ? p[0] : "";
            if (!fn.equals(excludeFilename))
                out.add(line);
        }
        return out;
    }
    // =========================================================================
    // Gestione Fisica Playlist (Spostata da LibreriaController)
    // =========================================================================

    private static Path libDir() {
        return Path.of(System.getProperty("user.dir"), "Libreria");
    }

    private static Path playlistDir(String nomePlaylist) {
        return libDir().resolve("Playlist - " + nomePlaylist);
    }

    public static void salvaIndicePlaylistSuCSV(java.util.Collection<com.musicplayer.model.Playlist> playlists) {
        Path csvPath = libDir().resolve("lista_playlist.csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(Files.newBufferedWriter(csvPath))) {
            pw.println("# INDICE GENERALE PLAYLIST");
            pw.println("# Formato: ID_Playlist,Nome_Playlist");
            for (com.musicplayer.model.Playlist pl : playlists) {
                if (!(pl instanceof com.musicplayer.model.SmartPlaylist)) {
                    pw.println(pl.getId() + "," + pl.getNome());
                }
            }
            pw.flush();
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dell'indice globale: " + e.getMessage());
        }
    }

    public static void salvaPlaylistSpecificaSuCSV(com.musicplayer.model.Playlist pl) {
        if (pl == null || pl instanceof com.musicplayer.model.SmartPlaylist)
            return;
        Path cartella = playlistDir(pl.getNome());
        Path csvPath = cartella.resolve("Playlist - " + pl.getNome() + ".csv");

        try {
            if (!Files.exists(cartella)) {
                Files.createDirectories(cartella);
            }
            try (java.io.PrintWriter pw = new java.io.PrintWriter(Files.newBufferedWriter(csvPath))) {
                pw.println("# Playlist: " + pl.getNome());
                pw.println("# PlayCount: " + pl.getPlayCount());
                pw.println("# Formato: ID_Playlist,PercorsoAssoluto_MP3");
                for (com.musicplayer.model.IBrano ib : pl.getBrani()) {
                    if (ib instanceof com.musicplayer.model.Brano b) {
                        pw.println(pl.getId() + "," + b.getPercorsoFile());
                    }
                }
                pw.flush();
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del CSV specifico: " + e.getMessage());
        }
    }

    public static void caricaPlaylistDaCSV(java.util.Map<String, com.musicplayer.model.Playlist> playlistMap,
            java.util.function.Function<String, com.musicplayer.model.Brano> findBrano) {
        Path lib = libDir();
        if (!Files.exists(lib)) return;

        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(lib)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry) && entry.getFileName().toString().startsWith("Playlist - ")) {
                    String folderName = entry.getFileName().toString();
                    Path csvPath = entry.resolve(folderName + ".csv");
                    if (Files.exists(csvPath)) {
                        String realName = null;
                        String id = java.util.UUID.randomUUID().toString().substring(0, 8); // fallback ID
                        
                        int playCount = 0;
                        
                        // 1. Leggi il VERO nome (case-sensitive) e PlayCount dall'header del CSV
                        try (java.io.BufferedReader br = Files.newBufferedReader(csvPath)) {
                            String linea;
                            while ((linea = br.readLine()) != null) {
                                if (linea.startsWith("# Playlist: ")) {
                                    realName = linea.substring("# Playlist: ".length()).trim();
                                } else if (linea.startsWith("# PlayCount: ")) {
                                    try {
                                        playCount = Integer.parseInt(linea.substring("# PlayCount: ".length()).trim());
                                    } catch (NumberFormatException ignored) {}
                                } else if (linea.startsWith("# Formato: ")) {
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Errore lettura header CSV: " + e.getMessage());
                        }

                        if (realName == null) realName = folderName.replace("Playlist - ", "");

                        com.musicplayer.model.Playlist pl = playlistMap.get(realName);
                        if (pl == null) {
                            pl = new com.musicplayer.model.Playlist(id, realName);
                            playlistMap.put(realName, pl);
                        }
                        pl.setPlayCount(playCount);

                        // 2. Carica i brani per questa playlist
                        try (java.io.BufferedReader br = Files.newBufferedReader(csvPath)) {
                            String linea;
                            while ((linea = br.readLine()) != null) {
                                if (linea.isBlank() || linea.startsWith("#")) continue;
                                String[] parti = linea.split(",", 2); 
                                if (parti.length >= 2 && !parti[1].trim().isEmpty()) {
                                    String percorsoAssoluto = parti[1].trim();
                                    String filename = com.musicplayer.PathUtils.filenameFromPath(percorsoAssoluto);
                                    com.musicplayer.model.Brano b = findBrano.apply(filename);
                                    if (b != null) {
                                        try { pl.aggiungiBrano(b); } catch (IllegalArgumentException ignored) {}
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Errore lettura brani della playlist " + realName + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore navigazione directory libreria: " + e.getMessage());
        }
    }

    public static void eliminaPlaylistFisica(String playlistName) {
        Path cartella = playlistDir(playlistName);
        if (Files.exists(cartella)) {
            try {
                // Elimina ricorsivamente tutto il contenuto per evitare cartelle orfane
                Files.walk(cartella)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            } catch (IOException e) {
                System.err.println("Errore durante eliminazione fisica playlist: " + e.getMessage());
            }
        }
    }

    public static void rinominaPlaylistFisica(String vecchioNome, String nuovoNome, boolean isCaseChangeOnly)
            throws IOException {
        Path vecchiaCartella = libDir().resolve("Playlist - " + vecchioNome);
        Path nuovaCartella = libDir().resolve("Playlist - " + nuovoNome);

        if (Files.exists(vecchiaCartella)) {
            // Se la cartella di destinazione esiste già (orfana) e non stiamo facendo un
            // case-change, puliamola
            if (!isCaseChangeOnly && Files.exists(nuovaCartella)) {
                eliminaPlaylistFisica(nuovoNome);
            }

            if (isCaseChangeOnly) {
                Path tempCartella = libDir().resolve("Playlist_temp_" + java.util.UUID.randomUUID().toString());
                Files.move(vecchiaCartella, tempCartella, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tempCartella, nuovaCartella, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(vecchiaCartella, nuovaCartella, StandardCopyOption.REPLACE_EXISTING);
            }

            Path vecchioCSV = nuovaCartella.resolve("Playlist - " + vecchioNome + ".csv");
            Path nuovoCSV = nuovaCartella.resolve("Playlist - " + nuovoNome + ".csv");
            if (Files.exists(vecchioCSV)) {
                if (isCaseChangeOnly) {
                    Path tempCSV = nuovaCartella
                            .resolve("Playlist_temp_" + java.util.UUID.randomUUID().toString() + ".csv");
                    Files.move(vecchioCSV, tempCSV, StandardCopyOption.REPLACE_EXISTING);
                    Files.move(tempCSV, nuovoCSV, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(vecchioCSV, nuovoCSV, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
