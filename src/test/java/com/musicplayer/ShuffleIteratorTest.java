package com.musicplayer;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

public class ShuffleIteratorTest {

    static class TestBrano implements IBrano {
        private final String titolo;
        private final int durata;

        public TestBrano(String titolo, int durata) {
            this.titolo = titolo;
            this.durata = durata;
        }

        @Override
        public java.util.Map<String, String> getDettagli() {
            return Collections.emptyMap();
        }

        @Override
        public int getDurata() {
            return durata;
        }

        @Override
        public String getTitolo() {
            return titolo;
        }

        @Override
        public String toString() {
            return titolo;
        }
    }

    @Test
    public void testEmptyPlaylist() {
        Playlist playlist = new Playlist("pl-empty", "Empty Playlist");
        PlaylistIterator it = playlist.creaIteratorShuffle();

        assertFalse(it.hasNext(), "Un'iterazione su playlist vuota non dovrebbe avere elementi successivi.");
        assertThrows(NoSuchElementException.class, it::next, "La chiamata a next() su iterator vuoto deve sollevare NoSuchElementException.");
    }

    @Test
    public void testOneTrack() {
        Playlist playlist = new Playlist("pl-one", "One Track Playlist");
        IBrano track = new TestBrano("Single", 180);
        playlist.aggiungiBrano(track);

        PlaylistIterator it = playlist.creaIteratorShuffle();
        assertTrue(it.hasNext());
        assertEquals(track, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testRandomOrder() {
        // Creiamo una playlist con 15 brani differenti
        Playlist playlist = new Playlist("pl-rand", "Random Playlist");
        List<IBrano> originalOrder = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            IBrano b = new TestBrano("Track " + i, 100 + i);
            playlist.aggiungiBrano(b);
            originalOrder.add(b);
        }

        PlaylistIterator it = playlist.creaIteratorShuffle();
        List<IBrano> shuffledOrder = new ArrayList<>();
        while (it.hasNext()) {
            shuffledOrder.add(it.next());
        }

        assertEquals(originalOrder.size(), shuffledOrder.size());
        assertTrue(shuffledOrder.containsAll(originalOrder));

        // C'è una probabilità estremamente bassa (1 su 1.3 * 10^12) che 15 elementi vengano
        // estratti esattamente nello stesso ordine di inserimento per puro caso.
        boolean isDifferent = false;
        for (int i = 0; i < originalOrder.size(); i++) {
            if (!originalOrder.get(i).equals(shuffledOrder.get(i))) {
                isDifferent = true;
                break;
            }
        }
        assertTrue(isDifferent, "L'ordine di estrazione dello shuffle dovrebbe essere casuale e differire da quello originale.");
    }

    @Test
    public void testAvoidRepetition() {
        Playlist playlist = new Playlist("pl-rep", "No Repetition Playlist");
        List<IBrano> tracks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            IBrano b = new TestBrano("Track " + i, 200);
            playlist.aggiungiBrano(b);
            tracks.add(b);
        }

        PlaylistIterator it = playlist.creaIteratorShuffle();
        List<IBrano> played = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            assertTrue(it.hasNext());
            IBrano nextTrack = it.next();
            assertFalse(played.contains(nextTrack), "Un brano non deve essere riprodotto più di una volta in un ciclo.");
            played.add(nextTrack);
        }

        assertFalse(it.hasNext(), "L'iterator dovrebbe essere terminato dopo aver riprodotto tutti i brani.");
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void testDynamicAddition() {
        Playlist playlist = new Playlist("pl-dyn-add", "Dynamic Add Playlist");
        IBrano a = new TestBrano("A", 100);
        IBrano b = new TestBrano("B", 100);
        IBrano c = new TestBrano("C", 100);
        playlist.aggiungiBrano(a);
        playlist.aggiungiBrano(b);
        playlist.aggiungiBrano(c);

        PlaylistIterator it = playlist.creaIteratorShuffle();
        
        // Estrai il primo brano
        IBrano firstPlayed = it.next();
        
        // Aggiungi un nuovo brano durante la riproduzione
        IBrano d = new TestBrano("D", 100);
        playlist.aggiungiBrano(d);

        // Raccogli i brani rimanenti
        List<IBrano> remaining = new ArrayList<>();
        while (it.hasNext()) {
            remaining.add(it.next());
        }

        // Verifiche:
        // 1. Il brano 'd' aggiunto deve essere tra quelli estratti
        assertTrue(remaining.contains(d), "Il nuovo brano aggiunto deve essere incluso nel bacino delle tracce da estrarre.");
        // 2. In totale devono essere estratti tutti i 4 brani (1 inizialmente + 3 rimanenti)
        assertEquals(3, remaining.size(), "Dovrebbero esserci esattamente 3 brani rimanenti da estrarre.");
        assertFalse(remaining.contains(firstPlayed), "Il brano già riprodotto non deve essere reinserito nei rimanenti.");
    }

    @Test
    public void testDynamicRemoval() {
        Playlist playlist = new Playlist("pl-dyn-rem", "Dynamic Remove Playlist");
        IBrano a = new TestBrano("A", 100);
        IBrano b = new TestBrano("B", 100);
        IBrano c = new TestBrano("C", 100);
        playlist.aggiungiBrano(a);
        playlist.aggiungiBrano(b);
        playlist.aggiungiBrano(c);

        PlaylistIterator it = playlist.creaIteratorShuffle();

        // Estraiamo il primo brano
        IBrano firstPlayed = it.next();

        // Identifichiamo i brani non ancora riprodotti
        List<IBrano> candidates = new ArrayList<>();
        if (!a.equals(firstPlayed)) candidates.add(a);
        if (!b.equals(firstPlayed)) candidates.add(b);
        if (!c.equals(firstPlayed)) candidates.add(c);

        // Rimuoviamo uno dei brani candidati dalla playlist
        IBrano toRemove = candidates.get(0);
        IBrano expectedRemaining = candidates.get(1);
        playlist.rimuoviBrano(toRemove);

        // Estraiamo il prossimo brano
        assertTrue(it.hasNext());
        IBrano secondPlayed = it.next();

        // Deve essere quello rimasto, e toRemove non deve più apparire
        assertEquals(expectedRemaining, secondPlayed, "Il brano estratto deve essere l'unico rimasto non rimosso.");
        assertFalse(it.hasNext(), "Non dovrebbero esserci altri brani da estrarre dopo la rimozione.");
    }

    @Test
    public void testReset() {
        Playlist playlist = new Playlist("pl-reset", "Reset Playlist");
        IBrano a = new TestBrano("A", 100);
        IBrano b = new TestBrano("B", 100);
        playlist.aggiungiBrano(a);
        playlist.aggiungiBrano(b);

        ShuffleIterator it = (ShuffleIterator) playlist.creaIteratorShuffle();
        
        // Consuma l'iterator
        it.next();
        it.next();
        assertFalse(it.hasNext());

        // Reset
        it.reset();
        assertTrue(it.hasNext(), "Dopo il reset l'iterator deve poter essere riutilizzato.");
        
        List<IBrano> secondRun = new ArrayList<>();
        secondRun.add(it.next());
        secondRun.add(it.next());
        assertEquals(2, secondRun.size());
        assertTrue(secondRun.contains(a));
        assertTrue(secondRun.contains(b));
    }
}
