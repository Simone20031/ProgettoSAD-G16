package com.musicplayer.adapter;

/**
 * Mp3LibFile: Interfaccia che rappresenta la libreria di terze parti 
 * per la gestione dei file MP3 (da adattare).
 */
public interface Mp3LibFile {
    String getFilename();
    int getLengthInSeconds();
    String getArtistName();
    String getTrackTitle();
}
