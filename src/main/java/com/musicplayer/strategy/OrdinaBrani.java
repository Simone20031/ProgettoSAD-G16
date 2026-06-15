package com.musicplayer.strategy;

import com.musicplayer.model.Brano;
import com.musicplayer.model.CampoOrdinamento;
import com.musicplayer.model.IBrano;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrdinaBrani implements OrdinamentoStrategy {

    @Override
    public void ordina(List<IBrano> brani, CampoOrdinamento campo, boolean crescente) {
        Comparator<IBrano> comparator = (b1, b2) -> {
            if (!(b1 instanceof Brano brano1) || !(b2 instanceof Brano brano2)) {
                return 0;
            }

            int result = 0;
            switch (campo) {
                case TITOLO:
                    result = compareStrings(brano1.getTitolo(), brano2.getTitolo());
                    break;
                case AUTORE:
                    result = compareStrings(brano1.getAutore(), brano2.getAutore());
                    break;
                case ANNO:
                    result = Integer.compare(brano1.getAnno(), brano2.getAnno());
                    break;
                case GENERE:
                    String gen1 = brano1.getGenereEnum() != null ? brano1.getGenereEnum().getEtichetta() : "";
                    String gen2 = brano2.getGenereEnum() != null ? brano2.getGenereEnum().getEtichetta() : "";
                    result = compareStrings(gen1, gen2);
                    break;
                case TAG:
                    String tag1 = brano1.getTag() != null ? brano1.getTag().getEtichetta() : "";
                    String tag2 = brano2.getTag() != null ? brano2.getTag().getEtichetta() : "";
                    result = compareStrings(tag1, tag2);
                    break;
            }

            return crescente ? result : -result;
        };

        Collections.sort(brani, comparator);
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareToIgnoreCase(s2);
    }
}
