package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EliminaPlaylistTest {

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
            controller.eliminaPlaylist("Rock");
            controller.eliminaPlaylist("Pop");
        } catch (Exception e) {}
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEliminazioneConfermata_RimuoveDaPlaylistEMantieneBrani() throws Exception {
        Libreria libreria = Libreria.getInstance();
        
        // Aggiungo un brano al catalogo tramite reflection
        Brano b = new Brano("Test Song", "Test Path", "Test Author", "Test Genre", 180, "2023", 1, null);
        
        Field catalogo = Libreria.class.getDeclaredField("catalogo");
        catalogo.setAccessible(true);
        ((List<IBrano>) catalogo.get(libreria)).add(b);
        
        assertEquals(1, libreria.getBrani().size());
        
        controller.aggiungiAPlaylist(null, "Rock");
        Playlist p = controller.getPlaylistMap().get("Rock");
        // Simulo l'aggiunta del brano alla playlist
        p.aggiungiBrano(b);
        
        assertEquals(1, controller.getPlaylist().size());
        
        // Simulo la conferma ed eliminazione
        controller.eliminaPlaylist("Rock");
        
        // Verifico che la playlist sia stata rimossa
        assertEquals(0, controller.getPlaylist().size());
        
        // Verifico che il catalogo generale sia intatto
        assertEquals(1, libreria.getBrani().size());
    }

    @Test
    public void testAnnullamento_LasciaPlaylistInvariata() throws Exception {
        controller.aggiungiAPlaylist(null, "Pop");
        Playlist p = controller.getPlaylistMap().get("Pop");
        
        assertEquals(1, controller.getPlaylist().size());
        
        // Simulo l'annullamento: il controller non viene chiamato, o viene chiamato con nome inesistente
        controller.eliminaPlaylist("NonEsistente");
        
        // La playlist deve rimanere invariata
        assertEquals(1, controller.getPlaylist().size());
        assertEquals("Pop", p.getNome());
        assertTrue(controller.getPlaylistMap().containsKey("Pop"));
    }
}
