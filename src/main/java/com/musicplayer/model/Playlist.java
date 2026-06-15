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
    private final List<IBrano> braniOriginali = new ArrayList<>();
    private int playCount = 0;
    private int[] ordinamento = new int[0];

    public Playlist(String id, String nome) {
        this.id = id == null ? "" : id;
        this.nome = nome == null ? "" : nome;
        aggiornaOrdinamento();
    }

    public int[] getOrdinamento() {
        return ordinamento;
    }

    public void setOrdinamento(int[] ordinamento) {
        this.ordinamento = ordinamento;
    }

    private void aggiornaOrdinamento() {
        this.ordinamento = new int[brani.size()];
        for (int i = 0; i < brani.size(); i++) {
            this.ordinamento[i] = i;
        }
    }

    // ── Gestione brani ────────────────────────────────────────────────────────

    /**
     * Aggiunge un brano alla playlist.
     * Verifica tramite contieneBrano() che il brano non sia già presente.
     * In caso di duplicato o brano nullo, lancia un'eccezione.
     */
    public void aggiungiBrano(IBrano b) {
        if (b == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo alla playlist.");
        }

        if (contieneBrano(b)) {
            throw new IllegalArgumentException("Il brano è già presente in questa playlist.");
        }

        brani.add(b);
        braniOriginali.add(b);
        aggiornaOrdinamento();
    }

    public void aggiungiBrano(IBrano b, int index) {
        if (b == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un brano nullo alla playlist.");
        }

        if (contieneBrano(b)) {
            throw new IllegalArgumentException("Il brano è già presente in questa playlist.");
        }

        if (index < 0 || index > brani.size()) {
            index = brani.size();
        }
        brani.add(index, b);
        
        int origIndex = Math.max(0, Math.min(index, braniOriginali.size()));
        braniOriginali.add(origIndex, b);
        aggiornaOrdinamento();
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
                braniOriginali.add(b);
                set.add(b);
            }
        }
        aggiornaOrdinamento();
    }

    public void rimuoviBrano(IBrano b) {
        brani.remove(b);
        braniOriginali.remove(b);
        aggiornaOrdinamento();
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
        braniOriginali.removeAll(toRemove);
        aggiornaOrdinamento();
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

        braniOriginali.remove(b);
        int posOrig = Math.max(0, Math.min(posizione, braniOriginali.size()));
        braniOriginali.add(posOrig, b);
        aggiornaOrdinamento();
    }

    public void ripristinaOrdineOriginale() {
        brani.clear();
        brani.addAll(braniOriginali);
        aggiornaOrdinamento();
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

    public void ordina(com.musicplayer.strategy.OrdinamentoStrategy strategy) {
        strategy.ordina(brani);
        aggiornaOrdinamento();
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