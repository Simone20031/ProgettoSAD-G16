package com.musicplayer.model;



/**
 * Genere: categorie musicali associabili a un brano.
 * Dal diagramma: «enumeration» Genere.
 */
public enum Genere {
    NESSUNO(""),
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSICA("Classica"),
    METAL("Metal"),
    ELETTRONICA("Elettronica"),
    HIP_HOP("Hip Hop"),
    RNB("R&B"),
    COUNTRY("Country"),
    REGGAE("Reggae"),
    BLUES("Blues"),
    FOLK("Folk"),
    ALTRO("Altro");

    private final String etichetta;

    Genere(String etichetta) {
        this.etichetta = etichetta;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public static Genere fromString(String s) {
        if (s == null || s.isBlank()) return NESSUNO;
        String trimmed = s.trim();
        for (Genere g : values()) {
            if (g.name().equalsIgnoreCase(trimmed) || g.etichetta.equalsIgnoreCase(trimmed)) {
                return g;
            }
        }
        return ALTRO;
    }

    @Override
    public String toString() {
        return etichetta.isEmpty() ? name() : etichetta;
    }
}
