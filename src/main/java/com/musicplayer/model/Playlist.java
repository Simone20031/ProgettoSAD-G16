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
    private int playCount = 0;

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

    public void aggiungiBrani(java.util.Collection<? extends Playable> col) {
        if (col == null)
            return;
        java.util.Set<IBrano> set = new java.util.HashSet<>(brani);
        for (Playable p : col) {
            if (p instanceof IBrano b) {
                if (set.contains(b)) {
                    throw new IllegalArgumentException("Il brano è già presente in questa playlist.");
                }
                brani.add(b);
                set.add(b);
            }
        }
    }

    public void rimuoviBrano(IBrano b) {
        brani.remove(b);
    }

    public void rimuoviBrani(java.util.Collection<? extends Playable> col) {
        if (col == null)
            return;
        java.util.Set<IBrano> toRemove = new java.util.HashSet<>();
        for (Playable p : col) {
            if (p instanceof IBrano b) {
                toRemove.add(b);
            }
        }
        brani.removeAll(toRemove);
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

    @Override
    public int getPlayCount() {
        return playCount;
    }

    @Override
    public void incrementPlayCount() {
        this.playCount++;
    }

    // Setter for loading from metadata
    public void setPlayCount(int playCount) {
        this.playCount = Math.max(0, playCount);
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