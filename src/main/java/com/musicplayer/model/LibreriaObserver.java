package com.musicplayer.model;



public interface LibreriaObserver {
    void onBranoAggiunto(IBrano brano);
    void onBranoEliminato(IBrano brano);
    void onPlaylistAggiornata(Playlist playlist);
}
