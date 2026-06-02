package com.musicplayer;

/**
 * Tag: etichette predefinite associabili a un brano.
 * Dal diagramma: «enumeration» Tag.
 */
public enum Tag {
    NESSUNO(""),
    PREFERITI("Preferiti"),
    NUOVA_USCITA("Nuova uscita"),
    RELAX("Relax"),
    CONCENTRAZIONE("Concentrazione"),
    ENERGIA("Energia"),
    MALINCONIA("Malinconia"),
    MACCHINA("Macchina"),
    VINTAGE("Vintage"),
    UNDERGROUND("Underground");

    private final String etichetta;

    Tag(String etichetta) {
        this.etichetta = etichetta;
    }

    public String getEtichetta() {
        return etichetta;
    }

    /**
     * Converte una stringa nel Tag corrispondente.
     * Se rileva una stringa multipla (con virgole), estrae il primo tag per compatibilità col modello.
     */
    public static Tag fromString(String s) {
        if (s == null || s.isBlank()) return NESSUNO;
        
        // Se ci sono virgole, prendiamo solo il primo elemento per l'istanza dell'enum atomica
        String trimmed = s.contains(",") ? s.split(",")[0].trim() : s.trim();
        
        for (Tag t : values()) {
            if (t.name().equalsIgnoreCase(trimmed) || t.etichetta.equalsIgnoreCase(trimmed)) {
                return t;
            }
        }
        return NESSUNO;
    }

    @Override
    public String toString() {
        return etichetta.isEmpty() ? name() : etichetta;
    }
}