package com.musicplayer;

import java.util.List;
import java.util.NoSuchElementException;

public class LoopIterator implements PlaylistIterator {
    private final List<IBrano> brani;
    private int currentIndex = 0;

    public LoopIterator(List<IBrano> brani) {
        if (brani == null || brani.isEmpty()) {
            throw new IllegalArgumentException("La lista dei brani non può essere vuota.");
        }
        this.brani = brani;
    }

    @Override
    public boolean hasNext() {
        return !brani.isEmpty();
    }

    @Override
    public IBrano next() {
        if (!hasNext()) {
            throw new NoSuchElementException("La playlist è vuota.");
        }
        IBrano scelto = brani.get(currentIndex);
        currentIndex = (currentIndex + 1) % brani.size();
        return scelto;
    }

    public void impostaBranoCorrente(IBrano brano) {
        int index = brani.indexOf(brano);
        if (index != -1) {
            // Impostiamo il currentIndex al prossimo brano della sequenza
            currentIndex = (index + 1) % brani.size();
        }
    }
}
