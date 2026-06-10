package com.musicplayer.model;



public interface RiproduzioneObserver {
    void onPlayerReady(int durataSecondi);
    void onPlay();
    void onPausa();
    void onStop();
    void onProgressoAggiornato(int secondi);
    void onBranoCambiato(String nuovoPercorso);
}
