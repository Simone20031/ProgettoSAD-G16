package com.musicplayer;

import com.musicplayer.model.*;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * Test per la gestione della libreria vuota (isEmpty).
 * Verifica che il modello Libreria segnali correttamente
 * lo stato "vuoto", che la View utilizza per mostrare
 * il messaggio informativo "La libreria è vuota".
 */
public class LibreriaTest {

    /**
     * Resetta il Singleton di Libreria prima di ogni test,
     * in modo che ogni test parta da una libreria pulita
     * indipendentemente dall'ordine di esecuzione.
     */
    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    // =========================================================================
    // Test isEmpty()
    // =========================================================================

    /**
     * Verifica che una libreria appena creata sia vuota.
     * Corrisponde al caso d'uso: avvio dell'applicazione senza brani salvati.
     */
    @Test
    public void testIsEmpty_LibreriaAppenaCreata_DeveEssereVuota() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT & ASSERT
        assertTrue(libreria.isEmpty(),
                "Una libreria appena creata deve essere vuota.");
    }

    /**
     * Verifica che isEmpty() ritorni false dopo l'aggiunta di un brano.
     */
    @Test
    public void testIsEmpty_DopoAggiuntaBrano_NonDeveEssereVuota() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();
        Brano b = new Brano("id-1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 200, Tag.NESSUNO);

        // ACT
        libreria.aggiungiBrano(b);

        // ASSERT
        assertFalse(libreria.isEmpty(),
                "La libreria non deve essere vuota dopo l'aggiunta di un brano.");
    }

    /**
     * Verifica che isEmpty() ritorni true dopo che l'unico brano
     * viene eliminato — simula il caso in cui l'utente elimina
     * l'ultimo brano e la View deve mostrare "La libreria è vuota".
     */
    @Test
    public void testIsEmpty_DopoEliminazioneUltimoBrano_DeveRitornareVuota() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();
        Brano b = new Brano("id-1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 200, Tag.NESSUNO);
        libreria.aggiungiBrano(b);
        assertFalse(libreria.isEmpty(), "Pre-condizione: la libreria deve contenere almeno un brano.");

        // ACT
        libreria.eliminaBrano(b);

        // ASSERT
        assertTrue(libreria.isEmpty(),
                "La libreria deve essere vuota dopo l'eliminazione dell'unico brano.");
    }
}
