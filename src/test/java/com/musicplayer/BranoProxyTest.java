package com.musicplayer;

import com.musicplayer.model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class BranoProxyTest {

    @Test
    public void testBranoProxyLazyLoading() {
        // Arrange
        Brano realBrano = new Brano("id-123", "Title", "Author", "Rock", 2020, "path/to/file.mp3", 180, Tag.PREFERITI);
        BranoProxy proxy = new BranoProxy(realBrano);

        assertFalse(proxy.isDettagliCaricati(), "Details should not be loaded initially");

        // Act & Assert for Titolo
        assertEquals("Title", proxy.getTitolo(), "Title should match");
        assertTrue(proxy.isDettagliCaricati(), "Details should be loaded after calling getTitolo");

        // Reset details flag for further lazy tests
        // (Note: proxy does not have reset, so we recreate it to test other triggers)
        BranoProxy proxyDurata = new BranoProxy(realBrano);
        assertFalse(proxyDurata.isDettagliCaricati());
        assertEquals(180, proxyDurata.getDurata());
        assertTrue(proxyDurata.isDettagliCaricati());

        BranoProxy proxyDettagli = new BranoProxy(realBrano);
        assertFalse(proxyDettagli.isDettagliCaricati());
        Map<String, String> dettagli = proxyDettagli.getDettagli();
        assertEquals("id-123", dettagli.get("id"));
        assertTrue(proxyDettagli.isDettagliCaricati());
    }

    @Test
    public void testBranoProxyDelegation() {
        // Arrange
        Brano realBrano = new Brano("id-123", "Title", "Author", "Rock", 2020, "path/to/file.mp3", 180, Tag.PREFERITI);
        BranoProxy proxy = new BranoProxy(realBrano);

        assertEquals(0, proxy.getPlayCount());
        proxy.incrementPlayCount();
        assertEquals(1, proxy.getPlayCount());
        assertEquals(1, realBrano.getPlayCount());
        assertEquals(180, proxy.getDurataTotale());
    }

    @Test
    public void testBranoProxyConstructorNullCheck() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BranoProxy(null);
        });
    }
}
