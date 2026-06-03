package com.musicplayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Playlist: collezione ordinata di brani con un nome univoco.
 * Creata e gestita da Libreria tramite creaPlaylist().
 */
public class Playlist {

    private final String    nome;
    private final List<IBrano> brani = new ArrayList<>();

    /**
     * Costruttore ad accesso package-private: solo Libreria può istanziare Playlist.
     *
     * @param nome il nome della playlist (già validato da Libreria)
     */
    Playlist(String nome) {
        this.nome = nome;
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getNome() { return nome; }

    public List<IBrano> getBrani() { return List.copyOf(brani); }

    // ── Gestione brani nella playlist ─────────────────────────────────────────

    /**
     * Aggiunge un brano alla playlist se non è già presente.
     *
     * @param b il brano da aggiungere (ignorato se null o duplicato)
     */
    public void aggiungiBrano(IBrano b) {
        if (b == null || brani.contains(b)) return;
        brani.add(b);
    }

    /**
     * Rimuove un brano dalla playlist.
     *
     * @param b il brano da rimuovere
     */
    public void rimuoviBrano(IBrano b) {
        brani.remove(b);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    @Override
    public String toString() { return nome; }
}