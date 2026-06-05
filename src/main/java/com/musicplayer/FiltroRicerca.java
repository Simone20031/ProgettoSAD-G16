package com.musicplayer;

import java.util.ArrayList;
import java.util.List;

public class FiltroRicerca {
    private String titolo;
    private String autore;
    private int anno;
    private Genere genere;
    private Tag tag;

    public FiltroRicerca() {
        reset();
    }

    public FiltroRicerca(String titolo, String autore, int anno, Genere genere, Tag tag) {
        this.titolo = titolo != null ? titolo : "";
        this.autore = autore != null ? autore : "";
        this.anno = anno;
        this.genere = genere != null ? genere : Genere.NESSUNO;
        this.tag = tag != null ? tag : Tag.NESSUNO;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo != null ? titolo : "";
    }

    public String getAutore() {
        return autore;
    }

    public void setAutore(String autore) {
        this.autore = autore != null ? autore : "";
    }

    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public Genere getGenere() {
        return genere;
    }

    public void setGenere(Genere genere) {
        this.genere = genere != null ? genere : Genere.NESSUNO;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag != null ? tag : Tag.NESSUNO;
    }

    public void reset() {
        this.titolo = "";
        this.autore = "";
        this.anno = 0;
        this.genere = Genere.NESSUNO;
        this.tag = Tag.NESSUNO;
    }

    public boolean isVuoto() {
        return titolo.isEmpty() && autore.isEmpty() && anno == 0
                && genere == Genere.NESSUNO && tag == Tag.NESSUNO;
    }

    public boolean corrisponde(Brano b) {
        if (!titolo.isEmpty()) {
            if (b.getTitolo() == null || !b.getTitolo().toLowerCase().contains(titolo.toLowerCase())) {
                return false;
            }
        }
        if (!autore.isEmpty()) {
            if (b.getAutore() == null || !b.getAutore().toLowerCase().contains(autore.toLowerCase())) {
                return false;
            }
        }
        if (anno != 0) {
            if (b.getAnno() != anno) {
                return false;
            }
        }
        if (genere != Genere.NESSUNO) {
            if (b.getGenereEnum() != genere) {
                return false;
            }
        }
        if (tag != Tag.NESSUNO) {
            if (b.getTag() != tag) {
                return false;
            }
        }
        return true;
    }

    public List<IBrano> applica(List<IBrano> brani) {
        if (isVuoto()) {
            return new ArrayList<>(brani);
        }
        List<IBrano> risultati = new ArrayList<>();
        for (IBrano ib : brani) {
            if (ib instanceof Brano b) {
                if (corrisponde(b)) {
                    risultati.add(ib);
                }
            }
        }
        return risultati;
    }
}