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
}
