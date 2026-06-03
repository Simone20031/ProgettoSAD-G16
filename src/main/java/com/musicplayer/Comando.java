package com.musicplayer;

public interface Comando {
    void esegui() throws ValidazioneException;
    void annulla();
}
