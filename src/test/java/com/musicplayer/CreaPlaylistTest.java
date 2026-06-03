package com.musicplayer;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test per Libreria.creaPlaylist() — Task 7.1.
 * Copre: nome vuoto, nome duplicato (case-insensitive), creazione corretta.
 */
public class CreaPlaylistTest {

    /**
     * Resetta il Singleton di Libreria prima di ogni test
     * per garantire isolamento completo.
     */
    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instance = Libreria.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    // =========================================================================
    // Test: nome vuoto o null → ValidazioneException (CAMPO_MANCANTE)
    // =========================================================================

    /**
     * Verifica che un nome null lanci ValidazioneException di tipo CAMPO_MANCANTE.
     */
    @Test
    public void testCreaPlaylist_NomeNull_LanciaEccezione() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT & ASSERT
        ValidazioneException ex = assertThrows(ValidazioneException.class,
                () -> libreria.creaPlaylist(null),
                "Un nome null deve lanciare ValidazioneException.");

        assertEquals(ValidazioneException.TipoErrore.CAMPO_MANCANTE, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
    }

    /**
     * Verifica che un nome vuoto ("") lanci ValidazioneException di tipo CAMPO_MANCANTE.
     */
    @Test
    public void testCreaPlaylist_NomeVuoto_LanciaEccezione() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT & ASSERT
        ValidazioneException ex = assertThrows(ValidazioneException.class,
                () -> libreria.creaPlaylist(""),
                "Un nome vuoto deve lanciare ValidazioneException.");

        assertEquals(ValidazioneException.TipoErrore.CAMPO_MANCANTE, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
    }

    /**
     * Verifica che un nome composto solo da spazi lanci ValidazioneException.
     */
    @Test
    public void testCreaPlaylist_NomeSoloSpazi_LanciaEccezione() {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT & ASSERT
        ValidazioneException ex = assertThrows(ValidazioneException.class,
                () -> libreria.creaPlaylist("   "),
                "Un nome di soli spazi deve lanciare ValidazioneException.");

        assertEquals(ValidazioneException.TipoErrore.CAMPO_MANCANTE, ex.getTipo());
    }

    // =========================================================================
    // Test: nome duplicato (case-insensitive) → ValidazioneException (GENERICO)
    // =========================================================================

    /**
     * Verifica che creare due playlist con lo stesso nome esatto lanci un'eccezione.
     */
    @Test
    public void testCreaPlaylist_NomeDuplicatoEsatto_LanciaEccezione() throws ValidazioneException {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();
        libreria.creaPlaylist("Preferiti");

        // ACT & ASSERT
        ValidazioneException ex = assertThrows(ValidazioneException.class,
                () -> libreria.creaPlaylist("Preferiti"),
                "Un nome duplicato deve lanciare ValidazioneException.");

        assertEquals(ValidazioneException.TipoErrore.GENERICO, ex.getTipo());
        assertEquals("nome", ex.getCampoErrato());
    }

    /**
     * Verifica il confronto case-insensitive: "preferiti" è duplicato di "Preferiti".
     */
    @Test
    public void testCreaPlaylist_NomeDuplicatoCaseInsensitive_LanciaEccezione() throws ValidazioneException {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();
        libreria.creaPlaylist("Preferiti");

        // ACT & ASSERT
        ValidazioneException ex = assertThrows(ValidazioneException.class,
                () -> libreria.creaPlaylist("PREFERITI"),
                "Il controllo duplicati deve essere case-insensitive.");

        assertEquals(ValidazioneException.TipoErrore.GENERICO, ex.getTipo());
    }

    // =========================================================================
    // Test: creazione corretta (happy path)
    // =========================================================================

    /**
     * Verifica che la playlist creata abbia il nome atteso
     * e sia registrata nella libreria.
     */
    @Test
    public void testCreaPlaylist_NomeValido_CreaERegistra() throws ValidazioneException {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT
        Playlist p = libreria.creaPlaylist("Preferiti");

        // ASSERT
        assertNotNull(p, "La playlist restituita non deve essere null.");
        assertEquals("Preferiti", p.getNome(),
                "Il nome della playlist deve corrispondere a quello passato.");
        assertTrue(libreria.getPlaylist().contains(p),
                "La playlist deve essere registrata nella libreria.");
    }

    /**
     * Verifica che due playlist con nomi diversi possano coesistere.
     */
    @Test
    public void testCreaPlaylist_DueNomiDiversi_EntrambeRegistrate() throws ValidazioneException {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT
        Playlist p1 = libreria.creaPlaylist("Preferiti");
        Playlist p2 = libreria.creaPlaylist("Rock");

        // ASSERT
        assertEquals(2, libreria.getPlaylist().size(),
                "Devono esserci esattamente 2 playlist.");
        assertTrue(libreria.getPlaylist().contains(p1));
        assertTrue(libreria.getPlaylist().contains(p2));
    }

    /**
     * Verifica che il nome venga trimmato degli spazi iniziali/finali.
     */
    @Test
    public void testCreaPlaylist_NomeConSpazi_VieneTroccato() throws ValidazioneException {
        // ARRANGE
        Libreria libreria = Libreria.getInstance();

        // ACT
        Playlist p = libreria.creaPlaylist("  Chill  ");

        // ASSERT
        assertEquals("Chill", p.getNome(),
                "Il nome della playlist deve essere trimmato degli spazi.");
    }
}