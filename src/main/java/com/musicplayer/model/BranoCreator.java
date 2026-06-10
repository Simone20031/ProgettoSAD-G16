package com.musicplayer.model;



/**
 * BranoCreator: classe astratta Factory Method per la creazione di brani.
 * Definisce il metodo template creaBrano() e il metodo astratto costruisciBrano().
 * Pattern: Factory Method (creazionale).
 */
public abstract class BranoCreator {

    /**
     * Crea un brano validato a partire dai parametri grezzi.
     * È il "template method": costruisce, popola e valida.
     */
    public Brano creaBrano(String titolo, String autore, String genere, int anno, String percorsoFile, int durataSec, Tag tag) throws ValidazioneException {
        Brano b = costruisciBrano(titolo, autore, genere, anno, percorsoFile, durataSec, tag);
        b.validaDati();
        return b;
    }

    /**
     * Hook astratto: le sottoclassi decidono come istanziare il Brano.
     */
    protected abstract Brano costruisciBrano(String titolo, String autore, String genere, int anno, String percorsoFile, int durataSec, Tag tag);
}