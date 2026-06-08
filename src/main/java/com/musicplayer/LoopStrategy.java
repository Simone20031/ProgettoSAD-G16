package com.musicplayer;

import java.util.List;

public class LoopStrategy implements PlaybackStrategy {
    @Override
    public int prossimoIndice(int corrente, int totale, List<Integer> riprodotti) {
        if (totale <= 0) {
            return -1;
        }
        return (corrente + 1) % totale;
    }
}
