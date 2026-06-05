package com.musicplayer;

import java.util.Map;

/**
 * Interfaccia comune per Brano e BranoProxy.
 * Definisce il contratto minimo per qualsiasi oggetto-brano nel sistema.
 */
public interface IBrano {
    Map<String, String> getDettagli();

    int getDurata();

    String getTitolo();
}
