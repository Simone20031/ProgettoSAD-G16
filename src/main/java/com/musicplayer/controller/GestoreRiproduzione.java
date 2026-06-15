package com.musicplayer.controller;

import com.musicplayer.model.*;
import com.musicplayer.strategy.*;
import com.musicplayer.state.*;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GestoreRiproduzione implements LibreriaObserver {

    private static GestoreRiproduzione instance;

    private Media media;
    private MediaPlayer mediaPlayer;
    private final List<RiproduzioneObserver> observers = new ArrayList<>();
    private PlayerState statoCorrente = new StoppedState();
    private PlaylistIterator iteratorCorrente;
    private PlaybackStrategy strategia = new SequentialStrategy();
    private boolean singleSongLoop = false;
    private double globalVolume = 0.5;

    public void setVolume(double vol) {
        this.globalVolume = vol;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(vol);
        }
    }

    public double getVolume() {
        return globalVolume;
    }
    private Playlist playlistCorrente;

    public Playlist getPlaylistCorrente() {
        return playlistCorrente;
    }

    private GestoreRiproduzione() {}

    public static GestoreRiproduzione getInstance() {
        if (instance == null) {
            instance = new GestoreRiproduzione();
        }
        return instance;
    }

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

    public void playFile(Path file) {
        if (file == null)
            return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            media = null;
        }

        try {
            media = new Media(file.toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(globalVolume);

            mediaPlayer.setOnReady(() -> {
                int durataSec = (int) media.getDuration().toSeconds();
                notificaReady(durataSec);
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                int sec = (int) newTime.toSeconds();
                notificaProgresso(sec);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                javafx.application.Platform.runLater(() -> {
                    if (singleSongLoop) {
                        mediaPlayer.seek(Duration.ZERO);
                        mediaPlayer.play();
                        notificaBranoRipetuto();
                    } else if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
                        playNext();
                    } else {
                        eseguiPausa();
                        statoCorrente = new PausedState();
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

    public void setSingleSongLoop(boolean singleSongLoop) {
        this.singleSongLoop = singleSongLoop;
    }

    public boolean isSingleSongLoop() {
        return this.singleSongLoop;
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
        if (elemento instanceof Brano b) {
            this.playlistCorrente = null;
            Path pathDaUsare = Path.of(b.getPercorsoFile());
            if (!pathDaUsare.isAbsolute()) {
                pathDaUsare = Path.of(System.getProperty("user.dir"), "Libreria").resolve(pathDaUsare.getFileName());
            }
            playFile(pathDaUsare);
        } else if (elemento instanceof Playlist p) {
            this.playlistCorrente = p;
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

    public void skipAvanti() {
        statoCorrente.premiSkipAvanti(this);
    }

    public void skipIndietro() {
        statoCorrente.premiSkipIndietro(this);
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
        if (playlistCorrente != null) {
            aggiornaCoda(playlistCorrente.getBrani());
        } else if (iteratorCorrente != null) {
            List<IBrano> brani = iteratorCorrente.getBrani();
            aggiornaCoda(brani);
        }
    }

    @Override
    public void onBranoAggiunto(IBrano brano) {}

    @Override
    public void onBranoEliminato(IBrano brano) {}

    @Override
    public void onPlaylistAggiornata(Playlist playlist) {
        if (playlistCorrente != null && playlist != null && playlistCorrente.getId().equals(playlist.getId())) {
            aggiornaCoda();
        }
    }

    /**
     * Task 22.1 - Aggiorna la coda di riproduzione con la lista di brani
     * aggiornata.
     * Usato quando un brano viene aggiunto, rimosso o spostato nella playlist
     * durante
     * la riproduzione: ricalcola l'iteratore corrente preservando la posizione del
     * brano in riproduzione senza interrompere l'audio.
     *
     * @param nuoviBrani lista aggiornata dei brani della playlist
     */
    public void aggiornaCoda(List<IBrano> nuoviBrani) {
        if (iteratorCorrente == null || nuoviBrani == null) {
            return;
        }
        if (nuoviBrani.isEmpty()) {
            // Nessun brano rimasto: azzera l'iteratore
            iteratorCorrente = null;
            return;
        }

        // Individua il brano attualmente in riproduzione nella nuova lista
        IBrano currentBrano = null;
        if (media != null) {
            try {
                String currentFilename = Path.of(java.net.URI.create(media.getSource())).getFileName().toString();
                for (IBrano b : nuoviBrani) {
                    String path = (b instanceof Brano br)
                            ? br.getPercorsoFile()
                            : b.getDettagli().get("percorsoFile");
                    if (path != null && path.endsWith(currentFilename)) {
                        currentBrano = b;
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        PlaylistIterator nuovoIter;
        if (strategia instanceof ShuffleStrategy) {
            nuovoIter = new ShuffleIterator(nuoviBrani);
        } else if (strategia instanceof LoopStrategy) {
            nuovoIter = new LoopIterator(nuoviBrani);
        } else {
            nuovoIter = new SequentialIterator(nuoviBrani);
        }

        if (currentBrano != null) {
            nuovoIter.impostaBranoCorrente(currentBrano);
        }
        this.iteratorCorrente = nuovoIter;
        notificaCodaAggiornata();
    }

    public void playNext() {
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            IBrano nextBrano = iteratorCorrente.next();
            if (nextBrano instanceof Brano b) {
                Path pathDaUsare = Path.of(b.getPercorsoFile());
                if (!pathDaUsare.isAbsolute()) {
                    pathDaUsare = Path.of(System.getProperty("user.dir"), "Libreria")
                            .resolve(pathDaUsare.getFileName());
                }
                playFile(pathDaUsare);
            } else {
                playNext(); // Salta se non è un Brano riproducibile
            }
        } else {
            eseguiPausa();
            statoCorrente = new PausedState();
        }
    }

    public void playPrevious() {
        if (iteratorCorrente != null && iteratorCorrente.hasPrevious()) {
            IBrano prevBrano = iteratorCorrente.previous();
            if (prevBrano instanceof Brano b) {
                Path pathDaUsare = Path.of(b.getPercorsoFile());
                if (!pathDaUsare.isAbsolute()) {
                    pathDaUsare = Path.of(System.getProperty("user.dir"), "Libreria")
                            .resolve(pathDaUsare.getFileName());
                }
                playFile(pathDaUsare);
            } else {
                playPrevious(); // Salta se non è un Brano riproducibile
            }
        } else {
            // Se non c'è un brano precedente, magari ricominciamo quello attuale da 0
            if (mediaPlayer != null) {
                seek(0);
            }
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
            mediaPlayer.pause();
            mediaPlayer.seek(Duration.ZERO);
            notificaStop();
        }
    }

    public void clearMedia() {
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
        } catch (Exception ignored) {
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

    private void notificaBranoRipetuto() {
        for (RiproduzioneObserver o : observers)
            o.onBranoRipetuto();
    }

    private void notificaCodaAggiornata() {
        for (RiproduzioneObserver o : observers)
            o.onCodaAggiornata();
    }
}
