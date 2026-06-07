package com.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class ShuffleIterator implements PlaylistIterator {
    private final List<IBrano> brani;
    private final List<IBrano> riprodotti = new ArrayList<>();
    private final Random random = new Random();

    public ShuffleIterator(List<IBrano> brani) {
        if (brani == null) {
            throw new IllegalArgumentException("La lista dei brani non può essere nulla.");
        }
        this.brani = brani;
    }

    @Override
    public boolean hasNext() {
        return !getNonRiprodotti().isEmpty();
    }

    @Override
    public IBrano next() {
        List<IBrano> nonRiprodotti = getNonRiprodotti();
        if (nonRiprodotti.isEmpty()) {
            throw new NoSuchElementException("Tutti i brani della playlist sono stati riprodotti.");
        }
        int index = random.nextInt(nonRiprodotti.size());
        IBrano scelto = nonRiprodotti.get(index);
        riprodotti.add(scelto);
        return scelto;
    }

    private List<IBrano> getNonRiprodotti() {
        // Mantiene in memoria solo i brani ancora presenti nella playlist
        riprodotti.retainAll(brani);

        List<IBrano> nonRiprodotti = new ArrayList<>();
        for (IBrano b : brani) {
            if (!riprodotti.contains(b)) {
                nonRiprodotti.add(b);
            }
        }
        return nonRiprodotti;
    }

    public void reset() {
        riprodotti.clear();
    }

    public List<IBrano> getRiprodotti() {
        return new ArrayList<>(riprodotti);
    }
}
