package com.musicplayer;

import java.util.List;

/**
 * StatoLibreria: stato del contesto "Libreria" del pattern State.
 * Implementazione minimale per fornire le opzioni contestuali per un singolo
 * brano.
 */
public class StatoLibreria implements StatoUI {

    /**
     * Restituisce le opzioni contestuali disponibili per una singola traccia.
     * 
     * @return lista di etichette delle opzioni
     */
    @Override
    public List<String> getOpzioniSingolo() {
        return List.of("Aggiungi tag", "Modifica", "Elimina brano", "Aggiungi a playlist");
    }

    /**
     * Smista l'opzione scelta verso il controller appropriato.
     * Nota: alcune azioni potrebbero richiedere interazione con la View;
     * qui si delega al controller per la logica di backend possibile.
     */
    @Override
    public void eseguiOpzione(String op, Brano brano, LibreriaController controller, LibreriaView view) {
        if (op == null || controller == null)
            return;
        switch (op) {
            case "Aggiungi tag" -> {
                // Placeholder
            }
            case "Modifica" -> {
                if (view != null) {
                    view.editBrano(brano);
                }
            }
            case "Elimina brano" -> {
                if (view != null) {
                    view.deleteBrano(brano);
                }
            }
            // Dentro StatoLibreria.java, nel case "Aggiungi a playlist"
            case "Aggiungi a playlist" -> {
                if (view != null) {
                    view.apriSelezionePlaylist(brano); // Chiamata al nuovo metodo creato sopra
                }
            }
            default -> {
            }
        }
    }
}
