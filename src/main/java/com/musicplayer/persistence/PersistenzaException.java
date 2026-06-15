package com.musicplayer.persistence;

/**
 * PersistenzaException: lanciata per errori di accesso al filesystem
 * (lettura/scrittura CSV, copia file audio, backup mancante).
 *
 * Sostituisce il pattern scorretto in cui IOException veniva wrappata
 * in ValidazioneException nei command, solo per soddisfare la firma
 * dell'interfaccia Command. "File non trovato su disco" non è
 * "dato non valido".
 */
public class PersistenzaException extends Exception {

    public enum TipoPersistenza {
        FILE_NON_TROVATO,    // file audio o CSV mancante
        ERRORE_SCRITTURA,    // impossibile scrivere su disco
        BACKUP_NON_TROVATO,  // backup assente durante operazione undo
        GENERICO
    }

    private final TipoPersistenza tipo;

    public PersistenzaException(String messaggio) {
        super(messaggio);
        this.tipo = TipoPersistenza.GENERICO;
    }

    public PersistenzaException(String messaggio, TipoPersistenza tipo) {
        super(messaggio);
        this.tipo = tipo;
    }

    public PersistenzaException(String messaggio, TipoPersistenza tipo, Throwable cause) {
        super(messaggio, cause);
        this.tipo = tipo;
    }

    public TipoPersistenza getTipo() { return tipo; }
}
