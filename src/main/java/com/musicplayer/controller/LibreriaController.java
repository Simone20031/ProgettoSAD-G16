package com.musicplayer.controller;
import com.musicplayer.PathUtils;

import com.musicplayer.model.*;



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
        if (observer != null && !observers.contains(observer)) {
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

        // 0. Se il brano è in riproduzione, fermiamolo per sbloccare il file lock
        if (GestoreRiproduzione.getInstance().isCurrentFile(target)) {
            GestoreRiproduzione.getInstance().stop();
        }

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

    public void ordinaLibreria(CampoOrdinamento campo, String playlistName) {
        libreria.ordinaBrani(campo);
        if (playlistName != null) {
            Playlist p = playlistMap.get(playlistName);
            if (p != null) {
                if (libreria.getUltimoCampoOrdinamento() == null) {
                    p.ripristinaOrdineOriginale();
                } else {
                    p.ordina(new com.musicplayer.strategy.OrdinaBrani(), libreria.getUltimoCampoOrdinamento(), libreria.isUltimoOrdineCrescente());
                }
            }
        }
    }

    public CampoOrdinamento getUltimoCampoOrdinamento() {
        return libreria.getUltimoCampoOrdinamento();
    }

    public boolean isUltimoOrdineCrescente() {
        return libreria.isUltimoOrdineCrescente();
    }

    public void registraAscolto(Playable p) {
        if (p == null) return;
        p.incrementPlayCount();
        if (p instanceof Brano b) {
            String filename = PathUtils.filenameFromPath(b.getPercorsoFile());
            // Need to pass the raw tag from the brano (we don't have the original raw tag here, but we can reconstruct it from SongMetadata if needed, 
            // but Brano's getTag().name() is the fallback. A better way is to use MetadataService.aggiornaMetadata(filename, b))
            // Actually, wait, MetadataService.aggiornaMetadata(String, Brano) uses new SongMetadata(Brano), which loses multiple tags.
            // Let's use the full SongMetadata from memory or just modify the playCount in the file directly.
            // The cleanest way is to load the existing SongMetadata, update playCount, and save it.
            Map<String, SongMetadata> map = new HashMap<>();
            MetadataService.caricaMappaDalCSV(map);
            SongMetadata m = map.get(filename);
            if (m != null) {
                m.playCount = b.getPlayCount();
                MetadataService.aggiornaMetadata(filename, m);
            } else {
                MetadataService.aggiornaMetadata(filename, b);
            }
        } else if (p instanceof Playlist pl) {
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
        }
    }

    public List<IBrano> getTopBraniAscoltati() {
        List<IBrano> sorted = new ArrayList<>(libreria.getBrani());
        // Rimuoviamo i brani con 0 ascolti per la home page
        sorted.removeIf(b -> b.getPlayCount() == 0);
        sorted.sort((b1, b2) -> Integer.compare(b2.getPlayCount(), b1.getPlayCount()));
        return sorted.subList(0, Math.min(5, sorted.size()));
    }

    public List<Playlist> getTopPlaylistsAscoltate() {
        List<Playlist> sorted = new ArrayList<>(playlistMap.values());
        sorted.sort((p1, p2) -> Integer.compare(p2.getPlayCount(), p1.getPlayCount()));
        return sorted.subList(0, Math.min(5, sorted.size()));
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
            } catch (NumberFormatException e) {
                System.err.println("Errore parsing anno in LibreriaController: " + e.getMessage());
            }
            int durata = 0;
            try {
                if (meta.duration != null && !meta.duration.isBlank())
                    durata = Integer.parseInt(meta.duration.trim());
            } catch (NumberFormatException e) {
                System.err.println("Errore parsing durata in LibreriaController: " + e.getMessage());
            }
            try {
                Brano b = factory.creaBrano(
                        meta.title, meta.author, meta.genre,
                        anno, meta.filename, durata,
                        Tag.fromString(meta.tag));
                b.setPlayCount(meta.playCount);
                libreria.aggiungiBrano(b);
            } catch (ValidazioneException e) {
                System.err.println("Errore validazione durante caricamento brano: " + e.getMessage());
            }
        }

        MetadataService.caricaPlaylistDaCSV(playlistMap, this::findBranoByFilename);
        generaSmartPlaylistTematiche();
    }

    private void gestisciConflittoSmartPlaylist(String nome) {
        if (playlistMap.containsKey(nome) && !(playlistMap.get(nome) instanceof SmartPlaylist)) {
            Playlist old = playlistMap.remove(nome);
            com.musicplayer.persistence.MetadataService.eliminaPlaylistFisica(old.getNome());
            com.musicplayer.persistence.MetadataService.salvaIndicePlaylistSuCSV(playlistMap.values());
        }
    }

    public void generaSmartPlaylistTematiche() {
        java.util.Set<Genere> generi = new java.util.HashSet<>();
        java.util.Set<Integer> decenni = new java.util.HashSet<>();
        java.util.Set<Tag> tags = new java.util.HashSet<>();

        for (IBrano ib : libreria.getBrani()) {
            if (ib instanceof Brano b) {
                if (b.getGenereEnum() != null && b.getGenereEnum() != Genere.NESSUNO) {
                    generi.add(b.getGenereEnum());
                }
                if (b.getAnno() > 0) {
                    decenni.add((b.getAnno() / 10) * 10);
                }
                if (b.getTag() != null && b.getTag() != Tag.NESSUNO) {
                    tags.add(b.getTag());
                }
            }
        }

        for (Genere g : generi) {
            String nome = "Genere: " + g.getEtichetta();
            gestisciConflittoSmartPlaylist(nome);
            if (!playlistMap.containsKey(nome)) {
                FiltroRicerca f = new FiltroRicerca();
                f.setGenere(g);
                SmartPlaylist sp = new SmartPlaylist(java.util.UUID.randomUUID().toString().substring(0, 8), nome, f, libreria);
                playlistMap.put(nome, sp);
                addObserver(sp);
            }
        }

        for (Integer d : decenni) {
            String decStr = String.valueOf(d).substring(2);
            String nome = "Anni " + decStr;
            gestisciConflittoSmartPlaylist(nome);
            if (!playlistMap.containsKey(nome)) {
                FiltroRicerca f = new FiltroRicerca();
                f.setDecennio(d);
                SmartPlaylist sp = new SmartPlaylist(java.util.UUID.randomUUID().toString().substring(0, 8), nome, f, libreria);
                playlistMap.put(nome, sp);
                addObserver(sp);
            }
        }

        for (Tag t : tags) {
            String nome = "Tag: " + t.name();
            gestisciConflittoSmartPlaylist(nome);
            if (!playlistMap.containsKey(nome)) {
                FiltroRicerca f = new FiltroRicerca();
                f.setTag(t);
                SmartPlaylist sp = new SmartPlaylist(java.util.UUID.randomUUID().toString().substring(0, 8), nome, f, libreria);
                playlistMap.put(nome, sp);
                addObserver(sp);
            }
        }
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
                // Task 22.1: Aggiorna la coda di riproduzione con la lista aggiornata
                // senza interrompere l'audio in corso.
                GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
            } catch (IllegalArgumentException e) {
                // Brano già presente, ignoriamo
            }
        }

        for (LibreriaObserver obs : observers) {
            obs.onPlaylistAggiornata(pl);
        }
    }

    public void aggiungiAPlaylist(Brano brano, String playlistName, int index) throws ValidazioneException {
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
                pl.aggiungiBrano(brano, index);
                MetadataService.salvaPlaylistSpecificaSuCSV(pl);
                GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
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
            // Task 22.2: Protezione rimozione — impedisci di rimuovere il brano attualmente
            // in riproduzione e mostra l'avviso richiesto dall'AC.
            Path branoPath = libDir().resolve(PathUtils.filenameFromPath(brano.getPercorsoFile()));
            if (GestoreRiproduzione.getInstance().isCurrentFile(branoPath)) {
                throw new ValidazioneException("Traccia in riproduzione, attendere la fine o saltarla.");
            }
            pl.rimuoviBrano(brano);
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            // Task 22.1: Aggiorna la coda di riproduzione con la lista aggiornata
            // senza interrompere l'audio in corso.
            GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
            for (LibreriaObserver obs : observers) {
                obs.onPlaylistAggiornata(pl);
            }
        }
    }

    public void aggiungiBraniAPlaylist(Collection<? extends Playable> braniCollection, String playlistName) throws ValidazioneException {
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

        if (exists && (braniCollection == null || braniCollection.isEmpty())) {
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
        if (braniCollection != null && !braniCollection.isEmpty()) {
            pl.aggiungiBrani(braniCollection);
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            // Aggiorna la coda di riproduzione con la lista aggiornata senza interrompere l'audio
            GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
        }

        for (LibreriaObserver obs : observers) {
            obs.onPlaylistAggiornata(pl);
        }
    }

    public void rimuoviDaPlaylist(Collection<? extends Playable> brani, String playlistName) throws ValidazioneException {
        Playlist pl = playlistMap.get(playlistName);
        if (pl != null && brani != null && !brani.isEmpty()) {
            // Controlla se almeno uno dei brani è in riproduzione
            for (Playable p : brani) {
                if (p instanceof Brano brano) {
                    Path branoPath = libDir().resolve(PathUtils.filenameFromPath(brano.getPercorsoFile()));
                    if (GestoreRiproduzione.getInstance().isCurrentFile(branoPath)) {
                        throw new ValidazioneException("Traccia in riproduzione, attendere la fine o saltarla.");
                    }
                }
            }
            pl.rimuoviBrani(brani);
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
            for (LibreriaObserver obs : observers) {
                obs.onPlaylistAggiornata(pl);
            }
        }
    }

    public void eliminaBrani(Collection<? extends Playable> collection) throws IOException {
        if (collection == null || collection.isEmpty())
            return;

        List<Brano> daEliminare = new ArrayList<>();
        for (Playable p : collection) {
            if (p instanceof Brano b) {
                daEliminare.add(b);
            }
        }

        if (daEliminare.isEmpty())
            return;

        // 0. Controlla se uno dei brani è in riproduzione, fermiamo per sbloccare il file lock
        boolean stopPlayback = false;
        for (Brano b : daEliminare) {
            String filename = PathUtils.filenameFromPath(b.getPercorsoFile());
            Path target = libDir().resolve(filename);
            if (GestoreRiproduzione.getInstance().isCurrentFile(target)) {
                stopPlayback = true;
                break;
            }
        }
        if (stopPlayback) {
            GestoreRiproduzione.getInstance().stop();
        }

        // 1. Rilascio del file
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        // 2. Eliminazioni fisiche e aggiornamento CSV
        for (Brano b : daEliminare) {
            String filename = PathUtils.filenameFromPath(b.getPercorsoFile());
            Path target = libDir().resolve(filename);
            if (Files.exists(target)) {
                try {
                    Files.delete(target);
                } catch (IOException e) {
                    System.err.println("Errore cancellazione file: " + target + " " + e.getMessage());
                }
            }
            MetadataService.removeMetadataRow(filename);
        }

        // 3. Rimozione collettiva nel modello principale
        libreria.eliminaBrani(daEliminare);

        // 4. RIMOZIONE SINCRONIZZATA DALLE PLAYLIST
        for (Playlist p : playlistMap.values()) {
            boolean modified = false;
            java.util.Set<IBrano> setBrani = new java.util.HashSet<>(p.getBrani());
            List<Playable> playablesToRemove = new ArrayList<>();
            for (Brano b : daEliminare) {
                if (setBrani.contains(b)) {
                    playablesToRemove.add(b);
                    modified = true;
                }
            }
            if (modified) {
                p.rimuoviBrani(playablesToRemove);
                MetadataService.salvaPlaylistSpecificaSuCSV(p);
            }
        }

        // 5. NOTIFICA PER LA VISTA
        for (Brano b : daEliminare) {
            for (LibreriaObserver obs : observers) {
                obs.onBranoEliminato(b);
            }
        }
    }

    public void spostaBranoInPlaylist(Brano brano, String playlistName, int nuovaPosizione) throws ValidazioneException {
        Playlist pl = playlistMap.get(playlistName);
        if (pl != null && brano != null) {
            if (libreria.getUltimoCampoOrdinamento() != null) {
                libreria.ordinaBrani(null);
                pl.ripristinaOrdineOriginale();
            }
            pl.spostaBrano(brano, nuovaPosizione);
            MetadataService.salvaPlaylistSpecificaSuCSV(pl);
            // Task 22.1: Aggiorna la coda di riproduzione dopo lo spostamento
            // senza interrompere l'audio in corso.
            GestoreRiproduzione.getInstance().aggiornaCoda(pl.getBrani());
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
        nuovaPlaylist.setPlayCount(p.getPlayCount());
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