package com.musicplayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FiltroRicercaTest {

    private List<IBrano> catalogo;
    private Brano branoRock;
    private Brano branoJazz;
    private Brano branoRock2000;
    private Brano branoPop;
    private Brano branoPopRock;

    @BeforeEach
    void setUp() throws Exception {
        catalogo = new ArrayList<>();
        branoRock = new Brano("1", "Thunderstruck", "AC/DC", "Rock", 1990, "path1", 292, Tag.PREFERITI);
        catalogo.add(branoRock);

        branoJazz = new Brano("2", "Blue Note", "Miles Davis", "Jazz", 1958, "path2", 300, Tag.RELAX);
        catalogo.add(branoJazz);

        branoRock2000 = new Brano("3", "In The End", "Linkin Park", "Rock", 2000, "path3", 216, Tag.NESSUNO);
        catalogo.add(branoRock2000);

        branoPop = new Brano("4", "Shake It Off", "Taylor Swift", "Pop", 2014, "path4", 219, Tag.PREFERITI);
        catalogo.add(branoPop);

        branoPopRock = new Brano("5", "Yellow", "Coldplay", "Pop", 2000, "path5", 269, Tag.PREFERITI);
        catalogo.add(branoPopRock);
    }

    @Test
    void testFiltroVuotoRestituisceTutto() {
        FiltroRicerca f = new FiltroRicerca();
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(5, risultati.size());
    }

    @Test
    void testFiltroPerTitolo() {
        FiltroRicerca f = new FiltroRicerca("Thunderstruck", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock));
    }

    @Test
    void testFiltroPerTitoloSubstring() {
        FiltroRicerca f = new FiltroRicerca("note", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoJazz));
    }

    @Test
    void testFiltroPerTitoloSubstringMultiplaCorrispondenza() {
        FiltroRicerca f = new FiltroRicerca("in", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertTrue(risultati.contains(branoRock2000));
    }

    @Test
    void testFiltroPerAutore() {
        FiltroRicerca f = new FiltroRicerca("", "AC/DC", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock));
    }

    @Test
    void testFiltroPerAutoreSubstringCaseInsensitive() {
        FiltroRicerca f = new FiltroRicerca("", "ac/dc", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock));
    }

    @Test
    void testFiltroPerAnno() {
        FiltroRicerca f = new FiltroRicerca("", "", 2000, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(2, risultati.size());
        assertTrue(risultati.contains(branoRock2000));
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroPerGenere() {
        FiltroRicerca f = new FiltroRicerca("", "", 0, Genere.ROCK, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(2, risultati.size());
        assertTrue(risultati.contains(branoRock));
        assertTrue(risultati.contains(branoRock2000));
    }

    @Test
    void testFiltroPerGenereEnum() {
        FiltroRicerca f = new FiltroRicerca("", "", 0, Genere.POP, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(2, risultati.size());
        assertTrue(risultati.contains(branoPop));
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroPerTag() {
        FiltroRicerca f = new FiltroRicerca("", "", 0, Genere.NESSUNO, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(3, risultati.size());
        assertTrue(risultati.contains(branoRock));
        assertTrue(risultati.contains(branoPop));
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroCombinatoTitoloEAutore() {
        FiltroRicerca f = new FiltroRicerca("Thunderstruck", "AC/DC", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock));
    }

    @Test
    void testFiltroCombinatoTitoloEAutoreNessunRisultato() {
        FiltroRicerca f = new FiltroRicerca("Thunderstruck", "Linkin Park", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(0, risultati.size());
    }

    @Test
    void testFiltroCombinatoGenereEAnno() {
        FiltroRicerca f = new FiltroRicerca("", "", 2000, Genere.ROCK, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock2000));
    }

    @Test
    void testFiltroCombinatoGenereETag() {
        FiltroRicerca f = new FiltroRicerca("", "", 0, Genere.POP, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(2, risultati.size());
        assertTrue(risultati.contains(branoPop));
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroCombinatoAnnoETag() {
        FiltroRicerca f = new FiltroRicerca("", "", 2000, Genere.NESSUNO, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroCombinatoGenereETitolo() {
        FiltroRicerca f = new FiltroRicerca("Yellow", "", 0, Genere.POP, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testFiltroCombinatoGenereEAutore() {
        FiltroRicerca f = new FiltroRicerca("", "Taylor Swift", 0, Genere.POP, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoPop));
    }

    @Test
    void testTriplaCombinazioneGenereAnnoTag() {
        FiltroRicerca f = new FiltroRicerca("", "", 2000, Genere.POP, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testTriplaCombinazioneAutoreGenereTag() {
        FiltroRicerca f = new FiltroRicerca("", "AC/DC", 0, Genere.ROCK, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoRock));
    }

    @Test
    void testQuintuplaCombinazioneCorretta() {
        FiltroRicerca f = new FiltroRicerca("Yellow", "Coldplay", 2000, Genere.POP, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(1, risultati.size());
        assertTrue(risultati.contains(branoPopRock));
    }

    @Test
    void testQuintuplaCombinazioneSbagliatoUnParametro() {
        FiltroRicerca f = new FiltroRicerca("Yellow", "Coldplay", 1999, Genere.POP, Tag.PREFERITI);
        List<IBrano> risultati = f.applica(catalogo);
        assertEquals(0, risultati.size());
    }

    @Test
    void testCorrispondeBranoNullo() {
        FiltroRicerca f = new FiltroRicerca("Test", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        Brano b = new Brano("id", null, "Autore", "Rock", 2000, "path", 0, null);
        assertFalse(f.corrisponde(b));
    }

    @Test
    void testAutoreBranoNulloMaFiltroAttivo() {
        FiltroRicerca f = new FiltroRicerca("", "Autore", 0, Genere.NESSUNO, Tag.NESSUNO);
        Brano b = new Brano("id", "Titolo", null, "Rock", 2000, "path", 0, null);
        assertFalse(f.corrisponde(b));
    }

    @Test
    void testGenereBranoNulloMaFiltroAttivo() {
        FiltroRicerca f = new FiltroRicerca("", "", 0, Genere.ROCK, Tag.NESSUNO);
        Brano b = new Brano("id", "Titolo", "Autore", null, 2000, "path", 0, null);
        // Genere null → Genere.NESSUNO, quindi non matcha ROCK
        assertFalse(f.corrisponde(b));
    }

    @Test
    void testCorrispondeTuttoSenzaFiltri() {
        FiltroRicerca f = new FiltroRicerca();
        assertTrue(f.corrisponde(branoRock));
    }

    @Test
    void testResetFiltro() {
        FiltroRicerca f = new FiltroRicerca("Titolo", "Autore", 2020, Genere.ROCK, Tag.RELAX);
        assertFalse(f.isVuoto());
        f.reset();
        assertTrue(f.isVuoto());
    }

    @Test
    void testSettersGetters() {
        FiltroRicerca f = new FiltroRicerca();
        f.setTitolo("A");
        f.setAutore("B");
        f.setAnno(2000);
        f.setGenere(Genere.JAZZ);
        f.setTag(Tag.PREFERITI);

        assertEquals("A", f.getTitolo());
        assertEquals("B", f.getAutore());
        assertEquals(2000, f.getAnno());
        assertEquals(Genere.JAZZ, f.getGenere());
        assertEquals(Tag.PREFERITI, f.getTag());
    }

    @Test
    void testSettersConValoriNulliDiventanoDefault() {
        FiltroRicerca f = new FiltroRicerca();
        f.setTitolo(null);
        f.setAutore(null);
        f.setGenere(null);
        f.setTag(null);

        assertEquals("", f.getTitolo());
        assertEquals("", f.getAutore());
        assertEquals(Genere.NESSUNO, f.getGenere());
        assertEquals(Tag.NESSUNO, f.getTag());
    }

    @Test
    void testCostruttoreConValoriNulli() {
        FiltroRicerca f = new FiltroRicerca(null, null, 2020, null, null);
        assertEquals("", f.getTitolo());
        assertEquals("", f.getAutore());
        assertEquals(Genere.NESSUNO, f.getGenere());
        assertEquals(Tag.NESSUNO, f.getTag());
    }

    @Test
    void testApplicaListaVuota() {
        FiltroRicerca f = new FiltroRicerca("A", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> risultati = f.applica(new ArrayList<>());
        assertTrue(risultati.isEmpty());
    }

    @Test
    void testApplicaElementiNonBrano() {
        FiltroRicerca f = new FiltroRicerca("A", "", 0, Genere.NESSUNO, Tag.NESSUNO);
        List<IBrano> mista = new ArrayList<>();
        mista.add(branoRock); // non matcha "A"
        mista.add(new IBrano() { // elemento fittizio
            @Override
            public String getTitolo() {
                return "fake";
            }

            @Override
            public int getDurata() {
                return 0;
            }

            @Override
            public int getDurataTotale() {
                return getDurata();
            }

            @Override
            public java.util.Map<String, String> getDettagli() {
                return new java.util.HashMap<>();
            }
        });
        List<IBrano> risultati = f.applica(mista);
        assertTrue(risultati.isEmpty());
    }
}
