package com.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Libreria: collezione centrale di brani e playlist.
 * Pattern: Singleton + Observer.
 */
public class Libreria {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static Libreria instance;

    private final List<IBrano> catalogo = new ArrayList<>();

    // Playlist non implementata in questo momento
    // private final List<Playlist> playlist = new ArrayList<>();

    private Libreria() {
    }

    public static Libreria getInstance() {
        if (instance == null)
            instance = new Libreria();
        return instance;
    }

    // ── Gestione brani ────────────────────────────────────────────────────────

    public void aggiungiBrano(IBrano b) {
        if (b == null || catalogo.contains(b))
            return;
        catalogo.add(b);
        // observer disabilitato in questo momento
    }

    public void modificaBrano(IBrano b, Map<String, String> dati) throws ValidazioneException {
        if (b instanceof Brano brano) {
            brano.setDettagli(dati);
            // observer disabilitato in questo momento
        }

    }

    /**
     * Rimuove il brano dal catalogo in memoria RAM.
     */
    public void eliminaBrano(IBrano b) {
        if (b == null)
            return;

        // Basta una sola riga per rimuovere l'oggetto
        catalogo.remove(b);

        // Non aggiungere altro qui, la logica di pulizia delle playlist
        // la stiamo gestendo direttamente nel LibreriaController
        // per mantenere il modello Libreria pulito e semplice.
    }

    // ── Gestione playlist ─────────────────────────────────────────────────────
    /*
     * public Playlist creaPlaylist(String nome) {
     * // Playlist non implementata in questo momento
     * // observer disabilitato in questo momento
     * return null;
     * }
     * 
     * public void eliminaPlaylist(Playlist p) {
     * // Playlist non implementata in questo momento
     * // observer disabilitato in questo momento
     * }
     */
    // ── Stato interno ─────────────────────────────────────────────────────────

    public List<IBrano> getBrani() {
        return List.copyOf(catalogo);
    }

    // public List<Playlist> getPlaylist() { return List.copyOf(playlist); }

    // ── Ricerca e ordinamento ─────────────────────────────────────────────────
    /*
     * Ricerca e ordinamento non implementati in questo momento.
     * cercaBrani
     * ordinaBrani
     */

    public boolean isEmpty() {
        return catalogo.isEmpty();
    }

    // Observer disabilitato in questo momento
    // public void addObserver(LibreriaObserver o) { /* observer disabilitato in
    // questo momento */ }
    // public void removeObserver(LibreriaObserver o) { /* observer disabilitato in
    // questo momento */ }

    // Permette a componenti esterni di forzare una notifica di aggiornamento
    // playlist
    public void notificaObserver() {
        /* observer disabilitato in questo momento */ }

}