package com.musicplayer;

import java.util.List;

/**
 * StatoPlaylist: stato del contesto "Playlist" del pattern State.
 */
public class StatoPlaylist implements Stato {

    private final Playlist playlist;

    public StatoPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public List<String> getOpzioniSingolo() {
        return List.of("Aggiungi tag", "Modifica", "Elimina brano", "Aggiungi a playlist", "Rimuovi da questa playlist");
    }

    public void eseguiOpzione(String op, Brano brano, LibreriaController controller) {
        if (op == null || controller == null || brano == null)
            return;

        switch (op) {
            case "Rimuovi da questa playlist" -> controller.rimuoviDaPlaylist(playlist, brano);
            case "Aggiungi a playlist" -> controller.aggiungiAPlaylist(brano, "default");
            default -> System.out.println("Opzione gestita altrove o non valida: " + op);
        }
    }
}
