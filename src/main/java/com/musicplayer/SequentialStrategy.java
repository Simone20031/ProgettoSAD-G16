package com.musicplayer;

import java.util.List;

public class SequentialStrategy implements PlaybackStrategy {
    @Override
    public int prossimoIndice(int corrente, int totale, List<Integer> riprodotti) {
        if (totale <= 0) {
            return -1;
        }
        int next = corrente + 1;
        if (next >= totale) {
            return -1; // Stops at the end of the playlist
        }
        return next;
    }
}
