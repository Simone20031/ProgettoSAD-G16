package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.command.*;
import com.musicplayer.controller.LibreriaController;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RimuoviDaPlaylistTest {

    @org.junit.jupiter.api.AfterEach
    public void cleanup() {
        try {
            LibreriaController c = new LibreriaController();
            c.eliminaPlaylist("Rock Classico");
            c.eliminaPlaylist("Pop");
            
            // Hard physical cleanup just in case Controller fails to clean it up
            java.io.File dir1 = new java.io.File(com.musicplayer.PathUtils.getLibraryPath() + "/Playlist - Rock Classico");
            if (dir1.exists()) {
                for (java.io.File f : dir1.listFiles()) f.delete();
                dir1.delete();
            }
            java.io.File dir2 = new java.io.File(com.musicplayer.PathUtils.getLibraryPath() + "/Playlist - Pop");
            if (dir2.exists()) {
                for (java.io.File f : dir2.listFiles()) f.delete();
                dir2.delete();
            }
        } catch (Exception e) {}
    }

    @Test
    public void testRimozioneBranoPresente() throws Exception {
        LibreriaController controller = new LibreriaController();
        Playlist p = new Playlist("id1", "Rock Classico");
        controller.getPlaylistMap().put("Rock Classico", p);
        Brano b = new Brano("Bohemian Rhapsody", "path1", "Queen", "Rock", 354, "1975", 1, null);
        
        // Setup: aggiungo il brano
        p.aggiungiBrano(b);
        assertEquals(1, p.getBrani().size());
        
        // Eseguo il comando di rimozione
        RimuoviDaPlaylistCmd cmd = new RimuoviDaPlaylistCmd(controller, b, "Rock Classico");
        cmd.esegui();
        
        // Verifico la rimozione
        assertEquals(0, p.getBrani().size());
        assertFalse(p.contieneBrano(b));
    }

    @Test
    public void testRimozioneBranoAssenteDaPlaylist() throws Exception {
        LibreriaController controller = new LibreriaController();
        Playlist p = new Playlist("id2", "Pop");
        controller.getPlaylistMap().put("Pop", p);
        Brano inPlaylist = new Brano("Billie Jean", "path1", "Michael Jackson", "Pop", 294, "1982", 1, null);
        Brano fuoriPlaylist = new Brano("Thriller", "path2", "Michael Jackson", "Pop", 357, "1982", 1, null);
        
        // Setup: aggiungo solo un brano
        p.aggiungiBrano(inPlaylist);
        assertEquals(1, p.getBrani().size());
        
        // Eseguo il comando di rimozione su un brano non presente nella playlist
        RimuoviDaPlaylistCmd cmd = new RimuoviDaPlaylistCmd(controller, fuoriPlaylist, "Pop");
        cmd.esegui();
        
        // Verifico che la dimensione sia rimasta invariata e il primo brano sia ancora presente
        assertEquals(1, p.getBrani().size());
        assertFalse(p.contieneBrano(fuoriPlaylist));
    }
}
