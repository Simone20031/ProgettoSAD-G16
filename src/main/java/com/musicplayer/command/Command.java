package com.musicplayer.command;

import com.musicplayer.model.*;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.controller.RiproduzioneException;


public interface Command {
    void esegui() throws ValidazioneException, PersistenzaException, PlaylistException, RiproduzioneException;
    void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException;
}
