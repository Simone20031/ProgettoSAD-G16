package com.musicplayer;

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
}
