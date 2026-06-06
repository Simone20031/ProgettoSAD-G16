package com.musicplayer;

public interface PlayerState {
    void premiPlay(GestoreRiproduzione ctx);
    void premiPausa(GestoreRiproduzione ctx);
    void premiStop(GestoreRiproduzione ctx);
}
