package com.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShuffleStrategy implements PlaybackStrategy {
    private final Random random = new Random();

    @Override
    public int prossimoIndice(int corrente, int totale, List<Integer> riprodotti) {
        if (totale <= 0) {
            return -1;
        }

        List<Integer> nonRiprodotti = new ArrayList<>();
        for (int i = 0; i < totale; i++) {
            if (riprodotti == null || !riprodotti.contains(i)) {
                nonRiprodotti.add(i);
            }
        }

        if (nonRiprodotti.isEmpty()) {
            return -1;
        }

        return nonRiprodotti.get(random.nextInt(nonRiprodotti.size()));
    }
}
