package com.musicplayer.command;

import com.musicplayer.model.*;
import com.musicplayer.persistence.PersistenzaException;
import com.musicplayer.controller.RiproduzioneException;


/**
 * Interfaccia fondamentale per il design pattern Command.
 * Incapsula una richiesta come oggetto, permettendo di parametrizzare i client
 * con code di richieste e supportare operazioni annullabili (Undo/Redo).
 */
public interface Command {
    
    /**
     * Esegue l'azione incapsulata nel comando.
     * @throws ValidazioneException Se i parametri dell'azione non sono validi.
     * @throws PersistenzaException Se fallisce il salvataggio o la modifica dei file fisici.
     * @throws PlaylistException Se l'azione riguarda una playlist e viola regole di univocità o accesso.
     * @throws RiproduzioneException Se l'azione entra in conflitto con un brano attualmente in esecuzione.
     */
    void esegui() throws ValidazioneException, PersistenzaException, PlaylistException, RiproduzioneException;
    
    /**
     * Ripristina lo stato del sistema annullando l'effetto del metodo {@link #esegui()}.
     * @throws ValidazioneException Se i parametri per l'annullamento non sono validi.
     * @throws PersistenzaException Se fallisce il ripristino su file.
     * @throws RiproduzioneException Se c'è conflitto con il player audio durante l'annullamento.
     * @throws PlaylistException Se c'è un errore nella ricostruzione o rimozione dalle playlist.
     */
    void annulla() throws ValidazioneException, PersistenzaException, RiproduzioneException, PlaylistException;
}
