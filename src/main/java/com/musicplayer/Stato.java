package com.musicplayer;

import java.util.List;

/**
 * Interfaccia comune per gli stati del pattern State usati nell'app.
 */
public interface Stato {
    List<String> getOpzioniSingolo();
}
