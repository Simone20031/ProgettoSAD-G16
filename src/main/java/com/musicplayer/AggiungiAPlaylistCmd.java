package com.musicplayer;

public class AggiungiAPlaylistCmd implements Comando {
    private final Playlist playlist;
    private final IBrano brano;

    public AggiungiAPlaylistCmd(Playlist playlist, IBrano brano) {
        this.playlist = playlist;
        this.brano = brano;
    }

    @Override
    public void esegui() throws ValidazioneException {
        playlist.aggiungiBrano(brano);
    }

    @Override
    public void annulla() {
        playlist.rimuoviBrano(brano);
    }
}
