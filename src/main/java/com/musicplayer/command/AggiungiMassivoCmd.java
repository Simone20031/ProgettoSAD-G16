package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.controller.RiproduzioneException;
import com.musicplayer.model.PlaylistException;
import com.musicplayer.model.Playable;
import com.musicplayer.model.ValidazioneException;
import com.musicplayer.persistence.PersistenzaException;
import java.util.Collection;

public class AggiungiMassivoCmd implements Command {
    private final LibreriaController controller;
    private final Collection<? extends Playable> brani;
    private final String playlistName;

    public AggiungiMassivoCmd(LibreriaController controller, Collection<? extends Playable> brani, String playlistName) {
        this.controller = controller;
        this.brani = brani;
        this.playlistName = playlistName;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException, PlaylistException {
        controller.aggiungiBraniAPlaylist(brani, playlistName);
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException {
        controller.rimuoviDaPlaylist(brani, playlistName);
    }
}
