package com.musicplayer.command;

import com.musicplayer.controller.LibreriaController;
import com.musicplayer.model.Brano;
import com.musicplayer.model.ValidazioneException;
import java.io.IOException;

public class AggiungiALibreriaCmd implements Command {
    private final LibreriaController controller;
    private final String titolo;
    private final String autore;
    private final String genere;
    private final int anno;
    private final String percorsoFile;
    private final int durataSec;
    private final String tagRaw;
    private Brano branoAggiunto;

    public AggiungiALibreriaCmd(LibreriaController controller, String titolo, String autore, String genere, int anno, String percorsoFile, int durataSec, String tagRaw) {
        this.controller = controller;
        this.titolo = titolo;
        this.autore = autore;
        this.genere = genere;
        this.anno = anno;
        this.percorsoFile = percorsoFile;
        this.durataSec = durataSec;
        this.tagRaw = tagRaw;
    }

    @Override
    public void esegui() throws ValidazioneException {
        try {
            controller.aggiungiBrano(titolo, autore, genere, anno, percorsoFile, durataSec, tagRaw);
            
            // Per poterlo annullare, ci serve il brano appena aggiunto.
            // Dato che il controller lo aggiunge in fondo o comunque possiamo trovarlo tramite il nome file
            String filename = com.musicplayer.PathUtils.filenameFromPath(percorsoFile);
            branoAggiunto = controller.trovaBranoDaNome(filename);
            
        } catch (IOException e) {
            throw new ValidazioneException("Errore IO durante l'aggiunta del brano: " + e.getMessage());
        }
    }

    @Override
    public void annulla() throws ValidazioneException {
        try {
            if (branoAggiunto != null) {
                controller.eliminaBrano(branoAggiunto);
            }
        } catch (IOException e) {
            throw new ValidazioneException("Errore IO durante l'annullamento dell'aggiunta del brano: " + e.getMessage());
        }
    }
}
