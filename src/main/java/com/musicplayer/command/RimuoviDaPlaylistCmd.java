package com.musicplayer.command;

import com.musicplayer.model.*;
import com.musicplayer.controller.LibreriaController;

public class RimuoviDaPlaylistCmd implements Command {
    private final LibreriaController controller;
    private final Brano brano;
    private final String playlistName;
    private int originalIndex = -1;

    public RimuoviDaPlaylistCmd(LibreriaController controller, Brano brano, String playlistName) {
        this.controller = controller;
        this.brano = brano;
        this.playlistName = playlistName;
    }

    @Override
    public void esegui() throws ValidazioneException {
        java.util.Map<String, Playlist> playlistMap = controller.getPlaylistMap();
        Playlist pl = playlistMap != null ? playlistMap.get(playlistName) : null;
        if (pl != null) {
            originalIndex = pl.getBrani().indexOf(brano);
        }
        controller.rimuoviDaPlaylist(brano, playlistName);
    }

    @Override
    public void annulla() throws ValidazioneException {
        if (originalIndex >= 0) {
            controller.aggiungiAPlaylist(brano, playlistName, originalIndex);
        } else {
            controller.aggiungiAPlaylist(brano, playlistName);
        }
    }
}
