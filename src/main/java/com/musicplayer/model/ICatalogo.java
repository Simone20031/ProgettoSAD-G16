package com.musicplayer.model;

import java.util.List;

/**
 * Interfaccia che rappresenta un catalogo contenente una collezione di brani musicali.
 * Permette l'astrazione sulle collezioni (es. l'intera Libreria o singole Playlist).
 */
public interface ICatalogo {
    
    /**
     * Recupera tutti i brani attualmente contenuti in questo catalogo.
     * @return Una lista di oggetti IBrano.
     */
    List<IBrano> getBrani();
}
