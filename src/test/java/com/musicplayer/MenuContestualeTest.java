package com.musicplayer;

import com.musicplayer.state.*;


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
    public void testStatoPlaylistOptions() {
        StatoPlaylist stato = new StatoPlaylist("Rock");
        MenuContestuale mc = new MenuContestuale(stato);
        List<String> opts = mc.getOpzioni();
        assertEquals(4, opts.size(), "StatoPlaylist deve fornire esattamente 4 opzioni");
        assertFalse(opts.contains("Elimina brano"), "Lo StatoPlaylist non deve contenere l'opzione 'Elimina brano'");
        assertTrue(opts.contains("Rimuovi da questa playlist"), "Lo StatoPlaylist deve contenere l'opzione 'Rimuovi da questa playlist'");
    }
}
