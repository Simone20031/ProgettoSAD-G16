package com.musicplayer.state;

import com.musicplayer.controller.*;


public interface PlayerState {
    void premiPlay(GestoreRiproduzione ctx);
    void premiPausa(GestoreRiproduzione ctx);
    void premiStop(GestoreRiproduzione ctx);
    void premiSkipAvanti(GestoreRiproduzione ctx);
    void premiSkipIndietro(GestoreRiproduzione ctx);
}
