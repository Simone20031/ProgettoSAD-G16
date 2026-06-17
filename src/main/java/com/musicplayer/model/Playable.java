package com.musicplayer.model;

/**
 * Interfaccia per tutti gli elementi che possono essere riprodotti nel sistema.
 * Un Playable può essere un singolo brano (IBrano) o una collezione riproducibile (Playlist).
 */
public interface Playable {
    
    /**
     * Calcola la durata totale dell'elemento riproducibile.
     * @return Durata in secondi.
     */
    int getDurataTotale();
    
    /**
     * Recupera il numero di volte che l'elemento è stato ascoltato.
     * @return Il numero di riproduzioni.
     */
    int getPlayCount();
    
    /**
     * Incrementa il contatore di ascolto di una unità.
     */
    void incrementPlayCount();
}
