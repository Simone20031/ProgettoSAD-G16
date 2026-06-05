package com.musicplayer;

import java.util.List;

public interface StatoUI {
    List<String> getOpzioniSingolo();

    // Ora accetta anche LibreriaView come parametro
    void eseguiOpzione(String opzione, Brano selezionato, LibreriaController controller, LibreriaView view);
}