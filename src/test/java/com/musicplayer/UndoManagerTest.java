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

import static org.junit.jupiter.api.Assertions.*;

public class UndoManagerTest {

    private UndoManager undoManager;
    private LibreriaController controller;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        undoManager = new UndoManager();
        controller = new LibreriaController();
        tempDir = Files.createTempDirectory("test_libreria");
        
        // Pulizia iniziale Libreria per test
        Libreria.getInstance().eliminaBrani(Libreria.getInstance().getBrani());
    }

    @org.junit.jupiter.api.AfterEach
    public void cleanup() {
        try {
            java.io.File libDir = new java.io.File(com.musicplayer.PathUtils.getLibraryPath());
            if (libDir.exists() && libDir.listFiles() != null) {
                for (java.io.File f : libDir.listFiles()) {
                    if (f.getName().startsWith("test_")) {
                        f.delete();
                    }
                }
            }
            controller.eliminaPlaylist("MyTestPlaylist");
            
            // Clean up from memory as well just in case
            for (com.musicplayer.model.IBrano b : new java.util.ArrayList<>(controller.getBrani())) {
                if (((com.musicplayer.model.Brano) b).getPercorsoFile().contains("test_")) {
                    controller.eliminaBrano((com.musicplayer.model.Brano) b);
                }
            }
        } catch (Exception e) {}
    }

    @Test
    void testAggiungiERimuoviLibreriaUndo() throws Exception {
        // Simuliamo l'aggiunta di un brano
        File f = File.createTempFile("test_brano", ".mp3", tempDir.toFile());
        f.deleteOnExit();

        Command addCmd = new AggiungiALibreriaCmd(controller, "TitoloTest", "AutoreTest", "Pop", 2024, f.getAbsolutePath(), 180, "NESSUNO");
        addCmd.esegui();
        undoManager.aggiungiComando(addCmd);

        assertEquals(1, controller.getBrani().size());

        // Annulliamo l'aggiunta
        assertTrue(undoManager.canUndo());
        undoManager.annullaUltimaOperazione();
        
        assertEquals(0, controller.getBrani().size());

        // Simuliamo una rimozione di un brano fittizio 
        Command addCmd2 = new AggiungiALibreriaCmd(controller, "TitoloTest2", "AutoreTest", "Pop", 2024, f.getAbsolutePath(), 180, "NESSUNO");
        addCmd2.esegui();
        
        assertEquals(1, controller.getBrani().size());
        Brano b = (Brano) controller.getBrani().get(0);

        Command removeCmd = new RimuoviDaLibreriaCmd(controller, b);
        removeCmd.esegui();
        undoManager.aggiungiComando(removeCmd);

        assertEquals(0, controller.getBrani().size());

        // Annulliamo la rimozione
        undoManager.annullaUltimaOperazione();
        assertEquals(1, controller.getBrani().size());
        assertEquals("TitoloTest2", ((Brano) controller.getBrani().get(0)).getTitolo());
    }

    @Test
    void testAggiungiERimuoviPlaylistUndo() throws Exception {
        String playlistName = "MyTestPlaylist";
        BranoFactory bf = new BranoFactory();
        Brano b = bf.creaBrano("Test", "Autore", "Pop", 2020, "path", 120, Tag.NESSUNO);

        Command addCmd = new AggiungiAPlaylistCmd(controller, b, playlistName);
        addCmd.esegui();
        undoManager.aggiungiComando(addCmd);

        Playlist pl = controller.getPlaylistMap().get(playlistName);
        assertNotNull(pl);
        assertEquals(1, pl.getBrani().size());

        // Annulliamo l'aggiunta
        undoManager.annullaUltimaOperazione();
        assertEquals(0, pl.getBrani().size());

        // Ora lo aggiungiamo e simuliamo la rimozione
        addCmd.esegui();
        assertEquals(1, pl.getBrani().size());

        Command rmCmd = new RimuoviDaPlaylistCmd(controller, b, playlistName);
        rmCmd.esegui();
        undoManager.aggiungiComando(rmCmd);

        assertEquals(0, pl.getBrani().size());

        // Annulliamo la rimozione
        undoManager.annullaUltimaOperazione();
        assertEquals(1, pl.getBrani().size());
    }
}
