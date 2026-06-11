package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassificaAscoltiTest {

    private LibreriaController controller;
    private Brano brano1;
    private Brano brano2;
    private Brano brano3;
    private Playlist playlist1;

    @BeforeEach
    void setUp() {
        // Usa una nuova istanza se possibile, o resetta Libreria in modo fittizio
        controller = new LibreriaController();
        
        brano1 = new Brano("b1", "Canzone 1", "Autore A", "Pop", 2020, "b1.mp3", 180, Tag.NESSUNO);
        brano2 = new Brano("b2", "Canzone 2", "Autore B", "Rock", 2021, "b2.mp3", 200, Tag.NESSUNO);
        brano3 = new Brano("b3", "Canzone 3", "Autore C", "Jazz", 2022, "b3.mp3", 220, Tag.NESSUNO);
        
        playlist1 = new Playlist("p1", "My Playlist");

        // Per testare getTopBraniAscoltati in modo pulito e indipendente
        // aggiungiamo i brani al catalogo della libreria.
        // Almeno settiamo a zero i counter per sicurezza.
        brano1.setPlayCount(0);
        brano2.setPlayCount(0);
        brano3.setPlayCount(0);
        playlist1.setPlayCount(0);
    }

    @Test
    void testIncrementoContatore() {
        assertEquals(0, brano1.getPlayCount());
        
        brano1.incrementPlayCount();
        assertEquals(1, brano1.getPlayCount());
        
        brano1.incrementPlayCount();
        assertEquals(2, brano1.getPlayCount());
        
        assertEquals(0, playlist1.getPlayCount());
        playlist1.incrementPlayCount();
        assertEquals(1, playlist1.getPlayCount());
    }

    @Test
    void testGraduatoriaBrani() {
        // Riproduciamo brano1 5 volte
        for (int i = 0; i < 5; i++) brano1.incrementPlayCount();
        // Riproduciamo brano2 3 volte
        for (int i = 0; i < 3; i++) brano2.incrementPlayCount();
        // Riproduciamo brano3 10 volte
        for (int i = 0; i < 10; i++) brano3.incrementPlayCount();

        // Aggiungiamo forzatamente i brani ad una lista per verificare il sorting
        // oppure usiamo direttamente la logica della libreria se è pulibile
        Libreria libreria = Libreria.getInstance();
        if (!libreria.getBrani().contains(brano1)) libreria.aggiungiBrano(brano1);
        if (!libreria.getBrani().contains(brano2)) libreria.aggiungiBrano(brano2);
        if (!libreria.getBrani().contains(brano3)) libreria.aggiungiBrano(brano3);

        List<IBrano> top = controller.getTopBraniAscoltati();
        
        assertFalse(top.isEmpty(), "La lista dei top brani non deve essere vuota");
        assertEquals(brano3, top.get(0), "Il brano più riprodotto dovrebbe essere brano3");
        assertEquals(brano1, top.get(1), "Il secondo brano dovrebbe essere brano1");
        assertEquals(brano2, top.get(2), "Il terzo brano dovrebbe essere brano2");
    }
}
