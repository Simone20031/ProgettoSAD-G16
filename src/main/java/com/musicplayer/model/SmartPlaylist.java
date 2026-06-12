package com.musicplayer.model;

import java.util.List;

public class SmartPlaylist extends Playlist implements LibreriaObserver {
    private FiltroRicerca filtro;
    private ICatalogo sorgenteDati;

    public SmartPlaylist(String id, String nome, FiltroRicerca filtro, ICatalogo sorgenteDati) {
        super(id, nome);
        this.filtro = filtro != null ? filtro : new FiltroRicerca();
        this.sorgenteDati = sorgenteDati;
        ricalcola();
    }

    public FiltroRicerca getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroRicerca filtro) {
        this.filtro = filtro != null ? filtro : new FiltroRicerca();
        ricalcola();
    }

    public ICatalogo getSorgenteDati() {
        return sorgenteDati;
    }

    public void setSorgenteDati(ICatalogo sorgenteDati) {
        this.sorgenteDati = sorgenteDati;
        ricalcola();
    }

    public void ricalcola() {
        rimuoviBrani(getBrani());
        if (sorgenteDati != null) {
            List<IBrano> filtrati = filtro.applica(sorgenteDati.getBrani());
            aggiungiBrani(filtrati);
        }
    }

    @Override
    public void onBranoAggiunto(IBrano brano) {
        if (brano instanceof Brano b) {
            if (filtro.corrisponde(b)) {
                ricalcola();
            }
        }
    }

    @Override
    public void onBranoEliminato(IBrano brano) {
        if (contieneBrano(brano)) {
            rimuoviBrano(brano);
        }
    }

    @Override
    public void onPlaylistAggiornata(Playlist playlist) {
        // Nessuna azione richiesta per l'aggiornamento di altre playlist
    }
}
