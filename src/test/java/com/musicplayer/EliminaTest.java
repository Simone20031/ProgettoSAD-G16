package com.musicplayer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * 3.3 - Classe di test JUnit dedicata alle logiche del Modello Libreria.
 */
public class EliminaTest {

    @Test
    public void testEliminaBrano() {
        // 1. ARRANGE
        Libreria libreria = Libreria.getInstance();
        
        // Memorizziamo quanti brani ci sono GIÀ nella RAM (es. caricati dal CSV di test)
        int braniPreesistenti = libreria.getBrani().size();
        
        Brano b1 = new Brano("id-1", "Song One", "Artist A", "Pop", 2020, "file1.mp3", 200, Tag.NESSUNO);
        Brano b2 = new Brano("id-2", "Song Two", "Artist B", "Rock", 2021, "file2.mp3", 180, Tag.NESSUNO);
        
        // Usiamo il metodo del modello per popolare la RAM
        libreria.aggiungiBrano(b1);
        libreria.aggiungiBrano(b2);
        
        // Il conteggio deve essere la somma dei preesistenti + i 2 nuovi inseriti
        int dimensioneIniziale = libreria.getBrani().size();
        assertEquals(braniPreesistenti + 2, dimensioneIniziale, "Il catalogo deve contenere i brani preesistenti più i 2 appena aggiunti.");

        // 2. ACT
        libreria.eliminaBrano(b1);

        // 3. ASSERT
        int dimensioneFinale = libreria.getBrani().size();
        
        // Dopo l'eliminazione, la dimensione deve essere calata esattamente di 1
        assertEquals(dimensioneIniziale - 1, dimensioneFinale, "Il catalogo deve contenere un brano in meno dopo l'eliminazione.");
        assertFalse(libreria.getBrani().contains(b1), "Il brano b1 avrebbe dovuto essere rimosso dal catalogo.");
        assertTrue(libreria.getBrani().contains(b2), "Il brano b2 deve essere ancora presente nel catalogo.");
    }
}