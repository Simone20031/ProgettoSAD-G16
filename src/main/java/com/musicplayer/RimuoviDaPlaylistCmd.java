package com.musicplayer;

public class RimuoviDaPlaylistCmd implements Command {
    private final Playlist playlist;
    private final IBrano brano;

    public RimuoviDaPlaylistCmd(Playlist playlist, IBrano brano) {
        this.playlist = playlist;
        this.brano = brano;
    }

    @Override
    public void esegui() throws ValidazioneException {
        // Se si volessero aggiungere controlli di validazione per la rimozione,
        // andrebbero messi qui. Per ora, la traccia richiede solo di invocare rimuoviBrano().
        playlist.rimuoviBrano(brano);
    }

    @Override
    public void annulla() {
        // Il rollback della rimozione è reinserire il brano.
        playlist.aggiungiBrano(brano);
    }
}
