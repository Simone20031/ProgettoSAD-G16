package com.musicplayer.command;

import com.musicplayer.model.*;
import com.musicplayer.controller.LibreriaController;
import com.musicplayer.controller.RiproduzioneException;
import com.musicplayer.persistence.PersistenzaException;

public class AggiungiAPlaylistCmd implements Command {
    private final LibreriaController controller;
    private final Brano brano;
    private final String playlistName;

    public AggiungiAPlaylistCmd(LibreriaController controller, Brano brano, String playlistName) {
        this.controller = controller;
        this.brano = brano;
        this.playlistName = playlistName;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException, PlaylistException {
        controller.aggiungiAPlaylist(brano, playlistName);
    }

    @Override
    public void annulla() throws ValidazioneException, RiproduzioneException, PersistenzaException, PlaylistException {
        controller.rimuoviDaPlaylist(brano, playlistName);
    }
}
