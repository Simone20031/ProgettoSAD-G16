package com.musicplayer.model;

/**
 * PlaylistException: lanciata per violazioni delle regole di business
 * sulle playlist (nome duplicato, rinomina di SmartPlaylist, nome vuoto, ecc.).
 *
 * Separata da ValidazioneException perché non riguarda i campi di un brano
 * ma le operazioni sulle playlist come entità di dominio.
 */
public class PlaylistException extends Exception {

    public enum TipoPlaylist {
        NOME_DUPLICATO,          // esiste già una playlist con quel nome
        NOME_VUOTO,              // nome della playlist non inserito
        SMART_PLAYLIST_PROTETTA, // operazione non consentita su SmartPlaylist
        GENERICO
    }

    private final TipoPlaylist tipo;

    public PlaylistException(String messaggio) {
        super(messaggio);
        this.tipo = TipoPlaylist.GENERICO;
    }

    public PlaylistException(String messaggio, TipoPlaylist tipo) {
        super(messaggio);
        this.tipo = tipo;
    }

    public TipoPlaylist getTipo() { return tipo; }
}
