package com.musicplayer;

import java.util.Arrays;
import java.util.List;

public class StatoPlaylist implements StatoUI {
    
    private final String nomePlaylist;

    public StatoPlaylist(String nomePlaylist) {
        this.nomePlaylist = nomePlaylist;
    }

    @Override
    public List<String> getOpzioniSingolo() {
        return Arrays.asList("Rimuovi da questa playlist");
    }

    @Override
    public void eseguiOpzione(String opzione, Brano selezionato, LibreriaController controller, LibreriaView view) {
        if (opzione == null || selezionato == null || controller == null) return;
        
        if (opzione.equals("Rimuovi da questa playlist")) {
            try {
                // 🚀 Avvolgiamo la chiamata in un blocco try-catch per gestire l'eccezione controllata
                controller.rimuoviDaPlaylist(selezionato, nomePlaylist);
            } catch (ValidazioneException ve) {
                // Sfruttiamo il quarto parametro 'view' per mostrare il popup di errore a schermo
                view.mostraErrore(ve);
            } catch (Exception e) {
                view.mostraErrore(new ValidazioneException("Errore durante la rimozione: " + e.getMessage()));
            }
        }
    }
}