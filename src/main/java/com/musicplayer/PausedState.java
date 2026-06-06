package com.musicplayer;

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
}
