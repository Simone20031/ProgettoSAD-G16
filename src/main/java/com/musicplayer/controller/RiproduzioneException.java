package com.musicplayer.controller;

/**
 * RiproduzioneException: lanciata quando un'operazione non è consentita
 * perché è in corso la riproduzione di una traccia.
 *
 * Separata da ValidazioneException perché non si tratta di un errore
 * sui dati del brano, ma di un vincolo di stato dell'applicazione.
 */
public class RiproduzioneException extends Exception {

    public enum TipoRiproduzione {
        TRACCIA_IN_RIPRODUZIONE,  // operazione bloccata dalla traccia corrente
        GENERICO
    }

    private final TipoRiproduzione tipo;

    public RiproduzioneException(String messaggio) {
        super(messaggio);
        this.tipo = TipoRiproduzione.GENERICO;
    }

    public RiproduzioneException(String messaggio, TipoRiproduzione tipo) {
        super(messaggio);
        this.tipo = tipo;
    }

    public TipoRiproduzione getTipo() { return tipo; }
}
