package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.Playable;
import com.musicplayer.model.ValidazioneException;
import java.util.Collection;

public class RimuoviMassivoCmd implements Command {
    private final LibreriaController controller;
    private final Collection<? extends Playable> brani;
    private final String playlistName;

    public RimuoviMassivoCmd(LibreriaController controller, Collection<? extends Playable> brani, String playlistName) {
        this.controller = controller;
        this.brani = brani;
        this.playlistName = playlistName;
    }

    @Override
    public void esegui() throws ValidazioneException {
        controller.rimuoviDaPlaylist(brani, playlistName);
    }

    @Override
    public void annulla() throws ValidazioneException {
        controller.aggiungiBraniAPlaylist(brani, playlistName);
    }
}
