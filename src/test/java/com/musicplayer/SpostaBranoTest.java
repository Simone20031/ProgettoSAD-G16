package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import com.musicplayer.persistence.MetadataService;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpostaBranoTest {

    private LibreriaController controller;
    private Brano brano1;
    private Brano brano2;
    private Brano brano3;
    private Brano brano4;
    private final String testPlaylistName = "TestSpostaPlaylist";

    @BeforeEach
    public void setup() throws Exception {
        // Reset Singleton instances to have a clean environment
        Field instanceLibreria = Libreria.class.getDeclaredField("instance");
        instanceLibreria.setAccessible(true);
        instanceLibreria.set(null, null);

        Field instanceGestore = GestoreRiproduzione.class.getDeclaredField("instance");
        instanceGestore.setAccessible(true);
        instanceGestore.set(null, null);

        controller = new LibreriaController();

        // Create mock songs
        brano1 = new Brano("b1", "titolo1", "autore1", "Pop", 180, "2020", 0, Tag.NESSUNO);
        brano2 = new Brano("b2", "titolo2", "autore2", "Rock", 200, "2021", 0, Tag.NESSUNO);
        brano3 = new Brano("b3", "titolo3", "autore3", "Jazz", 220, "2022", 0, Tag.NESSUNO);
        brano4 = new Brano("b4", "titolo4", "autore4", "Metal", 240, "2023", 0, Tag.NESSUNO);

        Libreria.getInstance().aggiungiBrano(brano1);
        Libreria.getInstance().aggiungiBrano(brano2);
        Libreria.getInstance().aggiungiBrano(brano3);
        Libreria.getInstance().aggiungiBrano(brano4);

        // Add to playlist via controller to initialize it
        controller.aggiungiAPlaylist(brano1, testPlaylistName);
        controller.aggiungiAPlaylist(brano2, testPlaylistName);
        controller.aggiungiAPlaylist(brano3, testPlaylistName);
        controller.aggiungiAPlaylist(brano4, testPlaylistName);
    }

    @AfterEach
    public void cleanup() {
        try {
            controller.eliminaPlaylist(testPlaylistName);
        } catch (Exception ignored) {}
    }

    @Test
    public void testSpostaBrano_MantieniIntegritaEOrdinamento() throws Exception {
        Playlist pl = controller.getPlaylistMap().get(testPlaylistName);
        assertNotNull(pl, "La playlist deve esistere.");
        
        // Initial list order: b1, b2, b3, b4
        List<IBrano> tracks = pl.getBrani();
        assertEquals(4, tracks.size());
        assertEquals(brano1, tracks.get(0));
        assertEquals(brano2, tracks.get(1));
        assertEquals(brano3, tracks.get(2));
        assertEquals(brano4, tracks.get(3));

        // Initial ordinamento array check
        int[] expectedInitialOrd = {0, 1, 2, 3};
        assertArrayEquals(expectedInitialOrd, pl.getOrdinamento());

        // Move b1 (index 0) to position 2
        controller.spostaBranoInPlaylist(brano1, testPlaylistName, 2);

        // New order should be: b2, b3, b1, b4
        List<IBrano> tracksAfterMove = pl.getBrani();
        assertEquals(4, tracksAfterMove.size(), "La lista deve contenere ancora 4 elementi.");
        assertEquals(brano2, tracksAfterMove.get(0));
        assertEquals(brano3, tracksAfterMove.get(1));
        assertEquals(brano1, tracksAfterMove.get(2));
        assertEquals(brano4, tracksAfterMove.get(3));

        // Ordinamento array check after move
        int[] expectedAfterMoveOrd = {0, 1, 2, 3};
        assertArrayEquals(expectedAfterMoveOrd, pl.getOrdinamento(), "L'array ordinamento deve essere riallineato.");
    }

    @Test
    public void testSpostaBrano_PersistenzaRiaperturaApp() throws Exception {
        Playlist pl = controller.getPlaylistMap().get(testPlaylistName);
        assertNotNull(pl);

        // Move b4 (index 3) to position 0
        controller.spostaBranoInPlaylist(brano4, testPlaylistName, 0);

        // Verify order in memory: b4, b1, b2, b3
        List<IBrano> tracksMemory = pl.getBrani();
        assertEquals(brano4, tracksMemory.get(0));
        assertEquals(brano1, tracksMemory.get(1));
        assertEquals(brano2, tracksMemory.get(2));
        assertEquals(brano3, tracksMemory.get(3));

        // Simulate reopening of the app by resetting Singleton instances and creating a new controller
        Field instanceLibreria = Libreria.class.getDeclaredField("instance");
        instanceLibreria.setAccessible(true);
        instanceLibreria.set(null, null);

        LibreriaController newController = new LibreriaController();

        // Re-inject the brani into the newly created Libreria since the file system metadata.csv
        // stores songs and caricaDaCSV expects files to physically exist.
        // We can manually populate the new Libreria instance first.
        Libreria.getInstance().aggiungiBrano(brano1);
        Libreria.getInstance().aggiungiBrano(brano2);
        Libreria.getInstance().aggiungiBrano(brano3);
        Libreria.getInstance().aggiungiBrano(brano4);

        // Trigger loading of playlists from the CSV files saved previously
        MetadataService.caricaPlaylistDaCSV(newController.getPlaylistMap(), fn -> {
            if (fn.equals(brano1.getPercorsoFile())) return brano1;
            if (fn.equals(brano2.getPercorsoFile())) return brano2;
            if (fn.equals(brano3.getPercorsoFile())) return brano3;
            if (fn.equals(brano4.getPercorsoFile())) return brano4;
            return null;
        });

        Playlist loadedPl = newController.getPlaylistMap().get(testPlaylistName);
        assertNotNull(loadedPl, "La playlist salvata deve essere ricaricata.");

        // Verify the persisted order remains: b4, b1, b2, b3
        List<IBrano> tracksLoaded = loadedPl.getBrani();
        assertEquals(4, tracksLoaded.size(), "La playlist caricata deve contenere 4 brani.");
        assertEquals(brano4, tracksLoaded.get(0), "Il primo brano deve essere b4.");
        assertEquals(brano1, tracksLoaded.get(1), "Il secondo brano deve essere b1.");
        assertEquals(brano2, tracksLoaded.get(2), "Il terzo brano deve essere b2.");
        assertEquals(brano3, tracksLoaded.get(3), "Il quarto brano deve essere b3.");
    }

    @Test
    public void test3WayToggleSorting() throws Exception {
        Playlist pl = controller.getPlaylistMap().get(testPlaylistName);
        assertNotNull(pl);

        // 1. First click: Sort by ANNO ascending
        controller.ordinaLibreria(CampoOrdinamento.ANNO, testPlaylistName);
        List<IBrano> tracksAsc = pl.getBrani();
        assertEquals(brano1, tracksAsc.get(0)); // 2020
        assertEquals(brano2, tracksAsc.get(1)); // 2021
        assertEquals(brano3, tracksAsc.get(2)); // 2022
        assertEquals(brano4, tracksAsc.get(3)); // 2023

        // 2. Second click: Sort by ANNO descending
        controller.ordinaLibreria(CampoOrdinamento.ANNO, testPlaylistName);
        List<IBrano> tracksDesc = pl.getBrani();
        assertEquals(brano4, tracksDesc.get(0)); // 2023
        assertEquals(brano3, tracksDesc.get(1)); // 2022
        assertEquals(brano2, tracksDesc.get(2)); // 2021
        assertEquals(brano1, tracksDesc.get(3)); // 2020

        // 3. Third click: Reset sorting (should restore original order: b1, b2, b3, b4)
        controller.ordinaLibreria(CampoOrdinamento.ANNO, testPlaylistName);
        List<IBrano> tracksReset = pl.getBrani();
        assertEquals(brano1, tracksReset.get(0));
        assertEquals(brano2, tracksReset.get(1));
        assertEquals(brano3, tracksReset.get(2));
        assertEquals(brano4, tracksReset.get(3));
    }
}
