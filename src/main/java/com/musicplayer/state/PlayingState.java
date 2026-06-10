package com.musicplayer.state;

import com.musicplayer.controller.*;


public class PlayingState implements PlayerState {

    @Override
    public void premiPlay(GestoreRiproduzione ctx) {
        // Già in riproduzione, nessuna azione
    }

    @Override
    public void premiPausa(GestoreRiproduzione ctx) {
        ctx.eseguiPausa();
        ctx.setStato(new PausedState());
    }

    @Override
    public void premiStop(GestoreRiproduzione ctx) {
        ctx.eseguiStop();
        ctx.setStato(new StoppedState());
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
