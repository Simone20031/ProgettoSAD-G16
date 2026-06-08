package com.musicplayer;

import java.util.Iterator;
import java.util.List;

public interface PlaylistIterator extends Iterator<IBrano> {
    void reset();
    void impostaBranoCorrente(IBrano brano);
    IBrano peekNext();
    List<IBrano> getCodaBrani(int maxElements);
    List<IBrano> getBrani();
}
