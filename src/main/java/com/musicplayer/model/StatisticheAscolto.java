package com.musicplayer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StatisticheAscolto: mantiene i conteggi degli ascolti per brani e playlist.
 * Implementa i pattern Observer per poter reagire ad eventi di libreria o riproduzione.
 */
public class StatisticheAscolto implements LibreriaObserver, RiproduzioneObserver {

    private final Libreria libreria;
    private final Map<String, Playlist> playlistMap;
    
    // Possiamo mantenere le statistiche in memoria per ottimizzare getTopBrani
    // ma siccome il playCount e' gia' salvato dentro i Brani,
    // possiamo anche delegare il sorting alla lista dei brani.

    public StatisticheAscolto(Libreria libreria, Map<String, Playlist> playlistMap) {
        this.libreria = libreria;
        this.playlistMap = playlistMap;
    }

    public void registraAscolto(Playable p) {
        if (p == null) return;
        p.incrementPlayCount();
    }

    public List<IBrano> getTopBrani(int n) {
        List<IBrano> sorted = new ArrayList<>(libreria.getBrani());
        // Rimuoviamo i brani con 0 ascolti per non mostrarli nella home
        sorted.removeIf(b -> b.getPlayCount() == 0);
        sorted.sort((b1, b2) -> Integer.compare(b2.getPlayCount(), b1.getPlayCount()));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    public List<Playlist> getTopPlaylist(int n) {
        List<Playlist> sorted = new ArrayList<>(playlistMap.values());
        sorted.sort((p1, p2) -> Integer.compare(p2.getPlayCount(), p1.getPlayCount()));
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    // -- LibreriaObserver --
    @Override
    public void onBranoAggiunto(IBrano brano) {}

    @Override
    public void onBranoEliminato(IBrano brano) {}

    @Override
    public void onPlaylistAggiornata(Playlist playlist) {}

    // -- RiproduzioneObserver --
    @Override
    public void onPlayerReady(int durataSecondi) {}

    @Override
    public void onPlay() {}

    @Override
    public void onPausa() {}

    @Override
    public void onStop() {}

    @Override
    public void onProgressoAggiornato(int secondi) {}

    @Override
    public void onBranoCambiato(String nuovoPercorso) {}

    @Override
    public void onBranoRipetuto() {}

    @Override
    public void onCodaAggiornata() {}
}
