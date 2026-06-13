package com.musicplayer.model;

public enum CampoOrdinamento {
    TITOLO("Titolo"),
    AUTORE("Autore"),
    ANNO("Anno"),
    GENERE("Genere"),
    TAG("Tag");

    private final String etichetta;

    CampoOrdinamento(String etichetta) {
        this.etichetta = etichetta;
    }

    public String getEtichetta() {
        return etichetta;
    }
}
