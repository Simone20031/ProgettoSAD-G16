package com.musicplayer.command;

import com.musicplayer.model.*;


public interface Command {
    void esegui() throws ValidazioneException;
    void annulla() throws ValidazioneException;
}
