package com.musicplayer.model;



import java.util.Map;

/**
 * Interfaccia comune per Brano e BranoProxy.
 * Definisce il contratto minimo per qualsiasi oggetto-brano nel sistema.
 */
public interface IBrano extends Playable {
    /**
     * Recupera una mappa di attributi del brano.
     * @return Una Map contenente i dettagli testuali (es. titolo, autore, genere, etc.).
     */
    Map<String, String> getDettagli();

    /**
     * Recupera la durata del brano in secondi.
     * @return La durata espressa in secondi.
     */
    int getDurata();

    /**
     * Recupera il titolo del brano.
     * @return Il titolo del brano musicale come stringa.
     */
    String getTitolo();
}
