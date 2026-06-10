package com.musicplayer.model;



import java.util.ArrayList;
import java.util.List;

/**
 * Playlist: collezione ordinata di IBrano con supporto a iteratori.
 * Implementa Playable (durata totale = somma durate brani).
 */
public class Playlist implements Playable {

    private String id;
    private String nome;
    private final List<IBrano> brani = new ArrayList<>();

    public Playlist(String id, String nome) {
        this.id = id == null ? "" : id;
        this.nome = nome == null ? "" : nome;
    }

    // ── Gestione brani ────────────────────────────────────────────────────────

    /**
     * Task - 10.1: Implementare Playlist.aggiungiBrano()
     * Verifica tramite contieneBrano() che il brano non sia già presente.
     * In caso di duplicato o brano nullo, segnala l'errore al chiamante lanciando
     * un'eccezione.
     */
    public void aggiungiBrano(IBrano b) {
        if (b == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo alla playlist.");
        }

        // Verifica la presenza del duplicato sfruttando il metodo interno
        if (contieneBrano(b)) {
            throw new IllegalArgumentException("Il brano è già presente in questa playlist.");
        }

        brani.add(b);
    }

    public void rimuoviBrano(IBrano b) {
        brani.remove(b);
    }

    public boolean contieneBrano(IBrano b) {
        return brani.contains(b);
    }

    public void spostaBrano(IBrano b, int posizione) {
        if (!brani.contains(b))
            return;
        brani.remove(b);
        int pos = Math.max(0, Math.min(posizione, brani.size()));
        brani.add(pos, b);
    }


    public void rinomina(String nuovoNome) {
        if (nuovoNome == null || nuovoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della playlist non può essere vuoto.");
        }
        this.nome = nuovoNome.trim();
    }

    public List<IBrano> getBrani() {
        return List.copyOf(brani);
    }

    // ── Iteratori (Factory Method) ────────────────────────────────────────────
    public PlaylistIterator creaIterator() {
        return new SequentialIterator(brani);
    }

    public PlaylistIterator creaIteratorShuffle() {
        return new ShuffleIterator(brani);
    }

    public PlaylistIterator creaIteratorLoop() {
        return new LoopIterator(brani);
    }

    // ── Playable ──────────────────────────────────────────────────────────────
    @Override
    public int getDurataTotale() {
        return brani.stream().mapToInt(IBrano::getDurata).sum();
    }
    // ── Accessori ─────────────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}