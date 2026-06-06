package com.musicplayer;

/**
 * BranoFactory: Classe che implementa il pattern 'Factory Method'.
 * * Sfrutta il principio dell'OVERLOADING (sovraccarico dei metodi): i due metodi 
 * si chiamano nello stesso modo ("creaBrano"), ma Java capisce quale usare in base 
 * al numero, all'ordine e al tipo dei parametri passati nella firma.
 */
public class BranoFactory extends BranoCreator {

    /**
     * METODO 1 (STATIC) ── SCENARIO: BOOTSTRAP / RIPRISTINO DAL FILE CSV
     * * Questo metodo è statico (si invoca con BranoFactory.creaBrano) e accetta 8 parametri.
     * Serve all'avvio del programma quando si legge il database 'metadata.csv' riga per riga.
     * Poiché nel CSV tutto è memorizzato come testo, questo metodo riceve stringhe grezze e 
     * si fa carico del "lavoro sporco" di conversione e parsing difensivo.
     *
     * @param filename   Il nome del file (es. "canzone.mp3"), usato storicamente come ID.
     * @param title      Il titolo letto dal CSV.
     * @param author     L'autore letto dal CSV.
     * @param year       L'anno in formato Stringa (es. "2023").
     * @param duration   La durata in formato Stringa (es. "180").
     * @param genre      Il genere musicale letto dal CSV.
     * @param tag        Il tag in formato Stringa (es. "PREFERITI").
     * @param playCount  Il contatore storico delle riproduzioni.
     * @return Un oggetto Brano pronto per la RAM, popolato con i vecchi dati storici.
     * @throws ValidazioneException Se i dati letti dal file violano l'integrità del modello.
     */
    public static Brano creaBrano(String filename, String title, String author, String year, String duration, String genre, String tag, int playCount) throws ValidazioneException {
        
        // Parsing dell'anno: se la stringa è vuota o corrotta assegna 0, evitando crash (NumberFormatException)
        int anno = 0;
        try { 
            anno = (year == null || year.isBlank()) ? 0 : Integer.parseInt(year.trim()); 
        } catch (NumberFormatException ignored) {}
        
        // Parsing della durata: converte i secondi testuali del CSV in un intero nativo Java
        int durSec = 0;
        try { 
            durSec = (duration == null || duration.isBlank()) ? 0 : Integer.parseInt(duration.trim()); 
        } catch (NumberFormatException ignored) {}
        
        // Mappatura sicura: converte la stringa del tag nel corrispondente valore fortemente tipizzato dell'Enum Tag
        Tag t = Tag.fromString(tag);
        
        // Ricostruisce il brano riutilizzando il vecchio identificativo (filename) senza inventare nuovi ID
        Brano b = new Brano(filename, title, author, genre, anno, filename, durSec, t);
        
        // Validazione eager immediata: controlla i vincoli prima di mandare l'oggetto al controller
        b.validaDati();
        
        return b;
    }

    /**
     * METODO 2 (D'ISTANZA) ── SCENARIO: CREAZIONE EX-NOVO DA INTERFACCIA GRAFICA (UI)
     * * Questo metodo non è statico (richiede l'oggetto "factory") e accetta 7 parametri.
     * Viene invocato dal LibreriaController quando l'utente compila il form popup e 
     * aggiunge una NUOVA canzone mai vista prima nel programma. Riceve parametri già 
     * tipizzati e puliti dal controller e ha il compito di battezzare il brano con un ID unico.
     *
     * @param titolo       Il titolo inserito dall'utente nel form grafico.
     * @param autore       L'autore inserito dall'utente nel form grafico.
     * @param genere       Il genere inserito dall'utente nel form grafico.
     * @param anno         L'anno di pubblicazione (già convalidato come numero intero).
     * @param percorsoFile Il percorso del file sul computer (normalizzato da PathUtils).
     * @param durataSec    La durata reale in secondi estratta dal player audio o inserita.
     * @param tag          L'oggetto Enum Tag selezionato nella UI.
     * @return Un oggetto Brano completamente nuovo con un'identità digitale vergine.
     * @throws ValidazioneException Se i campi obbligatori del form (es. titolo) sono vuoti.
     */
    @Override
    protected Brano costruisciBrano(String titolo, String autore, String genere, int anno, String percorsoFile, int durataSec, Tag tag) {
        
        // Fallback difensivo: se l'utente non ha scelto nessun tag, assegna di default il valore NESSUNO
        Tag t = tag == null ? Tag.NESSUNO : tag;
        
        // Generazione di un identificativo surrogato (UUID v4) alfanumerico globale e immutabile.
        // Questo sarà il codice fiscale della nuova canzone all'interno del database.
        String idUnivoco = java.util.UUID.randomUUID().toString();
        
        // Istanzia il brano accoppiando l'ID appena generato con i parametri numerici già pronti
        return new Brano(idUnivoco, titolo, autore, genere, anno, percorsoFile, durataSec, t);
    }
}