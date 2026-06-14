package com.musicplayer.model;




import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.musicplayer.strategy.OrdinamentoStrategy;
import com.musicplayer.strategy.OrdinaBrani;

/**
 * Libreria: collezione centrale di brani e playlist.
 * Pattern: Singleton + Observer.
 */
public class Libreria implements ICatalogo {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static Libreria instance;

    private final List<IBrano> catalogo = new ArrayList<>();

    // private final List<Playlist> playlist = new ArrayList<>();

    private OrdinamentoStrategy ordinamentoStrategy = new OrdinaBrani();
    private CampoOrdinamento ultimoCampoOrdinamento = null;
    private boolean ultimoOrdineCrescente = true;

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

    public void aggiungiBrani(java.util.Collection<? extends Playable> brani) {
        if (brani == null) return;
        java.util.Set<IBrano> set = new java.util.HashSet<>(catalogo);
        for (Playable p : brani) {
            if (p instanceof IBrano b) {
                if (b != null && !set.contains(b)) {
                    catalogo.add(b);
                    set.add(b);
                }
            }
        }
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

    public void eliminaBrani(java.util.Collection<? extends Playable> brani) {
        if (brani == null) return;
        java.util.Set<IBrano> toRemove = new java.util.HashSet<>();
        for (Playable p : brani) {
            if (p instanceof IBrano b) {
                toRemove.add(b);
            }
        }
        catalogo.removeAll(toRemove);
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

    public List<IBrano> cercaBrani(FiltroRicerca filtro) {
        if (filtro == null)
            return getBrani();
        return filtro.applica(getBrani());
    }

    public void ordinaBrani(CampoOrdinamento campo) {
        if (ultimoCampoOrdinamento == campo) {
            ultimoOrdineCrescente = !ultimoOrdineCrescente;
        } else {
            ultimoCampoOrdinamento = campo;
            ultimoOrdineCrescente = true;
        }
        ordinamentoStrategy.ordina(catalogo, ultimoCampoOrdinamento, ultimoOrdineCrescente);
    }

    public CampoOrdinamento getUltimoCampoOrdinamento() {
        return ultimoCampoOrdinamento;
    }

    public boolean isUltimoOrdineCrescente() {
        return ultimoOrdineCrescente;
    }

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