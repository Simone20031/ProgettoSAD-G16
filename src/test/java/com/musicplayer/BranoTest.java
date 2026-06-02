package com.musicplayer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * 1.7 - Classe di test JUnit per verificare la validazione dell'entità Brano.
 */
public class BranoTest {

    /**
     * Verifica che il metodo validaDati() si comporti correttamente 
     * quando tutti i campi del brano sono validi (Happy Path).
     * Non deve essere lanciata alcuna ValidazioneException.
     */
    @Test
    public void testValidaDati_TuttiICampiValidi_NessunaEccezione() {
        // 1. ARRANGIAME (Preparazione dei dati validi)
        String id = "test-uuid-12345";
        String titolo = "Bohemian Rhapsody";
        String autore = "Queen";
        String genere = "Rock";
        int anno = 1975; // Anno valido (compreso tra 1800 e 2100)
        String percorsoFile = "/Libreria/queen_bohemian.mp3";
        int durata = 355;
        Tag tag = Tag.PREFERITI;

        // Creazione dell'oggetto Brano con i dati validi
        Brano branoValido = new Brano(id, titolo, autore, genere, anno, percorsoFile, durata, tag);

        // 2. ACT & ASSERT (Esecuzione del comando e verifica)
        // assertDoesNotThrow si assicura che l'esecuzione del metodo vada a buon fine.
        // Se validaDati() dovesse lanciare per errore una ValidazioneException, il test fallirebbe immediatamente.
        assertDoesNotThrow(() -> {
            branoValido.validaDati();
        }, "Il metodo validaDati() non avrebbe dovuto lanciare alcuna eccezione con dati validi.");
    }
}