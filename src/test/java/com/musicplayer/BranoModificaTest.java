package com.musicplayer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * 2.6 - Classe di test dedicata alla modifica e mutazione dei dettagli di un Brano.
 */
public class BranoModificaTest {

    /**
     * Scenario 1: Modifica valida di tutti i campi.
     * Verifica che i campi dell'oggetto Brano mutino correttamente con i nuovi valori.
     */
    @Test
    public void testSetDettagli_ModificaValidaTuttiICampi_AggiornamentoCorretto() {
        Brano brano = new Brano("1", "Vecchio Titolo", "Vecchio Autore", "Pop", 2000, "file.mp3", 180, Tag.NESSUNO);

        Map<String, String> nuoviDati = new HashMap<>();
        nuoviDati.put("titolo", "Titolo Nuovo");
        nuoviDati.put("autore", "Autore Nuovo");
        nuoviDati.put("genere", "Rock");
        nuoviDati.put("anno", "2024");
        nuoviDati.put("durata", "240");
        nuoviDati.put("tag", "PREFERITI");

        assertDoesNotThrow(() -> brano.setDettagli(nuoviDati));

        assertEquals("Titolo Nuovo", brano.getTitolo());
        assertEquals("Autore Nuovo", brano.getAutore());
        assertEquals("Rock", brano.getGenere());
        assertEquals(2024, brano.getAnno());
        assertEquals(240, brano.getDurata());
        assertEquals(Tag.PREFERITI, brano.getTag());
    }

    /**
     * Scenario 2: Tentativo con campo obbligatorio vuoto.
     * Verifica che svuotando il titolo venga sollevata una ValidazioneException di tipo CAMPO_MANCANTE.
     */
    @Test
    public void testSetDettagli_CampoObbligatorioVuoto_LanciaCampoMancante() {
        Brano brano = new Brano("1", "Innuendo", "Queen", "Rock", 1991, "file.mp3", 390, Tag.NESSUNO);

        Map<String, String> datiErrati = new HashMap<>();
        datiErrati.put("titolo", "   "); // Solo spazi vuoti
        datiErrati.put("autore", "Queen");

        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            brano.setDettagli(datiErrati);
        });

        assertEquals(ValidazioneException.TipoErrore.CAMPO_MANCANTE, ex.getTipo());
        assertEquals("titolo", ex.getCampoErrato());
    }

    /**
     * Scenario 3: Tentativo con anno in formato errato.
     * Verifica che inserendo del testo alfanumerico nell'anno venga sollevata una ValidazioneException di tipo FORMATO_NON_VALIDO.
     */
    @Test
    public void testSetDettagli_AnnoFormatoErrato_LanciaFormatoNonValido() {
        Brano brano = new Brano("1", "Innuendo", "Queen", "Rock", 1991, "file.mp3", 390, Tag.NESSUNO);

        Map<String, String> datiErrati = new HashMap<>();
        datiErrati.put("titolo", "Innuendo");
        datiErrati.put("autore", "Queen");
        datiErrati.put("anno", "millenovecentonovantuno"); // Lettere invece di numeri!

        ValidazioneException ex = assertThrows(ValidazioneException.class, () -> {
            brano.setDettagli(datiErrati);
        });

        assertEquals(ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, ex.getTipo());
        assertEquals("anno", ex.getCampoErrato());
    }
}