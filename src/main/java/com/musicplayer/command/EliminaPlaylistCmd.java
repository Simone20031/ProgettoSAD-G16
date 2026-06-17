package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.Playlist;

import com.musicplayer.model.ValidazioneException;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.controller.RiproduzioneException;
import com.musicplayer.model.PlaylistException;
import java.io.IOException;

public class EliminaPlaylistCmd implements Command {

    private final LibreriaController controller;
    private final String nomePlaylist;
    private Playlist playlistSalvata;

    public EliminaPlaylistCmd(LibreriaController controller, String nomePlaylist) {
        this.controller = controller;
        this.nomePlaylist = nomePlaylist;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException, PlaylistException, RiproduzioneException {
        if (controller.getPlaylistMap().containsKey(nomePlaylist)) {
            this.playlistSalvata = controller.getPlaylistMap().get(nomePlaylist);
            try {
                controller.eliminaPlaylist(nomePlaylist);
            } catch (IOException e) {
                throw new PersistenzaException("Errore durante l'eliminazione: " + e.getMessage());
            }
        } else {
            throw new ValidazioneException("La playlist specificata non esiste.");
        }
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException {
        if (playlistSalvata != null) {
            try {
                controller.ripristinaPlaylist(playlistSalvata);
            } catch (IOException e) {
                throw new PersistenzaException("Errore durante il ripristino: " + e.getMessage());
            }
        }
    }
}
