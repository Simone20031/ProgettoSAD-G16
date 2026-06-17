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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RimuoviMassivoLibreriaCmd implements Command {
    private final LibreriaController controller;
    private final Collection<Brano> brani;
    private final List<File> backupFiles = new ArrayList<>();
    private final java.util.Map<Brano, java.util.Map<String, Integer>> playlistAssociations = new java.util.HashMap<>();

    public RimuoviMassivoLibreriaCmd(LibreriaController controller, Collection<Brano> brani) {
        this.controller = controller;
        this.brani = brani;
    }

    @Override
    public void esegui() throws ValidazioneException, PersistenzaException {
        try {
            // Salva le associazioni con le playlist per tutti i brani prima di eliminare
            java.util.Map<String, com.musicplayer.model.Playlist> playlistMap = controller.getPlaylistMap();
            if (playlistMap != null) {
                for (Brano b : brani) {
                    java.util.Map<String, Integer> bMap = new java.util.HashMap<>();
                    for (com.musicplayer.model.Playlist p : playlistMap.values()) {
                        int idx = p.getBrani().indexOf(b);
                        if (idx >= 0) {
                            bMap.put(p.getNome(), idx);
                        }
                    }
                    playlistAssociations.put(b, bMap);
                }
            }

            for (Brano b : brani) {
                String filename = PathUtils.filenameFromPath(b.getPercorsoFile());
                Path originalPath = Path.of(System.getProperty("user.dir"), com.musicplayer.PathUtils.getLibraryPath(), filename);
                if (Files.exists(originalPath)) {
                    Path tempDir = Files.createTempDirectory("musicplayer_backup");
                    tempDir.toFile().deleteOnExit();
                    File backupFile = tempDir.resolve(filename).toFile();
                    Files.copy(originalPath, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    backupFile.deleteOnExit();
                    backupFiles.add(backupFile);
                } else {
                    backupFiles.add(null);
                }
            }

            controller.eliminaBrani(new ArrayList<>(brani));
        } catch (IOException e) {
            throw new PersistenzaException("Errore IO durante la rimozione massiva: " + e.getMessage(),
                    PersistenzaException.TipoPersistenza.ERRORE_SCRITTURA, e);
        }
    }

    @Override
    public void annulla() throws ValidazioneException, PersistenzaException, com.musicplayer.model.PlaylistException {
        try {
            int i = 0;
            for (Brano b : brani) {
                File backupFile = backupFiles.get(i);
                if (backupFile != null && backupFile.exists()) {
                    controller.aggiungiBrano(backupFile, b);
                    
                    // Ripristina le associazioni per il brano b
                    java.util.Map<String, Integer> bMap = playlistAssociations.get(b);
                    if (bMap != null) {
                        for (java.util.Map.Entry<String, Integer> entry : bMap.entrySet()) {
                            controller.aggiungiAPlaylist(b, entry.getKey(), entry.getValue());
                        }
                    }
                }
                i++;
            }
        } catch (IOException e) {
            throw new PersistenzaException("Errore IO durante l'annullamento della rimozione massiva: " + e.getMessage(),
                    PersistenzaException.TipoPersistenza.ERRORE_SCRITTURA, e);
        }
    }
}
