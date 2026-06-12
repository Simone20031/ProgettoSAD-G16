package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.Brano;
import com.musicplayer.model.ValidazioneException;
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

    public RimuoviDaLibreriaCmd(LibreriaController controller, Brano brano) {
        this.controller = controller;
        this.brano = brano;
    }

    @Override
    public void esegui() throws ValidazioneException {
        try {
            // Salva una copia di backup prima di rimuoverlo
            String filename = PathUtils.filenameFromPath(brano.getPercorsoFile());
            Path originalPath = Path.of(System.getProperty("user.dir"), "Libreria", filename);
            if (Files.exists(originalPath)) {
                Path tempDir = Files.createTempDirectory("musicplayer_backup");
                tempDir.toFile().deleteOnExit();
                backupFile = tempDir.resolve(filename).toFile();
                Files.copy(originalPath, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backupFile.deleteOnExit();
            }

            controller.eliminaBrano(brano);
        } catch (IOException e) {
            throw new ValidazioneException("Errore IO durante la rimozione del brano: " + e.getMessage());
        }
    }

    @Override
    public void annulla() throws ValidazioneException {
        try {
            if (backupFile != null && backupFile.exists()) {
                controller.aggiungiBrano(backupFile, brano);
            } else {
                throw new ValidazioneException("Impossibile ripristinare il file: backup non trovato.");
            }
        } catch (IOException e) {
            throw new ValidazioneException("Errore IO durante l'annullamento della rimozione del brano: " + e.getMessage());
        }
    }
}
