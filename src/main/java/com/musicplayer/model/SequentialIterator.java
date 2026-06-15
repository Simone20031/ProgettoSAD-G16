package com.musicplayer.model;

import com.musicplayer.strategy.*;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SequentialIterator implements PlaylistIterator {
    private final List<IBrano> brani;
    private int indice = -1;
    private final SequentialStrategy strategy = new SequentialStrategy();

    public SequentialIterator(List<IBrano> brani) {
        if (brani == null) {
            throw new IllegalArgumentException("La lista dei brani non può essere nulla.");
        }
        this.brani = brani;
    }

    @Override
    public boolean hasNext() {
        if (brani.isEmpty()) {
            return false;
        }
        int next = strategy.prossimoIndice(indice, brani.size(), null);
        return next != -1;
    }

    @Override
    public IBrano next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Nessun brano successivo.");
        }
        indice = strategy.prossimoIndice(indice, brani.size(), null);
        return brani.get(indice);
    }

    @Override
    public boolean hasPrevious() {
        return !brani.isEmpty() && indice > 0;
    }

    @Override
    public IBrano previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException("Nessun brano precedente.");
        }
        indice--;
        return brani.get(indice);
    }

    @Override
    public void reset() {
        this.indice = -1;
    }

    @Override
    public void impostaBranoCorrente(IBrano brano) {
        int idx = brani.indexOf(brano);
        if (idx != -1) {
            this.indice = idx;
        }
    }

    @Override
    public IBrano peekNext() {
        if (brani.isEmpty()) {
            return null;
        }
        int next = strategy.prossimoIndice(indice, brani.size(), null);
        if (next != -1 && next >= 0 && next < brani.size()) {
            return brani.get(next);
        }
        return null;
    }

    @Override
    public List<IBrano> getCodaBrani(int maxElements) {
        List<IBrano> coda = new ArrayList<>();
        if (brani.isEmpty()) {
            return coda;
        }
        int tempIndex = indice;
        for (int k = 0; k < maxElements; k++) {
            int nextIdx = strategy.prossimoIndice(tempIndex, brani.size(), null);
            if (nextIdx == -1) {
                break;
            }
            coda.add(brani.get(nextIdx));
            if (nextIdx == indice) {
                break;
            }
            tempIndex = nextIdx;
        }
        return coda;
    }

    @Override
    public List<IBrano> getBrani() {
        return brani;
    }
}
