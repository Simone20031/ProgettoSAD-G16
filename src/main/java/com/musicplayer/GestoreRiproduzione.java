package com.musicplayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GestoreRiproduzione {

    private static GestoreRiproduzione instance;

    private Media media;
    private MediaPlayer mediaPlayer;
    private final List<RiproduzioneObserver> observers = new ArrayList<>();
    private PlayerState statoCorrente = new StoppedState();
    private PlaylistIterator iteratorCorrente;
    private Playable elementoCorrente;
    private PlaybackStrategy strategia = new SequentialStrategy();
    private int progressoSecondi;

    private GestoreRiproduzione() {
        // Costruttore privato
    }

    public static GestoreRiproduzione getInstance() {
        if (instance == null) {
            instance = new GestoreRiproduzione();
        }
        return instance;
    }

    // Aggiunto per permettere reset nei test isolati
    public static void resetInstance() {
        instance = null;
    }

    public void addObserver(RiproduzioneObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public void removeObserver(RiproduzioneObserver o) {
        observers.remove(o);
    }

    public PlayerState getStato() {
        return this.statoCorrente;
    }

    public void setStato(PlayerState stato) {
        this.statoCorrente = stato;
    }

    // [REFACTORING FUTURO]: Introdurre l'interfaccia Playable (Brano e Playlist la
    // implementano).
    // - GestoreRiproduzione.playFile(Path) diventerà play(Playable elemento)
    // - Con un PlaylistIterator (SequentialIterator / ShuffleIterator /
    // LoopIterator)
    // sarà possibile riprodurre automaticamente i brani di una playlist in sequenza
    // - GestoreRiproduzione gestirà next() usando la strategia PlaybackStrategy
    // corrente
    // - Questo abilitherà anche la riproduzione con shuffle e loop
    // Priorità: Media — non bloccante per le funzionalità attuali.
    public void playFile(Path file) {
        if (file == null)
            return;

        // 1. SE IL PLAYER ESISTE GIA' E IL BRANO E' LO STESSO
        if (mediaPlayer != null && media != null) {
            try {
                Path currentPath = Path.of(java.net.URI.create(media.getSource()));
                if (currentPath.toAbsolutePath().normalize().equals(file.toAbsolutePath().normalize())) {
                    mediaPlayer.play();
                    statoCorrente = new PlayingState();
                    notificaPlay();
                    return;
                }
            } catch (Exception e) {
                // ignore and recreate
            }
        }

        // 2. DISTRUGGI VECCHIO PLAYER SE E' DIVERSO
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            media = null;
        }

        // 3. CREA NUOVO PLAYER
        try {
            media = new Media(file.toUri().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                int durataSec = (int) media.getDuration().toSeconds();
                notificaReady(durataSec);
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                int sec = (int) newTime.toSeconds();
                this.progressoSecondi = sec;
                notificaProgresso(sec);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                javafx.application.Platform.runLater(() -> {
                    if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
                        playNext();
                    } else {
                        eseguiStop();
                        statoCorrente = new StoppedState();
                    }
                });
            });

            mediaPlayer.play();
            statoCorrente = new PlayingState();
            notificaBranoCambiato(file.toAbsolutePath().toString());
            notificaPlay();

        } catch (Exception e) {
            System.err.println("Errore caricamento media: " + e.getMessage());
        }
    }

    public void setIterator(PlaylistIterator iterator) {
        this.iteratorCorrente = iterator;
    }

    public PlaylistIterator getIterator() {
        return this.iteratorCorrente;
    }

    public PlaybackStrategy getStrategia() {
        return this.strategia;
    }

    public void setStrategia(PlaybackStrategy s) {
        this.strategia = s;
        aggiornaCoda();
    }

    public void play(Playable elemento) {
        this.elementoCorrente = elemento;
        if (elemento instanceof Brano b) {
            Path pathDaUsare = Path.of(b.getPercorsoFile());
            if (!pathDaUsare.isAbsolute()) {
                pathDaUsare = Path.of(System.getProperty("user.dir"), "Libreria").resolve(pathDaUsare.getFileName());
            }
            playFile(pathDaUsare);
        } else if (elemento instanceof Playlist p) {
            PlaylistIterator iter;
            if (strategia instanceof ShuffleStrategy) {
                iter = new ShuffleIterator(p.getBrani());
            } else if (strategia instanceof LoopStrategy) {
                iter = new LoopIterator(p.getBrani());
            } else {
                iter = new SequentialIterator(p.getBrani());
            }
            this.iteratorCorrente = iter;
            playNext();
        }
    }

    public void next() {
        playNext();
    }

    public void setProgressoManuale(int sec) {
        seek(sec);
    }

    public float getProgressoPercentuale() {
        if (mediaPlayer == null || media == null) {
            return 0.0f;
        }
        double current = mediaPlayer.getCurrentTime().toSeconds();
        double total = media.getDuration().toSeconds();
        if (total <= 0) {
            return 0.0f;
        }
        return (float) (current / total * 100.0);
    }

    public void aggiornaCoda() {
        if (iteratorCorrente == null) {
            return;
        }
        List<IBrano> brani = iteratorCorrente.getBrani();
        IBrano currentBrano = null;
        if (media != null) {
            try {
                String currentFilename = Path.of(java.net.URI.create(media.getSource())).getFileName().toString();
                for (IBrano b : brani) {
                    String path = null;
                    if (b instanceof Brano br) {
                        path = br.getPercorsoFile();
                    } else {
                        path = b.getDettagli().get("percorsoFile");
                    }
                    if (path != null && path.endsWith(currentFilename)) {
                        currentBrano = b;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        
        PlaylistIterator nuovoIter;
        if (strategia instanceof ShuffleStrategy) {
            nuovoIter = new ShuffleIterator(brani);
        } else if (strategia instanceof LoopStrategy) {
            nuovoIter = new LoopIterator(brani);
        } else {
            nuovoIter = new SequentialIterator(brani);
        }
        
        if (currentBrano != null) {
            nuovoIter.impostaBranoCorrente(currentBrano);
        }
        this.iteratorCorrente = nuovoIter;
    }

    public void playNext() {
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            IBrano nextBrano = iteratorCorrente.next();
            if (nextBrano instanceof Brano b) {
                Path pathDaUsare = Path.of(b.getPercorsoFile());
                if (!pathDaUsare.isAbsolute()) {
                    pathDaUsare = Path.of(System.getProperty("user.dir"), "Libreria").resolve(pathDaUsare.getFileName());
                }
                playFile(pathDaUsare);
            } else {
                playNext(); // Salta se non è un Brano riproducibile
            }
        } else {
            eseguiStop();
            statoCorrente = new StoppedState();
        }
    }

    public void play() {
        statoCorrente.premiPlay(this);
    }

    public void pausa() {
        statoCorrente.premiPausa(this);
    }

    public void stop() {
        statoCorrente.premiStop(this);
    }

    public void eseguiPausa() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            notificaPausa();
        }
    }

    public void eseguiStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            media = null;
            notificaStop();
        }
    }

    public void seek(int secondi) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(secondi));
        }
    }

    public boolean isCurrentFile(Path file) {
        if (media == null || file == null)
            return false;
        try {
            Path currentPath = Path.of(java.net.URI.create(media.getSource()));
            return currentPath.toAbsolutePath().normalize().equals(file.toAbsolutePath().normalize());
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentMediaSource() {
        return (media != null) ? media.getSource() : "";
    }

    public boolean hasActiveMedia() {
        return mediaPlayer != null;
    }

    public void eseguiPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            notificaPlay();
        }
    }

    private void notificaReady(int durata) {
        for (RiproduzioneObserver o : observers)
            o.onPlayerReady(durata);
    }

    private void notificaPlay() {
        for (RiproduzioneObserver o : observers)
            o.onPlay();
    }

    private void notificaPausa() {
        for (RiproduzioneObserver o : observers)
            o.onPausa();
    }

    private void notificaStop() {
        for (RiproduzioneObserver o : observers)
            o.onStop();
    }

    private void notificaProgresso(int sec) {
        for (RiproduzioneObserver o : observers)
            o.onProgressoAggiornato(sec);
    }

    private void notificaBranoCambiato(String path) {
        for (RiproduzioneObserver o : observers)
            o.onBranoCambiato(path);
    }
}
