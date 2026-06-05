package com.musicplayer;

/**
 * ValidazioneException: Gestisce l'errore specificando la causa esatta
 * e il componente che lo ha generato, senza toccare la grafica.
 */
public class ValidazioneException extends Exception {

    // Definiamo i tipi di errore
    public enum TipoErrore {
        CAMPO_MANCANTE,
        FORMATO_NON_VALIDO,
        GENERICO
    }

    private final TipoErrore tipo;
    private final String campoErrato; // Es: "titolo", "anno", "durata"

    // Costruttore base per compatibilità con il vecchio codice
    public ValidazioneException(String messaggio) {
        super(messaggio);
        this.tipo = TipoErrore.GENERICO;
        this.campoErrato = "";
    }

    // COSTRUTTORE COMPLETO: Usato per identificare l'errore e il campo
    public ValidazioneException(String messaggio, TipoErrore tipo, String campoErrato) {
        super(messaggio);
        this.tipo = tipo;
        this.campoErrato = campoErrato;
    }

    // Getter per la View
    public TipoErrore getTipo() { return tipo; }
    public String getCampoErrato() { return campoErrato; }
}