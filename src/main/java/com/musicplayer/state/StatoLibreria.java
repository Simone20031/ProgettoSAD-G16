package com.musicplayer.state;


import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import com.musicplayer.view.*;


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
        return List.of("Aggiungi ai preferiti", "Modifica", "Elimina brano", "Aggiungi a playlist");
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
            case "Aggiungi ai preferiti", "Togli dai preferiti" -> {
                if (view != null) {
                    try {
                        String fn = com.musicplayer.PathUtils.filenameFromPath(brano.getPercorsoFile());
                        com.musicplayer.persistence.SongMetadata m = view.getMetadataMap().get(fn);
                        String tagCorrente = (m != null && m.tag != null) ? m.tag : (brano.getTag() != null && brano.getTag() != Tag.NESSUNO ? brano.getTag().getEtichetta() : "");
                        
                        String tagAggiornato;
                        if (tagCorrente.contains("Preferiti")) {
                            tagAggiornato = java.util.Arrays.stream(tagCorrente.split(","))
                                    .map(String::trim)
                                    .filter(t -> !t.equals("Preferiti"))
                                    .collect(java.util.stream.Collectors.joining(", "));
                            if (tagAggiornato.isEmpty()) tagAggiornato = "NESSUNO";
                        } else {
                            tagAggiornato = tagCorrente.isEmpty() || tagCorrente.equals("NESSUNO") ? "Preferiti" : tagCorrente + ", Preferiti";
                        }
                        controller.modificaTagBrano(brano, tagAggiornato);
                        
                        // Per aggiornare il file CSV con il nuovo tag, simuliamo il reload della mappa
                        com.musicplayer.persistence.MetadataService.caricaMappaDalCSV(view.getMetadataMap());
                        
                        view.refreshList();
                        view.aggiornaCuorePreferiti();
                    } catch (ValidazioneException e) {
                        view.mostraErrore(e);
                    }
                }
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
