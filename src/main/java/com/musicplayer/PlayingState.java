package com.musicplayer;

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
}
