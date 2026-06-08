package com.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ShuffleIterator implements PlaylistIterator {
    private final List<IBrano> brani;
    private final List<IBrano> braniRiprodotti = new ArrayList<>();
    private final ShuffleStrategy strategy = new ShuffleStrategy();
    private int currentIdx = -1;

    public ShuffleIterator(List<IBrano> brani) {
        if (brani == null) {
            throw new IllegalArgumentException("La lista dei brani non può essere nulla.");
        }
        this.brani = brani;
    }

    private List<Integer> getIndicesRiprodotti() {
        List<Integer> list = new ArrayList<>();
        for (IBrano b : braniRiprodotti) {
            int idx = brani.indexOf(b);
            if (idx != -1) {
                list.add(idx);
            }
        }
        return list;
    }

    @Override
    public boolean hasNext() {
        if (brani.isEmpty()) {
            return false;
        }
        List<Integer> riprodotti = getIndicesRiprodotti();
        int next = strategy.prossimoIndice(currentIdx, brani.size(), riprodotti);
        return next != -1;
    }

    @Override
    public IBrano next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Tutti i brani della playlist sono stati riprodotti.");
        }
        List<Integer> riprodotti = getIndicesRiprodotti();
        int next = strategy.prossimoIndice(currentIdx, brani.size(), riprodotti);
        
        IBrano scelto = brani.get(next);
        braniRiprodotti.add(scelto);
        currentIdx = next;
        return scelto;
    }

    @Override
    public void reset() {
        braniRiprodotti.clear();
        currentIdx = -1;
    }

    @Override
    public void impostaBranoCorrente(IBrano brano) {
        int idx = brani.indexOf(brano);
        if (idx != -1) {
            if (!braniRiprodotti.contains(brano)) {
                braniRiprodotti.add(brano);
            }
            currentIdx = idx;
        }
    }

    @Override
    public IBrano peekNext() {
        if (brani.isEmpty()) {
            return null;
        }
        List<Integer> riprodotti = getIndicesRiprodotti();
        int next = strategy.prossimoIndice(currentIdx, brani.size(), riprodotti);
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
        List<Integer> riprodotti = getIndicesRiprodotti();
        for (int i = 0; i < brani.size(); i++) {
            if (!riprodotti.contains(i) && i != currentIdx) {
                coda.add(brani.get(i));
                if (coda.size() >= maxElements) {
                    break;
                }
            }
        }
        return coda;
    }

    @Override
    public List<IBrano> getBrani() {
        return brani;
    }

    public void forzaRiproduzione(IBrano brano) {
        impostaBranoCorrente(brano);
    }

    public List<IBrano> getRiprodotti() {
        return new ArrayList<>(braniRiprodotti);
    }
}
