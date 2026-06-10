package com.musicplayer.state;

import com.musicplayer.controller.*;


public class StoppedState implements PlayerState {

    @Override
    public void premiPlay(GestoreRiproduzione ctx) {
        ctx.eseguiPlay(); 
        ctx.setStato(new PlayingState());
    }

    @Override
    public void premiPausa(GestoreRiproduzione ctx) {
        // Da fermo non c'è transizione a pausa
    }

    @Override
    public void premiStop(GestoreRiproduzione ctx) {
        // Già fermo, nessuna azione
    }

    @Override
    public void premiSkipAvanti(GestoreRiproduzione ctx) {
        ctx.playNext();
    }

    @Override
    public void premiSkipIndietro(GestoreRiproduzione ctx) {
        ctx.playPrevious();
    }
}
