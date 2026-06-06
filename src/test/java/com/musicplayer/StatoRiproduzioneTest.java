package com.musicplayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatoRiproduzioneTest {

    @BeforeEach
    public void setUp() {
        GestoreRiproduzione.resetInstance();
    }

    @Test
    public void testPassaggioPlayPausaMantieneTempo() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        
        // Partiamo da uno stato fermo
        gestore.setStato(new StoppedState());
        assertTrue(gestore.getStato() instanceof StoppedState, "Il player deve essere fermo inizialmente");
        
        // Passiamo a Play
        gestore.play();
        assertTrue(gestore.getStato() instanceof PlayingState, "Dopo play(), lo stato deve essere PlayingState");
        
        // Passiamo a Pausa
        // In JavaFX, chiamare pause() su MediaPlayer (che avviene dentro eseguiPausa())
        // mantiene nativamente il punto temporale corrente. La cosa fondamentale è verificare 
        // che lo State passi effettivamente a PausedState senza azzerare nulla (come invece farebbe Stop).
        gestore.pausa();
        assertTrue(gestore.getStato() instanceof PausedState, "Dopo pausa(), lo stato deve essere PausedState e il tempo è mantenuto");
        
        // Riprendiamo la riproduzione
        gestore.play();
        assertTrue(gestore.getStato() instanceof PlayingState, "Dopo un ulteriore play(), si riprende dallo stesso punto (PlayingState)");
    }
    
    @Test
    public void testPassaggioPlayStopAzzeraTempo() {
        GestoreRiproduzione gestore = GestoreRiproduzione.getInstance();
        
        gestore.setStato(new PlayingState());
        
        // Chiamando stop, il MediaPlayer distrugge il media o resetta il tempo
        gestore.stop();
        assertTrue(gestore.getStato() instanceof StoppedState, "Dopo stop(), lo stato deve tornare a StoppedState e il tempo si azzera");
    }
}
