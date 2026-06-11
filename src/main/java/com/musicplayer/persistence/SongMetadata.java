package com.musicplayer.persistence;



import com.musicplayer.model.Brano;

public class SongMetadata {
    public String filename;
    public String title;
    public String author;
    public String year;
    public String duration;
    public String genre;
    public String tag;
    public int playCount;

    public SongMetadata(String filename, String title, String author, String duration, String genre) {
        this(filename, title, author, "", duration, genre, "", 0);
    }

    public SongMetadata(String filename, String title, String author, String year, String duration, String genre, String tag, int playCount) {
        this.filename = filename == null ? "" : filename;
        this.title = title == null ? "" : title;
        this.author = author == null ? "" : author;
        this.year = year == null ? "" : year;
        this.duration = duration == null ? "" : duration;
        this.genre = genre == null ? "" : genre;
        this.tag = tag == null ? "" : tag;
        this.playCount = Math.max(0, playCount);
    }

    public void validaDati() throws com.musicplayer.model.ValidazioneException {
        if (title == null || title.isBlank()) throw new com.musicplayer.model.ValidazioneException("Titolo obbligatorio");
        if (author == null || author.isBlank()) throw new com.musicplayer.model.ValidazioneException("Autore obbligatorio");
        if (genre == null || genre.isBlank()) throw new com.musicplayer.model.ValidazioneException("Genere obbligatorio");
        if (year != null && !year.isBlank()) {
            try {
                int y = Integer.parseInt(year.trim());
                if (y < 1800 || y > 2100) throw new com.musicplayer.model.ValidazioneException("Anno fuori range (1800-2100)");
            } catch (NumberFormatException e) {
                throw new com.musicplayer.model.ValidazioneException("Anno non numerico");
            }
        }
    }

    public SongMetadata(com.musicplayer.model.IBrano brano) {
        if (brano == null) {
            this.filename = ""; this.title = ""; this.author = ""; this.year = "";
            this.duration = ""; this.genre = ""; this.tag = ""; this.playCount = 0;
            return;
        }

        java.util.Map<String, String> dettagli = brano.getDettagli();
        String percorsoFile = dettagli.getOrDefault("percorsoFile", "");
        String id = dettagli.getOrDefault("id", "");

        // Salva il percorso ASSOLUTO completo (non solo il filename)
        // Questo permette di ritrovare il file originale al riavvio dell'applicazione
        if (percorsoFile.isBlank()) {
            this.filename = id;
        } else {
            this.filename = percorsoFile; // percorso assoluto originale
        }

        this.title = brano.getTitolo() == null ? "" : brano.getTitolo();
        this.author = dettagli.getOrDefault("author", dettagli.getOrDefault("autore", ""));
        String annoStr = dettagli.getOrDefault("anno", "");
        this.year = "0".equals(annoStr) ? "" : annoStr;
        this.duration = String.valueOf(brano.getDurata());
        this.genre = dettagli.getOrDefault("genere", "");
        this.tag = dettagli.getOrDefault("tag", "NESSUNO");
        this.playCount = brano.getPlayCount();
    }
    
    /**
     * Costruisce un SongMetadata dall'oggetto Brano corrente in RAM,
     * usando la stringa tag RAW fornita invece di serializzare l'enum.
     * Questo è il punto centrale che evita la perdita dei tag multipli.
     */
    public static SongMetadata buildSongMetadata(Brano brano, String filename, String tagRaw) {
        String annoStr = brano.getAnno() == 0 ? "" : String.valueOf(brano.getAnno());
        String tagDaSalvare = (tagRaw != null) ? tagRaw : brano.getTag().name();
        return new SongMetadata(
                filename,
                brano.getTitolo(),
                brano.getAutore(),
                annoStr,
                String.valueOf(brano.getDurata()),
                brano.getGenere(),
                tagDaSalvare,
                brano.getPlayCount()
        );
    }
}
