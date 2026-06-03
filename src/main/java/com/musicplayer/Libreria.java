package com.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Libreria: collezione centrale di brani e playlist.
 * Pattern: Singleton + Observer.
 */
public class Libreria {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static Libreria instance;

    private final List<IBrano>   catalogo = new ArrayList<>();
    private final List<Playlist> playlist = new ArrayList<>();

    private Libreria() {}
    
    public static Libreria getInstance() {
        if (instance == null) instance = new Libreria();
        return instance;
    }

    // ── Gestione brani ────────────────────────────────────────────────────────

    public void aggiungiBrano(IBrano b) {
        if (b == null || catalogo.contains(b)) return;
        catalogo.add(b);
        // observer disabilitato in questo momento
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
        if (b == null) return;

        // Rimuove l'oggetto dalla lista 'catalogo'
        if (catalogo.remove(b)) {
            
            //  playlist non implementata in questo momento
            // Rimuove il brano da tutte le playlist che lo contengono
            
            // pattern Observer per aggiornare la View in automatico da implementare in futuro:
            System.out.println("Brano rimosso con successo dal catalogo in RAM.");
        }
    }

    // ── Gestione playlist ─────────────────────────────────────────────────────

    /**
     * Crea una nuova Playlist e la registra nella libreria.
     *
     * Regole di validazione (task 7.1):
     * - Il nome non deve essere vuoto → ValidazioneException (CAMPO_MANCANTE, "nome")
     * - Non deve esistere già una playlist con lo stesso nome (case-insensitive)
     *   → ValidazioneException (GENERICO, "nome")
     *
     * @param nome il nome della nuova playlist
     * @return la Playlist appena creata e registrata
     * @throws ValidazioneException se il nome è vuoto o già in uso
     */
    public Playlist creaPlaylist(String nome) throws ValidazioneException {
        // Validazione 1: nome obbligatorio
        if (nome == null || nome.isBlank()) {
            throw new ValidazioneException(
                    "Il nome della playlist è obbligatorio!",
                    ValidazioneException.TipoErrore.CAMPO_MANCANTE,
                    "nome");
        }

        // Validazione 2: unicità case-insensitive
        String nomeTrim = nome.trim();
        boolean duplicato = playlist.stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase(nomeTrim));
        if (duplicato) {
            throw new ValidazioneException(
                    "Esiste già una playlist con il nome '" + nomeTrim + "'!",
                    ValidazioneException.TipoErrore.GENERICO,
                    "nome");
        }

        Playlist nuova = new Playlist(nomeTrim);
        playlist.add(nuova);
        // observer disabilitato in questo momento
        return nuova;
    }

    public void eliminaPlaylist(Playlist p) {
        // Playlist non implementata in questo momento
        // observer disabilitato in questo momento
    }
    // ── Stato interno ─────────────────────────────────────────────────────────

    public List<IBrano> getBrani() { return List.copyOf(catalogo); }
    
    public List<Playlist> getPlaylist() { return List.copyOf(playlist); }
    
    // ── Ricerca e ordinamento ─────────────────────────────────────────────────
    /* Ricerca e ordinamento non implementati in questo momento.
    cercaBrani
    ordinaBrani
    */

    public boolean isEmpty() { return catalogo.isEmpty(); }

    // Observer disabilitato in questo momento
    // public void addObserver(LibreriaObserver o)    { /*  observer disabilitato in questo momento */ }
    // public void removeObserver(LibreriaObserver o) { /*  observer disabilitato in questo momento */ }

    // Permette a componenti esterni di forzare una notifica di aggiornamento playlist
    public void notificaObserver() { /* observer disabilitato in questo momento */ }
    
}