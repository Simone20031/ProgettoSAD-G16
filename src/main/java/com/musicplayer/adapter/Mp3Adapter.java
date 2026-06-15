package com.musicplayer.adapter;

import com.musicplayer.model.Playable;

/**
 * Mp3Adapter: Adatta l'interfaccia Mp3LibFile all'interfaccia Playable
 * utilizzata dal nostro riproduttore musicale.
 */
public class Mp3Adapter implements Playable {

    private Mp3LibFile mp3File;
    private int playCount;

    public Mp3Adapter(Mp3LibFile file) {
        if (file == null) {
            throw new IllegalArgumentException("Mp3LibFile non può essere nullo");
        }
        this.mp3File = file;
        this.playCount = 0;
    }

    public int getDurata() {
        return mp3File.getLengthInSeconds();
    }

    @Override
    public int getDurataTotale() {
        return getDurata();
    }

    @Override
    public int getPlayCount() {
        return playCount;
    }

    @Override
    public void incrementPlayCount() {
        this.playCount++;
    }

    public Mp3LibFile getMp3File() {
        return mp3File;
    }
}
