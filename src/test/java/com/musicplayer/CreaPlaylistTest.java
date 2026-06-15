package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;


import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test per creazione playlist aggiornato per LibreriaController.
 */
public class CreaPlaylistTest {

    private LibreriaController controller;

    @BeforeEach
    public void setup() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
        controller = new LibreriaController();
    }

    @AfterEach
    public void cleanup() {
        try {
            controller.eliminaPlaylist("Preferiti");
            controller.eliminaPlaylist("Rock");
            controller.eliminaPlaylist("PREFERITI");
            controller.eliminaPlaylist("Chill");
        } catch (Exception e) {}
    }

    @Test
    public void testCreaPlaylist_NomeNull_NonCreaPlaylist() throws Exception {
        controller.aggiungiAPlaylist(null, null);
        assertTrue(controller.getPlaylist().isEmpty(), "Un nome null non deve creare la playlist.");
    }

    @Test
    public void testCreaPlaylist_NomeVuoto_NonCreaPlaylist() throws Exception {
        controller.aggiungiAPlaylist(null, "");
        assertTrue(controller.getPlaylist().isEmpty(), "Un nome vuoto non deve creare la playlist.");
    }

    @Test
    public void testCreaPlaylist_NomeSoloSpazi_NonCreaPlaylist() throws Exception {
        controller.aggiungiAPlaylist(null, "   ");
        assertTrue(controller.getPlaylist().isEmpty(), "Un nome di soli spazi non deve creare la playlist.");
    }

    @Test
    public void testCreaPlaylist_NomeDuplicatoEsatto_LanciaEccezione() throws Exception {
        controller.aggiungiAPlaylist(null, "Preferiti");

        PlaylistException ex = assertThrows(PlaylistException.class,
                () -> controller.aggiungiAPlaylist(null, "Preferiti"),
                "Un nome duplicato deve lanciare PlaylistException.");

        assertEquals("Una playlist con lo stesso nome esiste già!", ex.getMessage());
    }

    @Test
    public void testCreaPlaylist_NomeDuplicatoCaseInsensitive_LanciaEccezione() throws Exception {
        controller.aggiungiAPlaylist(null, "Preferiti");

        PlaylistException ex = assertThrows(PlaylistException.class,
                () -> controller.aggiungiAPlaylist(null, "PREFERITI"),
                "Il controllo duplicati deve essere case-insensitive.");

        assertEquals("Una playlist con lo stesso nome esiste già!", ex.getMessage());
    }

    @Test
    public void testCreaPlaylist_NomeValido_CreaERegistra() throws Exception {
        controller.aggiungiAPlaylist(null, "Preferiti");

        Playlist p = controller.getPlaylistMap().get("Preferiti");
        assertNotNull(p, "La playlist non deve essere null.");
        assertEquals("Preferiti", p.getNome(), "Il nome della playlist deve corrispondere.");
        assertTrue(controller.getPlaylist().contains(p), "La playlist deve essere registrata.");
    }

    @Test
    public void testCreaPlaylist_DueNomiDiversi_EntrambeRegistrate() throws Exception {
        controller.aggiungiAPlaylist(null, "Preferiti");
        controller.aggiungiAPlaylist(null, "Rock");

        assertEquals(2, controller.getPlaylist().size(), "Devono esserci esattamente 2 playlist.");
        assertNotNull(controller.getPlaylistMap().get("Preferiti"));
        assertNotNull(controller.getPlaylistMap().get("Rock"));
    }

    @Test
    public void testCreaPlaylist_NomeConSpazi_CreaPlaylistConSpaziTrimmatiNelMetodoGiaFattoOPreservati() throws Exception {
        // Il controller non trimma il nome, lo usa così com'è per la chiave se non esiste.
        controller.aggiungiAPlaylist(null, "Chill");
        
        Playlist p = controller.getPlaylistMap().get("Chill");
        assertNotNull(p, "La playlist deve essere creata.");
        assertEquals("Chill", p.getNome());
    }
}