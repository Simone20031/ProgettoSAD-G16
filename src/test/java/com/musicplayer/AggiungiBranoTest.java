package com.musicplayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AggiungiBranoTest {

    @Test
    public void testAggiungiBranoNonPresente() throws Exception {
        Playlist p = new Playlist("Rock");
        Brano b = new Brano("Test", "path", "author", "genre", 180, "2023", 1, null);
        
        // Aggiungo il brano tramite il metodo
        p.aggiungiBrano(b);
        
        // Verifico che sia presente e la dimensione sia 1
        assertEquals(1, p.getBrani().size());
        assertTrue(p.contieneBrano(b));
    }

    @Test
    public void testAggiungiBranoGiaPresente_LanciaEccezioneELasciaInvariato() throws Exception {
        Playlist p = new Playlist("Pop");
        Brano b = new Brano("Duplicato", "path2", "author2", "genre2", 200, "2023", 1, null);
        
        // Aggiunta iniziale
        p.aggiungiBrano(b);
        assertEquals(1, p.getBrani().size());
        
        // Tentativo di aggiunta duplicato
        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            p.aggiungiBrano(b);
        });
        
        // Verifico tipo di errore
        assertEquals(ValidazioneException.TipoErrore.DUPLICATO, ex.getTipo());
        
        // La lista deve rimanere invariata
        assertEquals(1, p.getBrani().size());
    }
}
