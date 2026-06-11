package com.musicplayer;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import com.musicplayer.strategy.*;
import com.musicplayer.state.*;


import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

public class PlaybackStrategyTest {

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
        public int getDurataTotale() {
            return getDurata();
        }
        
        @Override
        public int getPlayCount() {
            return 0;
        }
        
        @Override
        public void incrementPlayCount() {
        }

        @Override
        public String toString() {
            return titolo;
        }
    }

    @Test
    public void testSequentialStrategy() {
        PlaybackStrategy strategy = new SequentialStrategy();
        
        // Playlist size = 3. Current index = 0 -> next index should be 1
        assertEquals(1, strategy.prossimoIndice(0, 3, null));
        
        // Current index = 1 -> next index should be 2
        assertEquals(2, strategy.prossimoIndice(1, 3, null));
        
        // Current index = 2 (last song) -> next index should be -1 (stop)
        assertEquals(-1, strategy.prossimoIndice(2, 3, null));
        
        // Empty size -> should return -1
        assertEquals(-1, strategy.prossimoIndice(0, 0, null));
    }

    @Test
    public void testLoopStrategy() {
        PlaybackStrategy strategy = new LoopStrategy();
        
        // Playlist size = 3. Current index = 0 -> next index should be 1
        assertEquals(1, strategy.prossimoIndice(0, 3, null));
        
        // Current index = 1 -> next index should be 2
        assertEquals(2, strategy.prossimoIndice(1, 3, null));
        
        // Current index = 2 (last song) -> next index should wrap around to 0
        assertEquals(0, strategy.prossimoIndice(2, 3, null));
        
        // Empty size -> should return -1
        assertEquals(-1, strategy.prossimoIndice(0, 0, null));
    }

    @Test
    public void testShuffleStrategy() {
        ShuffleStrategy strategy = new ShuffleStrategy();
        int size = 5;

        List<Integer> played = new ArrayList<>();
        int current = -1;
        for (int i = 0; i < size; i++) {
            current = strategy.prossimoIndice(current, size, played);
            assertTrue(current >= 0 && current < size);
            assertFalse(played.contains(current));
            played.add(current);
        }

        // All elements should have been played
        assertEquals(size, played.size());

        // Next index after playing all should be -1
        assertEquals(-1, strategy.prossimoIndice(current, size, played));
    }

    @Test
    public void testSequentialIterator() {
        List<IBrano> tracks = new ArrayList<>();
        IBrano a = new TestBrano("A", 120);
        IBrano b = new TestBrano("B", 180);
        IBrano c = new TestBrano("C", 150);
        tracks.add(a);
        tracks.add(b);
        tracks.add(c);

        SequentialIterator iterator = new SequentialIterator(tracks);
        
        // Before starting
        assertTrue(iterator.hasNext());
        assertEquals(a, iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(b, iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(c, iterator.next());
        
        // End of playlist (FINE PLAYLIST)
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testLoopIterator() {
        List<IBrano> tracks = new ArrayList<>();
        IBrano a = new TestBrano("A", 120);
        IBrano b = new TestBrano("B", 180);
        tracks.add(a);
        tracks.add(b);

        LoopIterator iterator = new LoopIterator(tracks);
        
        assertTrue(iterator.hasNext());
        assertEquals(a, iterator.next());
        
        assertTrue(iterator.hasNext());
        assertEquals(b, iterator.next());
        
        // Loops back to A
        assertTrue(iterator.hasNext());
        assertEquals(a, iterator.next());
    }

    @Test
    public void testShuffleIterator() {
        List<IBrano> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(new TestBrano("Track " + i, 100));
        }

        ShuffleIterator iterator = new ShuffleIterator(tracks);
        List<IBrano> played = new ArrayList<>();
        while (iterator.hasNext()) {
            played.add(iterator.next());
        }

        assertEquals(10, played.size());
        assertTrue(played.containsAll(tracks));
    }

    @Test
    public void testDynamicOrderChange() {
        // Acceptance criterion: CAMBIO ORDINE IN CORSO
        // Setup initial list: [A, B, C]
        List<IBrano> tracks = new ArrayList<>();
        IBrano a = new TestBrano("A", 100);
        IBrano b = new TestBrano("B", 100);
        IBrano c = new TestBrano("C", 100);
        tracks.add(a);
        tracks.add(b);
        tracks.add(c);

        // User starts playing sequentially
        SequentialIterator iterator = new SequentialIterator(tracks);
        
        // 1. Play first song (A)
        assertEquals(a, iterator.next());
        
        // 2. Play second song (B)
        assertEquals(b, iterator.next());
        
        // Currently playing: B. (Index of B is 1)
        // Now user moves C or adds D below B.
        // New list: [A, B, D, C]
        List<IBrano> updatedTracks = new ArrayList<>();
        IBrano d = new TestBrano("D", 100);
        updatedTracks.add(a);
        updatedTracks.add(b);
        updatedTracks.add(d);
        updatedTracks.add(c);

        // The system updates the execution queue:
        SequentialIterator newIterator = new SequentialIterator(updatedTracks);
        newIterator.impostaBranoCorrente(b); // sets currentIndex to 1 (B's new position)

        // 3. Next song played should be D, followed by C, then stops
        assertTrue(newIterator.hasNext());
        assertEquals(d, newIterator.next());
        
        assertTrue(newIterator.hasNext());
        assertEquals(c, newIterator.next());
        
        assertFalse(newIterator.hasNext());
    }

    @Test
    public void testGestoreRiproduzioneEndOfPlaylist() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        gestore.setStato(new PlayingState());
        
        // Empty list iterator
        List<IBrano> tracks = new ArrayList<>();
        SequentialIterator iterator = new SequentialIterator(tracks);
        gestore.setIterator(iterator);
        
        // When playNext() is called on empty iterator
        gestore.playNext();
        
        // It must stop and transition to PausedState
        assertTrue(gestore.getStato() instanceof PausedState);
    }
}
