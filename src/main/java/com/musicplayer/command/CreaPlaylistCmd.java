package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.PlaylistException;
import java.io.IOException;

import com.musicplayer.model.ValidazioneException;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.controller.RiproduzioneException;

public class CreaPlaylistCmd implements Command {

    private final LibreriaController controller;
    private final String nomePlaylist;

    public CreaPlaylistCmd(LibreriaController controller, String nomePlaylist) {
        this.controller = controller;
        this.nomePlaylist = nomePlaylist;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException, PlaylistException, RiproduzioneException {
        controller.aggiungiAPlaylist(null, nomePlaylist);
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException {
        try {
            controller.eliminaPlaylist(nomePlaylist);
        } catch (IOException e) {
            throw new PersistenzaException("Errore annullamento: " + e.getMessage());
        }
    }
}
