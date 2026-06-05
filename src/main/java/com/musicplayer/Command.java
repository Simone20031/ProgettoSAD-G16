package com.musicplayer;

public interface Command {
    void esegui() throws ValidazioneException;
    void annulla() throws ValidazioneException;
}
