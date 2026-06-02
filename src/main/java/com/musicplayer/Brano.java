package com.musicplayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Brano: entità principale del dominio musicale.
 * Implementa IBrano (che estende Playable).
 */
public class Brano implements IBrano {

    private String id;
    private String titolo;
    private String autore;
    private String genere;
    private int anno;
    private String percorsoFile;
    private int durata; // in secondi
    private Tag tag;

    public Brano(String id, String titolo, String autore, String genere, int anno, String percorsoFile, int durata, Tag tag) {
        this.id           = id           == null ? "" : id;
        this.titolo       = titolo       == null ? "" : titolo;
        this.autore       = autore       == null ? "" : autore;
        this.genere       = genere       == null ? "" : genere;
        this.anno         = anno;
        this.percorsoFile = percorsoFile == null ? "" : percorsoFile;
        this.durata       = Math.max(0, durata);
        this.tag          = tag          == null ? Tag.NESSUNO : tag;
    }

    // Getter per i campi principali (usati da view e CSV)
    public String getId()           { return id; }
    public String getAutore()       { return autore; }
    public String getGenere()       { return genere; }
    public int    getAnno()         { return anno; }
    public String getPercorsoFile() { return percorsoFile; }
    public Tag    getTag()          { return tag; }

    // ── IBrano ────────────────────────────────────────────────────────────────

    @Override
    public String getTitolo() { return titolo; }

    @Override
    public int getDurata() { return durata; }

    // ── Dettagli (usati da view e CSV) ────────────────────────────────────────

    @Override
    public Map<String, String> getDettagli() {
        Map<String, String> m = new HashMap<>();
        m.put("id",           id);
        m.put("titolo",       titolo);
        m.put("autore",       autore);
        m.put("genere",       genere);
        m.put("anno",         String.valueOf(anno));
        m.put("percorsoFile", percorsoFile);
        m.put("durata",       String.valueOf(durata));
        m.put("tag",          tag.name());
        return m;
    }

    // Setter per aggiornare i dettagli (usato da view e CSV)
    public void setDettagli(Map<String, String> dati) throws ValidazioneException {
        if (dati == null) return;
        
        this.titolo       = dati.getOrDefault("titolo",       this.titolo);
        this.autore       = dati.getOrDefault("autore",       this.autore);
        this.genere       = dati.getOrDefault("genere",       this.genere);
        this.percorsoFile = dati.getOrDefault("percorsoFile", this.percorsoFile);
        
        String a = dati.get("anno");
        if (a != null) { 
            try { 
                this.anno = Integer.parseInt(a.trim()); 
            } catch (NumberFormatException e) { 
                throw new ValidazioneException("L'Anno inserito non è un numero valido!", ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, "anno");
            } 
        }
        
        String d = dati.get("durata");
        if (d != null) { 
            try { 
                this.durata = Math.max(0, Integer.parseInt(d.trim())); 
            } catch (NumberFormatException e) { 
                throw new ValidazioneException("La Durata inserita non è un numero valido!", ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, "durata");
            } 
        }
        
        String t = dati.get("tag");
        if (t != null) this.tag = Tag.fromString(t);
        
        // Dopo l'aggiornamento dei campi, validiamo i dati obbligatori e i range
        validaDati();
    }

    public void setPercorsoFile(String percorso) {
        this.percorsoFile = percorso == null ? "" : percorso;
    }

    // ── Validazione ───────────────────────────────────────────────────────────

    /**
     * 1.6 - Validazione dei dati del Brano.
     * Sfrutta il nuovo costruttore di ValidazioneException impostando il TipoErrore
     * e marchiando in modo preciso il campo incriminato.
     */
    public void validaDati() throws ValidazioneException {
        // Controlli sui campi obbligatori mancanti (CAMPO_MANCANTE)
        if (titolo == null || titolo.isBlank()) {
            throw new ValidazioneException("Il Titolo del brano è obbligatorio!", ValidazioneException.TipoErrore.CAMPO_MANCANTE, "titolo");
        }
        if (autore == null || autore.isBlank()) {
            throw new ValidazioneException("L'Autore del brano è obbligatorio!", ValidazioneException.TipoErrore.CAMPO_MANCANTE, "autore");
        }
        if (genere == null || genere.isBlank()) {
            throw new ValidazioneException("Il Genere del brano è obbligatorio!", ValidazioneException.TipoErrore.CAMPO_MANCANTE, "genere");
        }
        
        // Controllo sui valori fuori range numerico (FORMATO_NON_VALIDO)
        if (anno != 0 && (anno < 1800 || anno > 2100)) {
            throw new ValidazioneException("L'Anno inserito è fuori range (1800-2100)!", ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, "anno");
        }

        if (durata < 0) {
            throw new ValidazioneException("La Durata del brano non può essere negativa!", ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, "durata");
        }
    }

    // ── Accessori ─────────────────────────────────────────────────────────────

    public void rimuoviTag() { this.tag = Tag.NESSUNO; }
}