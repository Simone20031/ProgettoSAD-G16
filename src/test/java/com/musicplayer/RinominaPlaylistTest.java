package com.musicplayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RinominaPlaylistTest {

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
            controller.eliminaPlaylist("Indie Rock");
            controller.eliminaPlaylist("Pop");
            controller.eliminaPlaylist("ROCK");
        } catch (Exception e) {}
    }

    @Test
    public void testRinominaPlaylist_NomeValido_AggiornaCorrettamente() throws Exception {
        controller.aggiungiAPlaylist(null, "Rock");
        
        controller.rinominaPlaylist("Rock", "Indie Rock");
        Playlist p2 = controller.getPlaylistMap().get("Indie Rock");
        
        assertEquals("Indie Rock", p2.getNome());
    }

    @Test
    public void testRinominaPlaylist_NomeUgualeEsistente_LanciaEccezione() throws Exception {
        controller.aggiungiAPlaylist(null, "Rock");
        controller.aggiungiAPlaylist(null, "Pop");
        Playlist p2 = controller.getPlaylistMap().get("Pop");
        
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            controller.rinominaPlaylist("Pop", "Rock");
        });
        
        assertEquals("Una playlist con questo nome esiste già!", ex.getMessage());
        assertEquals("Pop", p2.getNome(), "Il nome non dovrebbe essere modificato in caso di duplicato");
    }
    
    @Test
    public void testRinominaPlaylist_StessoNomeConCaseDiverso_LanciaEccezione() throws Exception {
        controller.aggiungiAPlaylist(null, "Rock");
        controller.aggiungiAPlaylist(null, "Pop");
        Playlist p2 = controller.getPlaylistMap().get("Pop");
        
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            controller.rinominaPlaylist("Pop", "ROCK");
        });
        
        assertEquals("Una playlist con questo nome esiste già!", ex.getMessage());
        assertEquals("Pop", p2.getNome(), "Il nome non dovrebbe essere modificato in caso di duplicato case-insensitive");
    }

    @Test
    public void testRinominaPlaylist_SeStessaStessoNome_NonFaNullaOAggiornaCase() throws Exception {
        controller.aggiungiAPlaylist(null, "Rock");
        
        // Questo non dovrebbe lanciare eccezione per duplicato, visto che è la stessa playlist
        controller.rinominaPlaylist("Rock", "ROCK");
        Playlist p = controller.getPlaylistMap().get("ROCK");
        
        assertEquals("ROCK", p.getNome());
    }
}