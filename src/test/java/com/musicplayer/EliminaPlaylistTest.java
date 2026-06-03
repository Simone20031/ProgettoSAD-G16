package com.musicplayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EliminaPlaylistTest {

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
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
        
        Playlist p = libreria.creaPlaylist("Rock");
        // Simulo l'aggiunta del brano alla playlist
        p.getBrani().add(b);
        
        assertEquals(1, libreria.getPlaylist().size());
        
        // Simulo la conferma ed eliminazione
        libreria.eliminaPlaylist(p);
        
        // Verifico che la playlist sia stata rimossa
        assertEquals(0, libreria.getPlaylist().size());
        
        // Verifico che il catalogo generale sia intatto
        assertEquals(1, libreria.getBrani().size());
    }

    @Test
    public void testAnnullamento_LasciaPlaylistInvariata() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        Playlist p = libreria.creaPlaylist("Pop");
        
        assertEquals(1, libreria.getPlaylist().size());
        
        // Simulo l'annullamento: il controller non viene chiamato, o viene chiamato con null
        libreria.eliminaPlaylist(null);
        
        // La playlist deve rimanere invariata
        assertEquals(1, libreria.getPlaylist().size());
        assertEquals("Pop", p.getNome());
        assertEquals(p, libreria.getPlaylist().get(0));
    }
}