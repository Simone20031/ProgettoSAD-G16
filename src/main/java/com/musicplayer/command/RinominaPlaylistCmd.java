package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;

import com.musicplayer.model.ValidazioneException;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.controller.RiproduzioneException;
import com.musicplayer.model.PlaylistException;
import java.io.IOException;

public class RinominaPlaylistCmd implements Command {

    private final LibreriaController controller;
    private final String vecchioNome;
    private final String nuovoNome;

    public RinominaPlaylistCmd(LibreriaController controller, String vecchioNome, String nuovoNome) {
        this.controller = controller;
        this.vecchioNome = vecchioNome;
        this.nuovoNome = nuovoNome;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException, PlaylistException, RiproduzioneException {
        try {
            controller.rinominaPlaylist(vecchioNome, nuovoNome);
        } catch (IOException e) {
            throw new PersistenzaException("Errore durante la rinomina: " + e.getMessage());
        }
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException {
        try {
            controller.rinominaPlaylist(nuovoNome, vecchioNome);
        } catch (IOException e) {
            throw new PersistenzaException("Errore durante l'annullamento della rinomina: " + e.getMessage());
        }
    }
}
