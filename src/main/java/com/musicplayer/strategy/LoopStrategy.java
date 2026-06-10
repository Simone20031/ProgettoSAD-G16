package com.musicplayer.strategy;



import java.util.List;

public class LoopStrategy implements PlaybackStrategy {
    @Override
    public int prossimoIndice(int corrente, int totale, List<Integer> riprodotti) {
        if (totale <= 0) {
            return -1;
        }
        return (corrente + 1) % totale;
    }

    @Override
    public int indicePrecedente(int corrente, int totale, List<Integer> riprodotti) {
        if (totale <= 0) {
            return -1;
        }
        if (corrente <= 0) {
            return totale - 1;
        }
        return corrente - 1;
    }
}
