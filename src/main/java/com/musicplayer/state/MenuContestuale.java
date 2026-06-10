package com.musicplayer.state;

import com.musicplayer.model.*;


import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Helper per mostrare menu contestuali basati sullo stato corrente.
 */
public class MenuContestuale {

    private StatoUI statoCorrente;

    public MenuContestuale(StatoUI statoCorrente) {
        this.statoCorrente = statoCorrente;
    }

    /**
     * Aggiorna lo stato corrente usato per costruire le opzioni del menu.
     * @param nuovoStato nuovo stato da utilizzare
     */
    public void setStato(StatoUI nuovoStato) {
        this.statoCorrente = nuovoStato;
    }

    /**
     * Ottiene le opzioni dallo stato corrente e apre un ContextMenu
     * ancorato al nodo passato (il pulsante con i tre puntini).
     * @param brano elemento selezionato (può essere null se non necessario)
     * @param anchor nodo su cui visualizzare il menu (es. il bottone tre puntini)
     * @param runnableAzione callback per notificare la View dell'azione selezionata
     */
    public void apriMenuSingolo(IBrano brano, Node anchor, java.util.function.Consumer<String> runnableAzione) {
        ContextMenu cm = new ContextMenu();
        
        for (String label : statoCorrente.getOpzioniSingolo()) {
            MenuItem mi = new MenuItem(label);
            
            // Quando clicchi, notifica la View 
            mi.setOnAction(e -> {
                if (runnableAzione != null) {
                    runnableAzione.accept(label);
                }
            });
            cm.getItems().add(mi);
        }
        
        // Mostra il menu esattamente sotto il pulsante dei tre puntini
        cm.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    /**
     * Restituisce le etichette che verrebbero mostrate dal menu.
     * Metodo di utilità per test senza aprire l'interfaccia grafica.
     */
    public java.util.List<String> getOpzioni() {
        return statoCorrente == null ? java.util.List.of() : statoCorrente.getOpzioniSingolo();
    }
}