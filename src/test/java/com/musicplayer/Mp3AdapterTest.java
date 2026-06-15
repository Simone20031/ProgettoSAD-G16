package com.musicplayer;

import com.musicplayer.adapter.Mp3Adapter;
import com.musicplayer.adapter.Mp3LibFile;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class Mp3AdapterTest {

    // Simple implementation of Mp3LibFile for testing purposes
    private static class StubMp3LibFile implements Mp3LibFile {
        private String filename;
        private int length;
        private String artist;
        private String title;

        public StubMp3LibFile(String filename, int length, String artist, String title) {
            this.filename = filename;
            this.length = length;
            this.artist = artist;
            this.title = title;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public int getLengthInSeconds() {
            return length;
        }

        @Override
        public String getArtistName() {
            return artist;
        }

        @Override
        public String getTrackTitle() {
            return title;
        }
    }

    @Test
    public void testMp3AdapterDelegation() {
        // Arrange
        StubMp3LibFile stubFile = new StubMp3LibFile("song.mp3", 240, "Linkin Park", "In The End");
        Mp3Adapter adapter = new Mp3Adapter(stubFile);

        // Act & Assert
        assertEquals(240, adapter.getDurata(), "getDurata should return the adapted file's duration");
        assertEquals(240, adapter.getDurataTotale(), "getDurataTotale should match getDurata");
        assertEquals(stubFile, adapter.getMp3File(), "Should allow retrieval of the wrapped mp3File");
    }

    @Test
    public void testMp3AdapterPlayCount() {
        // Arrange
        StubMp3LibFile stubFile = new StubMp3LibFile("song.mp3", 240, "Linkin Park", "In The End");
        Mp3Adapter adapter = new Mp3Adapter(stubFile);

        // Act & Assert
        assertEquals(0, adapter.getPlayCount(), "Initial play count should be 0");
        adapter.incrementPlayCount();
        assertEquals(1, adapter.getPlayCount(), "Play count should increment to 1");
    }

    @Test
    public void testMp3AdapterConstructorNullCheck() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mp3Adapter(null);
        });
    }
}
