package com.musicplayer;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String nome;
    private List<IBrano> brani;

    public Playlist(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome playlist non valido");
        }
        this.nome = nome;
        this.brani = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public List<IBrano> getBrani() {
        return brani;
    }

    public void aggiungiBrano(IBrano brano) {
        if (!brani.contains(brano)) {
            brani.add(brano);
        }
    }

    public void rimuoviBrano(IBrano brano) {
        brani.remove(brano);
    }
}