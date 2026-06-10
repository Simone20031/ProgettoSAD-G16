package com.musicplayer.model;

import com.musicplayer.strategy.*;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ShuffleIterator implements PlaylistIterator {
    private final List<IBrano> brani;
    private final List<IBrano> braniRiprodotti = new ArrayList<>();
    private final ShuffleStrategy strategy = new ShuffleStrategy();
    private int currentIdx = -1;
    private final List<Integer> cachedQueue = new ArrayList<>();

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
    
    private void fillCache(int minElements) {
        List<Integer> simulatiRiprodotti = getIndicesRiprodotti();
        simulatiRiprodotti.addAll(cachedQueue);
        
        int tempCurrentIdx = cachedQueue.isEmpty() ? currentIdx : cachedQueue.get(cachedQueue.size() - 1);
        
        while (cachedQueue.size() < minElements) {
            int next = strategy.prossimoIndice(tempCurrentIdx, brani.size(), simulatiRiprodotti);
            if (next == -1) {
                break;
            }
            cachedQueue.add(next);
            simulatiRiprodotti.add(next);
            tempCurrentIdx = next;
        }
    }

    @Override
    public boolean hasNext() {
        if (brani.isEmpty()) {
            return false;
        }
        fillCache(1);
        return !cachedQueue.isEmpty();
    }

    @Override
    public IBrano next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Tutti i brani della playlist sono stati riprodotti.");
        }
        int nextIdx = cachedQueue.remove(0);
        IBrano scelto = brani.get(nextIdx);
        braniRiprodotti.add(scelto);
        currentIdx = nextIdx;
        return scelto;
    }

    @Override
    public boolean hasPrevious() {
        return braniRiprodotti.size() > 1;
    }

    @Override
    public IBrano previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException("Nessun brano precedente nella cronologia.");
        }
        // Il brano attualmente in riproduzione è l'ultimo in braniRiprodotti.
        // Lo rimuoviamo e lo mettiamo all'inizio della coda futura (cachedQueue)
        IBrano current = braniRiprodotti.remove(braniRiprodotti.size() - 1);
        int currentSongIdx = brani.indexOf(current);
        if (currentSongIdx != -1) {
            cachedQueue.add(0, currentSongIdx);
        }

        // Il nuovo brano corrente è ora l'ultimo in braniRiprodotti (cioè quello precedente)
        IBrano prev = braniRiprodotti.get(braniRiprodotti.size() - 1);
        currentIdx = brani.indexOf(prev);
        
        return prev;
    }

    @Override
    public void reset() {
        braniRiprodotti.clear();
        cachedQueue.clear();
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
            cachedQueue.clear();
        }
    }

    @Override
    public IBrano peekNext() {
        if (!hasNext()) {
            return null;
        }
        return brani.get(cachedQueue.get(0));
    }

    @Override
    public List<IBrano> getCodaBrani(int maxElements) {
        List<IBrano> coda = new ArrayList<>();
        if (brani.isEmpty() || maxElements <= 0) {
            return coda;
        }
        fillCache(maxElements);
        for (int i = 0; i < Math.min(maxElements, cachedQueue.size()); i++) {
            coda.add(brani.get(cachedQueue.get(i)));
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
