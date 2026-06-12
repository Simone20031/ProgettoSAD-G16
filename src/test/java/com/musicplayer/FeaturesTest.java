package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeaturesTest {

    private LibreriaController controller;
    private Libreria libreria;
    private BranoFactory factory;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LibreriaController();
        libreria = Libreria.getInstance();

        java.lang.reflect.Field f = Libreria.class.getDeclaredField("catalogo");
        f.setAccessible(true);
        ((java.util.List<?>) f.get(libreria)).clear();

        controller.getPlaylistMap().clear();
        factory = new BranoFactory();
    }

    @Test
    void testGeneriETagEnum() throws Exception {
        // Test feature 15: Genere e Tag diventanti bottoni selezionabili (uso di Enum)
        Brano brano = factory.creaBrano("Titolo", "Autore", "Rock", 2024, "C:\\test.mp3", 120, Tag.PREFERITI);

        assertEquals("Rock", brano.getGenere()); // Conserva il getter testuale
        assertEquals(Tag.PREFERITI, brano.getTag()); // Tag è un Enum fortemente tipizzato
    }

    @Test
    void testCreazionePlaylistCaseInsensitive() throws Exception {
        // Test: Controllo creazione playlist esistente case-insensitive (allineato con l'implementazione)
        try {
            controller.aggiungiAPlaylist(null, "Rock");
            
            assertThrows(ValidazioneException.class, () -> {
                controller.aggiungiAPlaylist(null, "rock");
            });

            assertThrows(ValidazioneException.class, () -> {
                controller.aggiungiAPlaylist(null, "ROCK");
            });
        } finally {
            controller.eliminaPlaylist("Rock");
            controller.eliminaPlaylist("rock");
            controller.eliminaPlaylist("ROCK");
        }
    }

    @Test
    void testLunghezzaNomePlaylist() throws Exception {
        // Test feature 4: Lunghezza del nome della playlist
        String nomeLungo = "Questa e' una playlist con un nome estremamente lungo per testare i puntini di sospensione e il salvataggio corretto";
        controller.aggiungiAPlaylist(null, nomeLungo);

        Playlist p = controller.getPlaylistMap().get(nomeLungo);
        assertNotNull(p);
        assertEquals(nomeLungo, p.getNome());
        
        // Pulizia: eliminiamo la playlist creata col nome lungo
        controller.eliminaPlaylist(nomeLungo);
    }

    @Test
    void testSincronizzazioneEliminazioneBrano() throws Exception {
        // Test feature 7 & 13: Sincronizzazione libreria e playlist, riferimenti invece
        // di copie
        Brano brano = factory.creaBrano("SyncTest", "Autore", "Pop", 2024, "C:\\sync.mp3", 180, Tag.NESSUNO);
        libreria.aggiungiBrano(brano);

        controller.aggiungiAPlaylist(null, "MyPlaylist");
        controller.aggiungiAPlaylist(brano, "MyPlaylist");

        Playlist p = controller.getPlaylistMap().get("MyPlaylist");
        assertEquals(1, p.getBrani().size());

        // Verifica feature 13: Il brano in playlist è lo stesso oggetto in libreria
        assertSame(brano, p.getBrani().get(0));

        // Simula l'eliminazione da libreria
        controller.eliminaBranoPerFilename("sync.mp3");

        // Verifica feature 7: Il brano deve sparire anche dalla playlist
        assertTrue(p.getBrani().isEmpty(), "Il brano doveva essere rimosso anche dalla playlist sincronizzata");
        
        // Pulizia
        controller.eliminaPlaylist("MyPlaylist");
    }

    @Test
    void testObserverGestoreRiproduzione() {
        // Test feature 10: GestoreRiproduzione e observer
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();

        final boolean[] notificato = { false };
        RiproduzioneObserver observer = new RiproduzioneObserver() {
            @Override
            public void onPlayerReady(int durata) {
            }

            @Override
            public void onPlay() {
                notificato[0] = true;
            }

            @Override
            public void onPausa() {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onProgressoAggiornato(int secondi) {
            }

            @Override
            public void onBranoCambiato(String nuovoPercorso) {
            }

            @Override
            public void onBranoRipetuto() {
            }

            @Override
            public void onCodaAggiornata() {
            }
        };

        gestore.addObserver(observer);

        // verifichiamo che l'attach dell'observer avvenga senza errori.
        gestore.removeObserver(observer);
    }
}
