package com.musicplayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RinominaPlaylistTest {

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testRinominaPlaylist_NomeValido_AggiornaCorrettamente() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        Playlist p = libreria.creaPlaylist("Rock");
        
        libreria.rinominaPlaylist(p, "Indie Rock");
        
        assertEquals("Indie Rock", p.getNome());
    }

    @Test
    public void testRinominaPlaylist_NomeVuoto_LanciaEccezione() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        Playlist p = libreria.creaPlaylist("Rock");
        
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            libreria.rinominaPlaylist(p, "   ");
        });
        
        assertEquals(ValidazioneException.TipoErrore.CAMPO_MANCANTE, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
        assertEquals("Rock", p.getNome(), "Il nome non dovrebbe essere modificato in caso di errore");
    }

    @Test
    public void testRinominaPlaylist_NomeUgualeEsistente_LanciaEccezione() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        libreria.creaPlaylist("Rock");
        Playlist p2 = libreria.creaPlaylist("Pop");
        
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            libreria.rinominaPlaylist(p2, "Rock");
        });
        
        assertEquals(ValidazioneException.TipoErrore.GENERICO, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
        assertEquals("Pop", p2.getNome(), "Il nome non dovrebbe essere modificato in caso di duplicato");
    }
    
    @Test
    public void testRinominaPlaylist_StessoNomeConCaseDiverso_LanciaEccezione() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        libreria.creaPlaylist("Rock");
        Playlist p2 = libreria.creaPlaylist("Pop");
        
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            libreria.rinominaPlaylist(p2, "ROCK");
        });
        
        assertEquals(ValidazioneException.TipoErrore.GENERICO, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
        assertEquals("Pop", p2.getNome(), "Il nome non dovrebbe essere modificato in caso di duplicato case-insensitive");
    }

    @Test
    public void testRinominaPlaylist_SeStessaStessoNome_NonFaNullaOAggiornaCase() throws ValidazioneException {
        Libreria libreria = Libreria.getInstance();
        Playlist p = libreria.creaPlaylist("Rock");
        
        // Questo non dovrebbe lanciare eccezione per duplicato, visto che è la stessa playlist
        libreria.rinominaPlaylist(p, "ROCK");
        
        assertEquals("ROCK", p.getNome());
    }
}