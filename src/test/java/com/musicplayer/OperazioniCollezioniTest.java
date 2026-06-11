package com.musicplayer;

import com.musicplayer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OperazioniCollezioniTest {

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testAggiungiBrani_Libreria_InserimentoCollezioneValida() {
        Libreria libreria = Libreria.getInstance();
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        Brano b2 = new Brano("2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);

        libreria.aggiungiBrani(Arrays.asList(b1, b2));

        List<IBrano> brani = libreria.getBrani();
        assertEquals(2, brani.size(), "La libreria dovrebbe contenere 2 brani.");
        assertTrue(brani.contains(b1));
        assertTrue(brani.contains(b2));
    }

    @Test
    public void testAggiungiBrani_Libreria_IgnoraDuplicati() {
        Libreria libreria = Libreria.getInstance();
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        libreria.aggiungiBrano(b1);

        // Aggiunge lo stesso brano e uno nuovo
        Brano b2 = new Brano("2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);
        libreria.aggiungiBrani(Arrays.asList(b1, b2));

        List<IBrano> brani = libreria.getBrani();
        assertEquals(2, brani.size(), "La libreria non dovrebbe contenere duplicati.");
        assertTrue(brani.contains(b1));
        assertTrue(brani.contains(b2));
    }

    @Test
    public void testEliminaBrani_Libreria_RimozioneCollezione() {
        Libreria libreria = Libreria.getInstance();
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        Brano b2 = new Brano("2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);
        libreria.aggiungiBrano(b1);
        libreria.aggiungiBrano(b2);

        libreria.eliminaBrani(Collections.singletonList(b1));

        List<IBrano> brani = libreria.getBrani();
        assertEquals(1, brani.size());
        assertFalse(brani.contains(b1));
        assertTrue(brani.contains(b2));
    }

    @Test
    public void testAggiungiBrani_Playlist_InserimentoEfficace() {
        Playlist playlist = new Playlist("p1", "My Playlist");
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        Brano b2 = new Brano("2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);

        playlist.aggiungiBrani(Arrays.asList(b1, b2));

        List<IBrano> brani = playlist.getBrani();
        assertEquals(2, brani.size());
        assertTrue(playlist.contieneBrano(b1));
        assertTrue(playlist.contieneBrano(b2));
    }

    @Test
    public void testAggiungiBrani_Playlist_LanciaEccezioneSeDuplicato() {
        Playlist playlist = new Playlist("p1", "My Playlist");
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        playlist.aggiungiBrano(b1);

        assertThrows(IllegalArgumentException.class, () -> {
            playlist.aggiungiBrani(Collections.singletonList(b1));
        }, "Dovrebbe lanciare IllegalArgumentException se il brano è già presente.");
    }

    @Test
    public void testRimuoviBrani_Playlist_RimozioneCollezione() {
        Playlist playlist = new Playlist("p1", "My Playlist");
        Brano b1 = new Brano("1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 120, Tag.NESSUNO);
        Brano b2 = new Brano("2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);
        playlist.aggiungiBrano(b1);
        playlist.aggiungiBrano(b2);

        playlist.rimuoviBrani(Collections.singletonList(b1));

        List<IBrano> brani = playlist.getBrani();
        assertEquals(1, brani.size());
        assertFalse(playlist.contieneBrano(b1));
        assertTrue(playlist.contieneBrano(b2));
    }
}
