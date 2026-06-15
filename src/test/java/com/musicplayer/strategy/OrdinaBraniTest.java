package com.musicplayer.strategy;

import com.musicplayer.model.Brano;
import com.musicplayer.model.CampoOrdinamento;
import com.musicplayer.model.IBrano;
import com.musicplayer.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrdinaBraniTest {

    private OrdinaBrani ordinaBrani;
    private List<IBrano> brani;

    @BeforeEach
    public void setUp() {
        ordinaBrani = new OrdinaBrani();
        brani = new ArrayList<>();
        
        brani.add(new Brano("1", "Zebra", "BAutore", "Pop", 2020, "/path/1", 100, Tag.NESSUNO));
        brani.add(new Brano("2", "Apple", "CAutore", "Rock", 2018, "/path/2", 100, Tag.PREFERITI));
        brani.add(new Brano("3", "Mango", "AAutore", "Jazz", 2022, "/path/3", 100, Tag.NESSUNO));
    }

    @Test
    public void testOrdinaPerTitoloCrescente() {
        ordinaBrani.ordina(brani, CampoOrdinamento.TITOLO, true);
        assertEquals("Apple", brani.get(0).getTitolo());
        assertEquals("Mango", brani.get(1).getTitolo());
        assertEquals("Zebra", brani.get(2).getTitolo());
    }

    @Test
    public void testOrdinaPerTitoloDecrescente() {
        ordinaBrani.ordina(brani, CampoOrdinamento.TITOLO, false);
        assertEquals("Zebra", brani.get(0).getTitolo());
        assertEquals("Mango", brani.get(1).getTitolo());
        assertEquals("Apple", brani.get(2).getTitolo());
    }

    @Test
    public void testOrdinaPerAutoreCrescente() {
        ordinaBrani.ordina(brani, CampoOrdinamento.AUTORE, true);
        assertEquals("AAutore", ((Brano)brani.get(0)).getAutore());
        assertEquals("BAutore", ((Brano)brani.get(1)).getAutore());
        assertEquals("CAutore", ((Brano)brani.get(2)).getAutore());
    }

    @Test
    public void testOrdinaPerAnnoCrescente() {
        ordinaBrani.ordina(brani, CampoOrdinamento.ANNO, true);
        assertEquals(2018, ((Brano)brani.get(0)).getAnno());
        assertEquals(2020, ((Brano)brani.get(1)).getAnno());
        assertEquals(2022, ((Brano)brani.get(2)).getAnno());
    }

    @Test
    public void testOrdinaPerTagDecrescente() {
        ordinaBrani.ordina(brani, CampoOrdinamento.TAG, false);
        assertEquals(Tag.PREFERITI, ((Brano)brani.get(0)).getTag());
        assertEquals(Tag.NESSUNO, ((Brano)brani.get(1)).getTag());
    }
}
