package com.musicplayer;

import com.musicplayer.persistence.MetadataService;
import com.musicplayer.persistence.SongMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;

/**
 * LibreriaController: coordinatore MVC tra la View e il modello (Libreria).
 *
 * Dipendenze:
 * - libreria : Libreria → Singleton del modello
 * - factory : BranoFactory → Factory Method per creare brani
 *
 * Responsabilità:
 * 1. Gestione brani: crea (con copia fisica), modifica, elimina.
 * 2. Persistenza CSV delegata a MetadataService.
 *
 * NOTA SUI TAG MULTIPLI:
 * Il campo Tag sul Brano è un enum (valore singolo) per ragioni di dominio.
 * La stringa dei tag multipli (es. "RELAX, Preferiti") è una funzionalità di
 * presentazione/persistenza: vive solo nel CSV (SongMetadata.tag) e nella View.
 * Il metodo modificaTagBrano() gestisce questo caso senza perdere la stringa
 * completa facendo un round-trip attraverso l'enum.
 */
public class LibreriaController {

    // ── Dipendenze ────────────────────────────────────────────────────────────

    private final Libreria libreria = Libreria.getInstance();
    private final BranoFactory factory = new BranoFactory();

    private static final String LIB_DIR = "Libreria";

    // Lista degli iscritti alle notifiche (Observer Pattern)
    private final List<LibreriaObserver> observers = new ArrayList<>();

    public void addObserver(LibreriaObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // =========================================================================
    // Gestione brani
    // =========================================================================

    /**
     * Crea un nuovo brano tramite factory, copia il file fisico nella Libreria,
     * persiste i metadati su CSV e aggiunge il brano al modello Libreria.
     */
    public void aggiungiBrano(String titolo, String autore, String genere, int anno, String percorsoFile, int durataSec,
            String tagRaw) throws ValidazioneException, IOException {
        Tag primoTag = Tag.fromString(tagRaw);
        Brano brano = factory.creaBrano(titolo, autore, genere, anno, percorsoFile, durataSec, primoTag);
        File sorgente = new File(percorsoFile);

        if (brano == null)
            throw new IllegalArgumentException("Brano nullo");

        copiaNellaLibreria(sorgente);

        String filename = PathUtils.filenameFromPath(sorgente.getAbsolutePath());
        brano.setPercorsoFile(filename);

        SongMetadata meta = SongMetadata.buildSongMetadata(brano, filename, tagRaw);
        MetadataService.appendMetadataRow(meta);
        libreria.aggiungiBrano(brano);

        for (LibreriaObserver obs : observers) {
            obs.onBranoAggiunto(brano);
        }
    }

    /**
     * Overload diretto con oggetto File già scelto (usato dalla View dopo
     * FileChooser).
     */
    public void aggiungiBrano(File sorgente, Brano brano) throws IOException {
        if (sorgente == null)
            throw new IllegalArgumentException("File sorgente nullo");
        if (brano == null)
            throw new IllegalArgumentException("Brano nullo");

        copiaNellaLibreria(sorgente);

        String filename = PathUtils.filenameFromPath(sorgente.getAbsolutePath());
        brano.setPercorsoFile(filename);

        MetadataService.appendMetadataRow(new SongMetadata(brano));
        libreria.aggiungiBrano(brano);

        for (LibreriaObserver obs : observers) {
            obs.onBranoAggiunto(brano);
        }
    }

    /**
     * Modifica titolo/autore/genere/anno/durata/tag di un brano.
     *
     * Il campo "tag" nella mappa viene trattato come stringa grezza e scritto
     * direttamente nel CSV senza passare dall'enum, così i tag multipli
     * (es. "RELAX, Preferiti") vengono preservati integralmente.
     * Il campo Tag sull'oggetto Brano in RAM viene aggiornato al primo valore
     * utile per compatibilità con il dominio.
     */
    public void modificaBrano(Brano brano, Map<String, String> dati)
            throws ValidazioneException {
        if (brano == null || dati == null)
            return;

        // Aggiorno il Brano in RAM e valida (lancia ValidazioneException se i dati non
        // vanno)
        libreria.modificaBrano(brano, dati);

        // Costruisco un SongMetadata che porta la stringa tag RAW dal form,
        // bypassando la conversione enum → evita la perdita dei tag multipli.
        String filename = PathUtils.filenameFromPath(brano.getPercorsoFile());

        // Aggiunto "SongMetadata." prima di buildSongMetadata
        SongMetadata metaAggiornato = SongMetadata.buildSongMetadata(brano, filename, dati.get("tag"));

        // 3. Riscrivo la riga nel CSV
        MetadataService.aggiornaMetadata(filename, metaAggiornato);
    }

    /**
     * Aggiorna SOLO la stringa dei tag nel CSV senza toccare gli altri campi.
     * Usato da "Aggiungi tag" nel menu contestuale: concatena il nuovo tag
     * alla stringa esistente e persiste il risultato.
     *
     * @param brano         il brano da aggiornare
     * @param stringaTagRaw la nuova stringa completa dei tag (es. "RELAX,
     *                      Preferiti")
     */
    public void modificaTagBrano(Brano brano, String stringaTagRaw)
            throws ValidazioneException {
        if (brano == null)
            return;

        // Aggiorna il campo enum sul Brano in RAM con il primo tag (compatibilità
        // dominio)
        brano.getClass(); // null-check implicito già fatto sopra
        // Usa setDettagli con solo il tag per aggiornare l'enum in RAM
        Map<String, String> soloTag = new HashMap<>();
        soloTag.put("titolo", brano.getTitolo());
        soloTag.put("autore", brano.getAutore());
        soloTag.put("genere", brano.getGenere());
        soloTag.put("anno", brano.getAnno() == 0 ? "" : String.valueOf(brano.getAnno()));
        soloTag.put("durata", String.valueOf(brano.getDurata()));
        soloTag.put("tag", stringaTagRaw);
        libreria.modificaBrano(brano, soloTag);

        // Persisti nel CSV con la stringa RAW completa (non l'enum serializzato)
        String filename = PathUtils.filenameFromPath(brano.getPercorsoFile());

        SongMetadata meta = SongMetadata.buildSongMetadata(brano, filename, stringaTagRaw);

        MetadataService.aggiornaMetadata(filename, meta);
    }

    /**
     * Overload: modifica tramite SongMetadata (compatibilità con vecchio codice).
     */
    public void modificaBrano(String filename, SongMetadata meta)
            throws ValidazioneException {
        if (filename == null || meta == null)
            return;
        for (IBrano ib : libreria.getBrani()) {
            if (ib instanceof Brano b &&
                    PathUtils.filenameFromPath(b.getPercorsoFile()).equals(filename)) {
                Map<String, String> dati = new HashMap<>();
                dati.put("titolo", meta.title == null ? "" : meta.title);
                dati.put("autore", meta.author == null ? "" : meta.author);
                dati.put("genere", meta.genre == null ? "" : meta.genre);
                dati.put("anno", meta.year == null ? "" : meta.year);
                dati.put("tag", meta.tag == null ? "" : meta.tag);
                this.modificaBrano(b, dati);
                break;
            }
        }
    }

    /**
     * Elimina il brano: file fisico + riga CSV + modello in RAM.
     */
    /**
     * Elimina il brano: file fisico + riga CSV + modello in RAM + rimozione da
     * tutte le playlist.
     */
    public void eliminaBrano(Brano brano) throws IOException {
        if (brano == null)
            return;

        String filename = PathUtils.filenameFromPath(brano.getPercorsoFile());
        Path target = libDir().resolve(filename);

        // 1. Rilascio del file (per evitare FileSystemException su Windows)
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        // 2. Eliminazione fisica
        if (Files.exists(target))
            Files.delete(target);

        // 3. Rimozione dai metadati e dal modello principale
        MetadataService.removeMetadataRow(filename);
        libreria.eliminaBrano(brano);

        // 4. RIMOZIONE SINCRONIZZATA DALLE PLAYLIST
        // Iteriamo su tutte le playlist caricate in memoria
        for (Playlist p : playlistMap.values()) {
            if (p.getBrani().contains(brano)) {
                p.rimuoviBrano(brano); // Rimuove dall'oggetto Playlist in RAM
                MetadataService.salvaPlaylistSpecificaSuCSV(p); // Aggiorna il file .csv della playlist
            }
        }

        // 5. NOTIFICA PER LA VISTA
        // Questo fa scattare l'onBranoEliminato nella LibreriaView
        for (LibreriaObserver obs : observers) {
            obs.onBranoEliminato(brano);
        }
    }

    /**
     * Overload: elimina tramite filename (usato dalla View).
     */
    public void eliminaBranoPerFilename(String filename) throws IOException {
        if (filename == null || filename.isBlank())
            return;
        libreria.getBrani().stream()
                .filter(ib -> ib instanceof Brano b &&
                        PathUtils.filenameFromPath(b.getPercorsoFile()).equals(filename))
                .findFirst()
                .ifPresent(ib -> {
                    try {
                        this.eliminaBrano((Brano) ib);
                    } catch (IOException e) {
                        throw new RuntimeException("Errore eliminazione file", e);
                    }
                });
    }

    // Nel LibreriaController.java
    public Brano trovaBranoDaNome(String nome) {
        // Usiamo getBrani() che esiste già nella tua classe Libreria
        for (IBrano b : libreria.getBrani()) {
            if (b instanceof Brano brano && PathUtils.filenameFromPath(brano.getPercorsoFile()).equals(nome)) {
                // Dobbiamo fare un cast perché getBrani restituisce IBrano
                return brano;
            }
        }
        return null;
    }

    // RIMOSSI PLACEHOLDER PLAYLIST

    // =========================================================================
    // Lettura modello
    // =========================================================================

    public List<IBrano> getBrani() {
        return libreria.getBrani();
    }

    public List<IBrano> cercaBrani(FiltroRicerca filtro) {
        return libreria.cercaBrani(filtro);
    }

    /**
     * Carica i brani dal CSV nella Libreria in-memory (bootstrap applicazione).
     * Da chiamare una volta sola all'avvio.
     */
    public void caricaDaCSV() {
        Map<String, SongMetadata> mappa = new HashMap<>();
        MetadataService.caricaMappaDalCSV(mappa);
        Path libPath = libDir();
        for (SongMetadata meta : mappa.values()) {
            if (!Files.exists(libPath.resolve(meta.filename)))
                continue;
            int anno = 0;
            try {
                if (meta.year != null && !meta.year.isBlank())
                    anno = Integer.parseInt(meta.year.trim());
            } catch (NumberFormatException ignored) {
            }
            int durata = 0;
            try {
                if (meta.duration != null && !meta.duration.isBlank())
                    durata = Integer.parseInt(meta.duration.trim());
            } catch (NumberFormatException ignored) {
            }
            try {
                Brano b = factory.creaBrano(
                        meta.title, meta.author, meta.genre,
                        anno, meta.filename, durata,
                        Tag.fromString(meta.tag));
                libreria.aggiungiBrano(b);
            } catch (ValidazioneException ignored) {
            }
        }

        MetadataService.caricaPlaylistDaCSV(playlistMap, this::findBranoByFilename);
    }

    // =========================================================================
    // Privati
    // =========================================================================

    private Path libDir() {
        Path p = Path.of(System.getProperty("user.dir"), LIB_DIR);
        try {
            if (!Files.exists(p))
                Files.createDirectories(p);
        } catch (IOException ignored) {
        }
        return p;
    }

    private void copiaNellaLibreria(File sorgente) throws IOException {
        if (!sorgente.exists())
            throw new IOException("File non trovato: " + sorgente.getAbsolutePath());
        Files.copy(sorgente.toPath(),
                libDir().resolve(sorgente.getName()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private Brano findBranoByFilename(String fn) {
        for (IBrano ib : getBrani()) {
            if (ib instanceof Brano b && PathUtils.filenameFromPath(b.getPercorsoFile()).equals(fn))
                return b;
        }
        return null;
    }

    // =========================================================================
    // Gestione Playlist
    // =========================================================================

    // Mappa per tracciare le playlist in RAM (Chiave: Nome della Playlist)
    private final Map<String, Playlist> playlistMap = new HashMap<>();

    public java.util.Map<String, Playlist> getPlaylistMap() {
        return playlistMap;
    }

    public Collection<Playlist> getPlaylist() {
        return playlistMap.values();
    }

    public void aggiungiAPlaylist(Brano brano, String playlistName) throws ValidazioneException {
        if (playlistName == null || playlistName.isBlank())
            return;

        String targetName = playlistName;
        boolean exists = false;
        for (String existingName : playlistMap.keySet()) {
            if (existingName.equalsIgnoreCase(playlistName)) {
                targetName = existingName;
                exists = true;
                break;
            }
        }

        if (exists && brano == null) {
            throw new ValidazioneException("Una playlist con lo stesso nome esiste già!");
        }

        // Se la playlist non esiste, creala
        if (!exists) {
            String univoqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
            Playlist nuovaPlaylist = new Playlist(univoqueId, targetName);
            playlistMap.put(targetName, nuovaPlaylist);
            MetadataService.salvaIndicePlaylistSuCSV(playlistMap.values());
            MetadataService.salvaPlaylistSpecificaSuCSV(nuovaPlaylist);
        }

        Playlist pl = playlistMap.get(targetName);
        if (brano != null) {
            try {
                pl.aggiungiBrano(brano);
                MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            } catch (IllegalArgumentException e) {
                // Brano già presente, ignoriamo
            }
        }

        for (LibreriaObserver obs : observers) {
            obs.onPlaylistAggiornata(pl);
        }
    }

    public void rimuoviDaPlaylist(Brano brano, String playlistName) throws ValidazioneException {
        Playlist pl = playlistMap.get(playlistName);
        if (pl != null && brano != null) {
            pl.rimuoviBrano(brano);
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            for (LibreriaObserver obs : observers) {
                obs.onPlaylistAggiornata(pl);
            }
        }
    }

    public void eliminaPlaylist(String nome) throws IOException {
        if (playlistMap.remove(nome) != null) {
            MetadataService.eliminaPlaylistFisica(nome);
            MetadataService.salvaIndicePlaylistSuCSV(playlistMap.values());
            for (LibreriaObserver obs : observers) {
                obs.onPlaylistAggiornata(null);
            }
        }
    }

    public void rinominaPlaylist(String vecchioNome, String nuovoNome) throws IOException, ValidazioneException {
        Playlist p = playlistMap.get(vecchioNome);
        if (p == null)
            return;
        boolean caseChangeOnly = vecchioNome.equalsIgnoreCase(nuovoNome);
        if (!caseChangeOnly) {
            for (String existingName : playlistMap.keySet()) {
                if (existingName.equalsIgnoreCase(nuovoNome)) {
                    throw new ValidazioneException("Una playlist con questo nome esiste già!");
                }
            }
        }

        MetadataService.rinominaPlaylistFisica(vecchioNome, nuovoNome, caseChangeOnly);

        Playlist nuovaPlaylist = new Playlist(p.getId(), nuovoNome);
        for (IBrano ib : p.getBrani()) {
            if (ib instanceof Brano b) {
                try {
                    nuovaPlaylist.aggiungiBrano(b);
                } catch (Exception e) {
                }
            }
        }

        playlistMap.remove(vecchioNome);
        playlistMap.put(nuovoNome, nuovaPlaylist);

        MetadataService.salvaIndicePlaylistSuCSV(playlistMap.values());
        MetadataService.salvaPlaylistSpecificaSuCSV(nuovaPlaylist);

        for (LibreriaObserver obs : observers) {
            obs.onPlaylistAggiornata(nuovaPlaylist);
        }
    }
}