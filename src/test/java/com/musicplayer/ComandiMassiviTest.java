package com.musicplayer;

import com.musicplayer.command.*;
import com.musicplayer.model.*;
import com.musicplayer.controller.LibreriaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ComandiMassiviTest {

    private UndoManager undoManager;
    private LibreriaController controller;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        undoManager = new UndoManager();
        controller = new LibreriaController();
        tempDir = Files.createTempDirectory("test_libreria_massivo");

        // Pulizia iniziale
        Libreria.getInstance().eliminaBrani(Libreria.getInstance().getBrani());
    }

    @Test
    void testAggiungiERimuoviMassivoPlaylistUndo() throws Exception {
        String playlistName = "MassivoPlaylist";
        BranoFactory bf = new BranoFactory();
        Brano b1 = bf.creaBrano("Test1", "Autore1", "Pop", 2020, "path1", 120, Tag.NESSUNO);
        Brano b2 = bf.creaBrano("Test2", "Autore2", "Pop", 2020, "path2", 150, Tag.NESSUNO);

        // Aggiunta massiva
        Command addBulkCmd = new AggiungiMassivoCmd(controller, Arrays.asList(b1, b2), playlistName);
        addBulkCmd.esegui();
        undoManager.aggiungiComando(addBulkCmd);

        Playlist pl = controller.getPlaylistMap().get(playlistName);
        assertNotNull(pl);
        assertEquals(2, pl.getBrani().size());

        // Annulla aggiunta massiva
        undoManager.annullaUltimaOperazione();
        assertEquals(0, pl.getBrani().size());

        // Rimozione massiva
        addBulkCmd.esegui();
        assertEquals(2, pl.getBrani().size());

        Command rmBulkCmd = new RimuoviMassivoCmd(controller, Arrays.asList(b1, b2), playlistName);
        rmBulkCmd.esegui();
        undoManager.aggiungiComando(rmBulkCmd);

        assertEquals(0, pl.getBrani().size());

        // Annulla rimozione massiva
        undoManager.annullaUltimaOperazione();
        assertEquals(2, pl.getBrani().size());
    }

    @Test
    void testRimuoviMassivoLibreriaUndo() throws Exception {
        File f1 = File.createTempFile("test_brano_massivo1", ".mp3", tempDir.toFile());
        File f2 = File.createTempFile("test_brano_massivo2", ".mp3", tempDir.toFile());
        f1.deleteOnExit();
        f2.deleteOnExit();

        Command addCmd1 = new AggiungiALibreriaCmd(controller, "Titolo1", "Autore1", "Pop", 2024, f1.getAbsolutePath(),
                180, "NESSUNO");
        Command addCmd2 = new AggiungiALibreriaCmd(controller, "Titolo2", "Autore2", "Pop", 2024, f2.getAbsolutePath(),
                200, "NESSUNO");
        addCmd1.esegui();
        addCmd2.esegui();

        assertEquals(2, controller.getBrani().size());
        Brano b1 = (Brano) controller.getBrani().get(0);
        Brano b2 = (Brano) controller.getBrani().get(1);

        Command removeBulk = new RimuoviMassivoLibreriaCmd(controller, Arrays.asList(b1, b2));
        removeBulk.esegui();
        undoManager.aggiungiComando(removeBulk);

        assertEquals(0, controller.getBrani().size());

        // Annulla rimozione massiva dalla libreria
        undoManager.annullaUltimaOperazione();
        assertEquals(2, controller.getBrani().size());
    }
}
