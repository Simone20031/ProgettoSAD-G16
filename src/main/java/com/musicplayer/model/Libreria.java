package com.musicplayer.model;




import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.musicplayer.strategy.OrdinamentoStrategy;

/**
 * Libreria: collezione centrale di brani e playlist.
 * Pattern: Singleton + Observer.
 */
public class Libreria implements ICatalogo {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static Libreria instance;

    private final List<IBrano> catalogo = new ArrayList<>();
    private final List<IBrano> catalogoOriginale = new ArrayList<>();

    private CampoOrdinamento ultimoCampoOrdinamento = null;
    private boolean ultimoOrdineCrescente = true;

    public static OrdinamentoStrategy getStrategyFor(CampoOrdinamento campo, boolean crescente) {
        if (campo == null) return null;
        switch (campo) {
            case TITOLO: return crescente ? new com.musicplayer.strategy.OrdinaTitoloAsc() : new com.musicplayer.strategy.OrdinaTitoloDesc();
            case AUTORE: return crescente ? new com.musicplayer.strategy.OrdinaAutoreAsc() : new com.musicplayer.strategy.OrdinaAutoreDesc();
            case ANNO: return crescente ? new com.musicplayer.strategy.OrdinaAnnoAsc() : new com.musicplayer.strategy.OrdinaAnnoDesc();
            case GENERE: return crescente ? new com.musicplayer.strategy.OrdinaGenereAsc() : new com.musicplayer.strategy.OrdinaGenereDesc();
            case TAG: return crescente ? new com.musicplayer.strategy.OrdinaTagAsc() : new com.musicplayer.strategy.OrdinaTagDesc();
        }
        return null;
    }

    private Libreria() {
    }

    public static Libreria getInstance() {
        if (instance == null)
            instance = new Libreria();
        return instance;
    }

    // ── Gestione brani ────────────────────────────────────────────────────────

    public void aggiungiBrano(IBrano b) {
        if (b == null || catalogo.contains(b))
            return;
        catalogo.add(b);
        catalogoOriginale.add(b);
        // observer disabilitato in questo momento
    }

    public void aggiungiBrani(java.util.Collection<? extends Playable> brani) {
        if (brani == null) return;
        java.util.Set<IBrano> set = new java.util.HashSet<>(catalogo);
        for (Playable p : brani) {
            if (p instanceof IBrano b) {
                if (b != null && !set.contains(b)) {
                    catalogo.add(b);
                    catalogoOriginale.add(b);
                    set.add(b);
                }
            }
        }
    }

    public void modificaBrano(IBrano b, Map<String, String> dati) throws ValidazioneException {
        if (b instanceof Brano brano) {
            brano.setDettagli(dati);
            // observer disabilitato in questo momento
        }

    }

    /**
     * Rimuove il brano dal catalogo in memoria RAM.
     */
    public void eliminaBrano(IBrano b) {
        if (b == null)
            return;

        // Basta una sola riga per rimuovere l'oggetto
        catalogo.remove(b);
        catalogoOriginale.remove(b);

        // Non aggiungere altro qui, la logica di pulizia delle playlist
        // la stiamo gestendo direttamente nel LibreriaController
        // per mantenere il modello Libreria pulito e semplice.
    }

    public void eliminaBrani(java.util.Collection<? extends Playable> brani) {
        if (brani == null) return;
        java.util.Set<IBrano> toRemove = new java.util.HashSet<>();
        for (Playable p : brani) {
            if (p instanceof IBrano b) {
                toRemove.add(b);
            }
        }
        catalogo.removeAll(toRemove);
        catalogoOriginale.removeAll(toRemove);
    }

    public List<IBrano> getBrani() {
        return List.copyOf(catalogo);
    }


    // ── Ricerca e ordinamento ─────────────────────────────────────────────────

    public List<IBrano> cercaBrani(FiltroRicerca filtro) {
        if (filtro == null)
            return getBrani();
        return filtro.applica(getBrani());
    }

    public void ordinaBrani(CampoOrdinamento campo) {
        if (campo == null) {
            ultimoCampoOrdinamento = null;
            catalogo.clear();
            catalogo.addAll(catalogoOriginale);
            return;
        }
        if (ultimoCampoOrdinamento == campo) {
            if (ultimoOrdineCrescente) {
                ultimoOrdineCrescente = false;
                OrdinamentoStrategy strategy = getStrategyFor(ultimoCampoOrdinamento, ultimoOrdineCrescente);
                if (strategy != null) strategy.ordina(catalogo);
            } else {
                ultimoCampoOrdinamento = null;
                catalogo.clear();
                catalogo.addAll(catalogoOriginale);
            }
        } else {
            ultimoCampoOrdinamento = campo;
            ultimoOrdineCrescente = true;
            OrdinamentoStrategy strategy = getStrategyFor(ultimoCampoOrdinamento, ultimoOrdineCrescente);
            if (strategy != null) strategy.ordina(catalogo);
        }
    }

    public CampoOrdinamento getUltimoCampoOrdinamento() {
        return ultimoCampoOrdinamento;
    }

    public boolean isUltimoOrdineCrescente() {
        return ultimoOrdineCrescente;
    }

    public boolean isEmpty() {
        return catalogo.isEmpty();
    }

}