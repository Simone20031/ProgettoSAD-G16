package com.musicplayer.state;

import com.musicplayer.controller.*;


public class PausedState implements PlayerState {

    @Override
    public void premiPlay(GestoreRiproduzione ctx) {
        ctx.eseguiPlay();
        ctx.setStato(new PlayingState());
    }

    @Override
    public void premiPausa(GestoreRiproduzione ctx) {
        // Già in pausa, nessuna azione
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
