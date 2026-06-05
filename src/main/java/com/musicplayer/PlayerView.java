package com.musicplayer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class PlayerView implements RiproduzioneObserver {
    private final Button playBtn;
    private final Button pauseBtn;
    private final Button stopBtn;
    private final Label currentTimeLabel;
    private final Label totalTimeLabel;
    private final Slider progressSlider;

    private final GestoreRiproduzione gestoreRiproduzione;
    private final LibreriaView libreriaView; // Per chiamare playSelected() della view

    public PlayerView(Button playBtn, Button pauseBtn, Button stopBtn,
            Label currentTimeLabel, Label totalTimeLabel, Slider progressSlider,
            GestoreRiproduzione gestoreRiproduzione, LibreriaView libreriaView) {
        this.playBtn = playBtn;
        this.pauseBtn = pauseBtn;
        this.stopBtn = stopBtn;
        this.currentTimeLabel = currentTimeLabel;
        this.totalTimeLabel = totalTimeLabel;
        this.progressSlider = progressSlider;
        this.gestoreRiproduzione = gestoreRiproduzione;
        this.libreriaView = libreriaView;

        initHandlers();
        setPlaybackControlsDisabled(true);

        // Registrati come observer
        if (this.gestoreRiproduzione != null) {
            this.gestoreRiproduzione.addObserver(this);
        }
    }

    private void initHandlers() {
        playBtn.setOnAction(e -> libreriaView.playSelected());

        pauseBtn.setOnAction(e -> {
            if (gestoreRiproduzione != null) {
                gestoreRiproduzione.pausa();
            }
        });

        stopBtn.setOnAction(e -> {
            if (gestoreRiproduzione != null) {
                gestoreRiproduzione.stop();
            }
        });

        progressSlider.setOnMouseReleased(e -> {
            if (gestoreRiproduzione != null) {
                gestoreRiproduzione.seek((int) progressSlider.getValue());
            }
        });
    }

    public void setPlaybackControlsDisabled(boolean disabled) {
        if (playBtn != null)
            playBtn.setDisable(disabled);
        if (pauseBtn != null)
            pauseBtn.setDisable(disabled);
        if (stopBtn != null)
            stopBtn.setDisable(disabled);
        if (progressSlider != null)
            progressSlider.setDisable(disabled);
    }

    private void aggiornaStilePlayer(Button attivo) {
        String baseColor = "#ffffff";
        String activeColor = "#1DB954";

        playBtn.setStyle(playBtn.getStyle().replaceAll("-fx-background-color: #[a-fA-F0-9]+;",
                "-fx-background-color: " + baseColor + ";"));
        pauseBtn.setStyle(pauseBtn.getStyle().replaceAll("-fx-background-color: #[a-fA-F0-9]+;",
                "-fx-background-color: " + baseColor + ";"));
        stopBtn.setStyle(stopBtn.getStyle().replaceAll("-fx-background-color: #[a-fA-F0-9]+;",
                "-fx-background-color: " + baseColor + ";"));

        if (attivo != null) {
            attivo.setStyle(attivo.getStyle().replaceAll("-fx-background-color: #[a-fA-F0-9]+;",
                    "-fx-background-color: " + activeColor + ";"));
        }
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public void setTotalTimeLabel(int durataSecondi) {
        totalTimeLabel.setText(formatTime(durataSecondi));
        progressSlider.setMax(durataSecondi);
        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
    }

    // --- Metodi Observer ---

    @Override
    public void onPlayerReady(int durataSecondi) {
        progressSlider.setMax(durataSecondi);
        progressSlider.setValue(0);
        totalTimeLabel.setText(formatTime(durataSecondi));
        currentTimeLabel.setText("00:00");
        setPlaybackControlsDisabled(false);
    }

    @Override
    public void onPlay() {
        aggiornaStilePlayer(playBtn);
    }

    @Override
    public void onPausa() {
        aggiornaStilePlayer(pauseBtn);
    }

    @Override
    public void onStop() {
        aggiornaStilePlayer(stopBtn);
        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
    }

    @Override
    public void onProgressoAggiornato(int secondi) {
        if (!progressSlider.isValueChanging()) {
            progressSlider.setValue(secondi);
        }
        currentTimeLabel.setText(formatTime(secondi));
    }
}
