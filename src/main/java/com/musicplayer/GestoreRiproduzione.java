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
                notificaProgresso((int) newTime.toSeconds());
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                eseguiStop();
                statoCorrente = new StoppedState();
            });

            mediaPlayer.play();
            statoCorrente = new PlayingState();
            notificaPlay();

        } catch (Exception e) {
            System.err.println("Errore caricamento media: " + e.getMessage());
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
}
