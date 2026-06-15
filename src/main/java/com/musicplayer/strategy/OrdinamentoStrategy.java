package com.musicplayer.strategy;

import com.musicplayer.model.IBrano;

import java.util.List;

/**
 * Interfaccia per la strategia di ordinamento dei brani.
 */
public interface OrdinamentoStrategy {
    void ordina(List<IBrano> brani);
}
