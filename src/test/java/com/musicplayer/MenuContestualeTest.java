package com.musicplayer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class MenuContestualeTest {

    @Test
    public void testStatoLibreriaOptionsCount() {
        StatoLibreria stato = new StatoLibreria();
        MenuContestuale mc = new MenuContestuale(stato);
        List<String> opts = mc.getOpzioni();
        assertEquals(4, opts.size(), "StatoLibreria deve fornire esattamente 4 opzioni");
    }

    @Test
    public void testStatoPlaylistOptionsContainsRimuovi() {
        Playlist p = new Playlist("Test Playlist");
        StatoPlaylist stato = new StatoPlaylist(p);
        MenuContestuale mc = new MenuContestuale(stato);
        List<String> opts = mc.getOpzioni();
        
        assertTrue(opts.contains("Rimuovi da questa playlist"), "StatoPlaylist deve includere l'opzione di rimozione");
    }
}
