package com.musicplayer.model;

import java.util.Map;

/**
 * BranoProxy: Implementa il pattern Proxy (Virtual Proxy) per ritardare 
 * il caricamento/elaborazione dei dettagli del brano reale.
 */
public class BranoProxy implements IBrano {

    private Brano branoReale;
    private boolean dettagliCaricati;

    public BranoProxy(Brano branoReale) {
        if (branoReale == null) {
            throw new IllegalArgumentException("Il brano reale non può essere nullo");
        }
        this.branoReale = branoReale;
        this.dettagliCaricati = false;
    }

    private void caricaDettagli() {
        if (!dettagliCaricati) {
            java.util.Map<String, com.musicplayer.persistence.SongMetadata> mappa = new java.util.HashMap<>();
            com.musicplayer.persistence.MetadataService.caricaMappaDalCSV(mappa);

            String chiave = branoReale.getPercorsoFile();
            if (chiave == null || chiave.isEmpty()) {
                chiave = branoReale.getId();
            }

            com.musicplayer.persistence.SongMetadata meta = mappa.get(chiave);
            if (meta != null) {
                java.util.Map<String, String> dati = new java.util.HashMap<>();
                dati.put("titolo", meta.title);
                dati.put("autore", meta.author);
                dati.put("anno", meta.year);
                dati.put("durata", meta.duration);
                dati.put("genere", meta.genre);
                dati.put("tag", meta.tag);

                try {
                    branoReale.setDettagli(dati);
                    branoReale.setPlayCount(meta.playCount);
                } catch (ValidazioneException e) {
                    System.err.println("Errore nel caricamento lazy dei metadati del brano: " + e.getMessage());
                }
            }
            dettagliCaricati = true;
        }
    }

    @Override
    public Map<String, String> getDettagli() {
        caricaDettagli();
        return branoReale.getDettagli();
    }

    @Override
    public int getDurata() {
        caricaDettagli();
        return branoReale.getDurata();
    }

    @Override
    public String getTitolo() {
        caricaDettagli();
        return branoReale.getTitolo();
    }

    @Override
    public int getDurataTotale() {
        caricaDettagli();
        return branoReale.getDurataTotale();
    }

    @Override
    public int getPlayCount() {
        // La lettura del play count fa parte delle informazioni caricate
        caricaDettagli();
        return branoReale.getPlayCount();
    }

    @Override
    public void incrementPlayCount() {
        caricaDettagli();
        branoReale.incrementPlayCount();
    }

    public Brano getBranoReale() {
        return branoReale;
    }

    public boolean isDettagliCaricati() {
        return dettagliCaricati;
    }
}
