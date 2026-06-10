package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.command.*;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RimuoviDaPlaylistTest {

    @Test
    public void testRimozioneBranoPresente() throws Exception {
        Playlist p = new Playlist("id1", "Rock Classico");
        Brano b = new Brano("Bohemian Rhapsody", "path1", "Queen", "Rock", 354, "1975", 1, null);
        
        // Setup: aggiungo il brano
        p.aggiungiBrano(b);
        assertEquals(1, p.getBrani().size());
        
        // Eseguo il comando di rimozione
        RimuoviDaPlaylistCmd cmd = new RimuoviDaPlaylistCmd(p, b);
        cmd.esegui();
        
        // Verifico la rimozione
        assertEquals(0, p.getBrani().size());
        assertFalse(p.contieneBrano(b));
    }

    @Test
    public void testRimozioneBranoAssenteDaPlaylist() throws Exception {
        Playlist p = new Playlist("id2", "Pop");
        Brano inPlaylist = new Brano("Billie Jean", "path1", "Michael Jackson", "Pop", 294, "1982", 1, null);
        Brano fuoriPlaylist = new Brano("Thriller", "path2", "Michael Jackson", "Pop", 357, "1982", 1, null);
        
        // Setup: aggiungo solo un brano
        p.aggiungiBrano(inPlaylist);
        assertEquals(1, p.getBrani().size());
        
        // Eseguo il comando di rimozione su un brano non presente nella playlist
        RimuoviDaPlaylistCmd cmd = new RimuoviDaPlaylistCmd(p, fuoriPlaylist);
        cmd.esegui();
        
        // Verifico che la dimensione sia rimasta invariata e il primo brano sia ancora presente
        assertEquals(1, p.getBrani().size());
        assertFalse(p.contieneBrano(fuoriPlaylist));
    }
}
