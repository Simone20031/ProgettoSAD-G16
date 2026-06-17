package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.Brano;
import com.musicplayer.model.ValidazioneException;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RimuoviDaLibreriaCmd implements Command {
    private final LibreriaController controller;
    private final Brano brano;
    private File backupFile;
    private final java.util.Map<String, Integer> playlistIndexMap = new java.util.HashMap<>();

    public RimuoviDaLibreriaCmd(LibreriaController controller, Brano brano) {
        this.controller = controller;
        this.brano = brano;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException {
        try {
            // Salva le associazioni con le playlist prima di eliminare
            java.util.Map<String, com.musicplayer.model.Playlist> playlistMap = controller.getPlaylistMap();
            if (playlistMap != null) {
                for (com.musicplayer.model.Playlist p : playlistMap.values()) {
                    int idx = p.getBrani().indexOf(brano);
                    if (idx >= 0) {
                        playlistIndexMap.put(p.getNome(), idx);
                    }
                }
            }

            // Salva una copia di backup prima di rimuoverlo
            String filename = PathUtils.filenameFromPath(brano.getPercorsoFile());
            Path originalPath = Path.of(System.getProperty("user.dir"), com.musicplayer.PathUtils.getLibraryPath(), filename);
            if (Files.exists(originalPath)) {
                Path tempDir = Files.createTempDirectory("musicplayer_backup");
                tempDir.toFile().deleteOnExit();
                backupFile = tempDir.resolve(filename).toFile();
                Files.copy(originalPath, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backupFile.deleteOnExit();
            }

            controller.eliminaBrano(brano);
        } catch (IOException e) {
            throw new PersistenzaException("Errore IO durante la rimozione del brano: " + e.getMessage(),
                    PersistenzaException.TipoPersistenza.ERRORE_SCRITTURA, e);
        }
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, com.musicplayer.model.PlaylistException {
        try {
            if (backupFile != null && backupFile.exists()) {
                controller.aggiungiBrano(backupFile, brano);
                
                // Ripristina le associazioni alle playlist nelle posizioni originali
                for (java.util.Map.Entry<String, Integer> entry : playlistIndexMap.entrySet()) {
                    controller.aggiungiAPlaylist(brano, entry.getKey(), entry.getValue());
                }
            } else {
                throw new PersistenzaException("Impossibile ripristinare il file: backup non trovato.",
                        PersistenzaException.TipoPersistenza.BACKUP_NON_TROVATO);
            }
        } catch (IOException e) {
            throw new PersistenzaException("Errore IO durante l'annullamento della rimozione del brano: " + e.getMessage(),
                    PersistenzaException.TipoPersistenza.ERRORE_SCRITTURA, e);
        }
    }
}
