package com.musicplayer.model;



import java.util.Map;

/**
 * Interfaccia comune per Brano e BranoProxy.
 * Definisce il contratto minimo per qualsiasi oggetto-brano nel sistema.
 */
public interface IBrano extends Playable {
    Map<String, String> getDettagli();

    int getDurata();

    String getTitolo();
}
