package com.musicplayer;

import java.util.List;
import java.io.IOException;
import java.util.Map;

/**
 * StatoLibreria: stato del contesto "Libreria" del pattern State.
 * Implementazione minimale per fornire le opzioni contestuali per un singolo brano.
 */
public class StatoLibreria implements Stato {

    /**
     * Restituisce le opzioni contestuali disponibili per una singola traccia.
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
    public void eseguiOpzione(String op, Brano brano, LibreriaController controller) {
        if (op == null || controller == null) return;
        try {
            switch (op) {
                case "Aggiungi tag" -> {
                    // Qui potremmo aprire una UI ma per ora log/placeholder
                    System.out.println("Richiesta aggiunta tag per: " + (brano == null ? "<null>" : brano.getTitolo()));
                }
                case "Modifica" -> controller.modificaBrano(brano, Map.of());
                case "Elimina brano" -> controller.eliminaBrano(brano);
                case "Aggiungi a playlist" -> controller.aggiungiAPlaylist(brano, "default");
                default -> System.out.println("Opzione non gestita: " + op);
            }
        } catch (IOException | ValidazioneException ex) {
            throw new RuntimeException("Errore esecuzione opzione: " + ex.getMessage(), ex);
        }
    }
}
