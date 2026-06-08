package com.musicplayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GestioneBraniRiproduzioneTest {

    private Brano brano1;
    private Brano brano2;
    private Brano brano3;

    @BeforeEach
    public void setUp() throws Exception {
        GestoreRiproduzione.resetInstance();
        BranoFactory factory = new BranoFactory();
        brano1 = factory.creaBrano("T1", "A1", "Pop", 2020, "b1.mp3", 100, Tag.RELAX);
        brano2 = factory.creaBrano("T2", "A2", "Pop", 2021, "b2.mp3", 120, Tag.ENERGIA);
        brano3 = factory.creaBrano("T3", "A3", "Rock", 2022, "b3.mp3", 140, Tag.RELAX);
    }

    @Test
    public void testAggiornaCoda_AggiuntaBrano() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        gestore.setStrategia(new SequentialStrategy());

        List<IBrano> listaIniziale = new ArrayList<>();
        listaIniziale.add(brano1);
        listaIniziale.add(brano2);

        // Imposta l'iteratore iniziale
        gestore.setIterator(new SequentialIterator(listaIniziale));
        
        assertEquals(2, gestore.getIterator().getBrani().size(), "L'iteratore iniziale deve avere 2 brani");

        // Simula l'aggiunta di un brano alla playlist
        List<IBrano> listaAggiornata = new ArrayList<>(listaIniziale);
        listaAggiornata.add(brano3);

        // Aggiorna la coda
        gestore.aggiornaCoda(listaAggiornata);

        // Verifica che il nuovo iteratore abbia i brani aggiornati
        assertNotNull(gestore.getIterator(), "L'iteratore non deve essere nullo");
        assertEquals(3, gestore.getIterator().getBrani().size(), "L'iteratore aggiornato deve avere 3 brani");
        assertTrue(gestore.getIterator().getBrani().contains(brano3), "L'iteratore deve contenere il nuovo brano");
    }

    @Test
    public void testAggiornaCoda_RimozioneBrano() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        gestore.setStrategia(new SequentialStrategy());

        List<IBrano> listaIniziale = new ArrayList<>();
        listaIniziale.add(brano1);
        listaIniziale.add(brano2);
        listaIniziale.add(brano3);

        gestore.setIterator(new SequentialIterator(listaIniziale));
        assertEquals(3, gestore.getIterator().getBrani().size(), "L'iteratore iniziale deve avere 3 brani");

        // Simula la rimozione di un brano
        List<IBrano> listaAggiornata = new ArrayList<>(listaIniziale);
        listaAggiornata.remove(brano2);

        // Aggiorna la coda
        gestore.aggiornaCoda(listaAggiornata);

        // Verifica
        assertEquals(2, gestore.getIterator().getBrani().size(), "L'iteratore aggiornato deve avere 2 brani");
        assertFalse(gestore.getIterator().getBrani().contains(brano2), "L'iteratore non deve contenere il brano rimosso");
    }

    @Test
    public void testAggiornaCoda_SvuotamentoPlaylist() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        List<IBrano> listaIniziale = new ArrayList<>();
        listaIniziale.add(brano1);
        
        gestore.setIterator(new SequentialIterator(listaIniziale));
        
        // Simula la rimozione dell'unico brano
        List<IBrano> listaAggiornata = new ArrayList<>();
        gestore.aggiornaCoda(listaAggiornata);
        
        assertNull(gestore.getIterator(), "L'iteratore deve essere nullo se la playlist è vuota");
    }
}
