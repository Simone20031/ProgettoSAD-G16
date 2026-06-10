package com.musicplayer.strategy;



import java.util.List;

public interface PlaybackStrategy {
    /**
     * Calcola il prossimo indice da riprodurre.
     * Restituisce -1 se non ci sono ulteriori brani da riprodurre.
     *
     * @param corrente indice del brano corrente
     * @param totale dimensione della playlist
     * @param riprodotti lista di indici già riprodotti
     * @return il prossimo indice, oppure -1
     */
    int prossimoIndice(int corrente, int totale, List<Integer> riprodotti);
    int indicePrecedente(int corrente, int totale, List<Integer> riprodotti);
}
