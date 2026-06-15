package com.musicplayer.strategy;

import com.musicplayer.model.Brano;
import com.musicplayer.model.IBrano;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrdinaTitoloDesc implements OrdinamentoStrategy {

    @Override
    public void ordina(List<IBrano> brani) {
        Comparator<IBrano> comparator = (b1, b2) -> {
            if (!(b1 instanceof Brano brano1) || !(b2 instanceof Brano brano2)) {
                return 0;
            }

            int result = compare(brano1.getTitolo(), brano2.getTitolo());
            return -result;
        };

        Collections.sort(brani, comparator);
    }

    private int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        
        if (o1 instanceof String s1 && o2 instanceof String s2) {
            return s1.compareToIgnoreCase(s2);
        }
        if (o1 instanceof Integer i1 && o2 instanceof Integer i2) {
            return i1.compareTo(i2);
        }
        return 0;
    }
}
