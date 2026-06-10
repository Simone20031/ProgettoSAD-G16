package com.musicplayer.view;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import com.musicplayer.state.*;


import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class PlayerView implements RiproduzioneObserver {
    private final Button playBtn;
    private final Button stopBtn;
    private final Button skipBackBtn;
    private final Button skipBtn;
    private final Button shuffleBtn;
    private final Button loopBtn;
    private final Label currentTimeLabel;
    private final Label totalTimeLabel;
    private final Slider progressSlider;

    private final GestoreRiproduzione gestoreRiproduzione;
    private final LibreriaView libreriaView; // Per chiamare playSelected() della view
    private boolean isPlaying = false;
    private boolean shuffleEnabled = false;
    private boolean loopEnabled = false;

    public PlayerView(Button playBtn, Button stopBtn, Button skipBackBtn, Button skipBtn, Button shuffleBtn, Button loopBtn,
            Label currentTimeLabel, Label totalTimeLabel, Slider progressSlider,
            GestoreRiproduzione gestoreRiproduzione, LibreriaView libreriaView) {
        this.playBtn = playBtn;
        this.stopBtn = stopBtn;
        this.skipBackBtn = skipBackBtn;
        this.skipBtn = skipBtn;
        this.shuffleBtn = shuffleBtn;
        this.loopBtn = loopBtn;
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
        playBtn.setOnAction(e -> {
            animateButtonClick(playBtn);
            
            // Ritardiamo leggermente l'azione logica per permettere all'animazione
            // di renderizzare il primo frame (il bottone verde) prima di eventuali
            // blocchi del thread dovuti al caricamento del media.
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            delay.setOnFinished(ev -> {
                if (!isPlaying) {
                    if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                        gestoreRiproduzione.play();
                    } else {
                        libreriaView.playSelected();
                    }
                } else {
                    if (gestoreRiproduzione != null) {
                        gestoreRiproduzione.pausa();
                    }
                }
            });
            delay.play();
        });

        if (stopBtn != null) {
            stopBtn.setOnAction(e -> {
                animateButtonClick(stopBtn);
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
                delay.setOnFinished(ev -> {
                    if (gestoreRiproduzione != null) {
                        gestoreRiproduzione.stop();
                    }
                });
                delay.play();
            });
        }

        if (skipBackBtn != null) {
            skipBackBtn.setOnAction(e -> {
                animateButtonClick(skipBackBtn);
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
                delay.setOnFinished(ev -> {
                    if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                        gestoreRiproduzione.skipIndietro();
                    }
                });
                delay.play();
            });
        }

        if (skipBtn != null) {
            skipBtn.setOnAction(e -> {
                animateButtonClick(skipBtn);
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
                delay.setOnFinished(ev -> {
                    if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                        gestoreRiproduzione.skipAvanti();
                    }
                });
                delay.play();
            });
        }

        progressSlider.setOnMouseReleased(e -> {
            if (gestoreRiproduzione != null) {
                gestoreRiproduzione.seek((int) progressSlider.getValue());
            }
        });

        if (shuffleBtn != null) {
            shuffleBtn.setOnAction(e -> {
                shuffleEnabled = !shuffleEnabled;
                if (shuffleEnabled) {
                    shuffleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand;");
                    // Disattiva il loop se attivo
                    if (loopEnabled) {
                        loopEnabled = false;
                        if (loopBtn != null) {
                            loopBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                        }
                    }
                } else {
                    shuffleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                }
                if (libreriaView != null) {
                    libreriaView.onShuffleToggled(shuffleEnabled);
                }
            });
        }

        if (loopBtn != null) {
            loopBtn.setOnAction(e -> {
                loopEnabled = !loopEnabled;
                if (loopEnabled) {
                    loopBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand;");
                    // Disattiva lo shuffle se attivo
                    if (shuffleEnabled) {
                        shuffleEnabled = false;
                        if (shuffleBtn != null) {
                            shuffleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                        }
                    }
                } else {
                    loopBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                }
                if (libreriaView != null) {
                    libreriaView.onLoopToggled(loopEnabled);
                }
            });
        }
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public boolean isLoopEnabled() {
        return loopEnabled;
    }

    public void setPlaybackControlsDisabled(boolean disabled) {
        if (playBtn != null)
            playBtn.setDisable(disabled);
        if (stopBtn != null)
            stopBtn.setDisable(disabled);
        if (skipBackBtn != null)
            skipBackBtn.setDisable(disabled);
        if (skipBtn != null)
            skipBtn.setDisable(disabled);
        if (progressSlider != null)
            progressSlider.setDisable(disabled);
    }

    public void mostraStatoPlay() {
        isPlaying = false;
        playBtn.setText("▶");
        aggiornaStilePlayer(null); // Rimuove evidenziatore se necessario, o lo setta
    }

    public void mostraStatoPausa() {
        isPlaying = true;
        playBtn.setText("⏸");
        aggiornaStilePlayer(playBtn);
    }

    private void aggiornaStilePlayer(Button attivo) {
        if (playBtn != null && !playBtn.getProperties().containsKey("isAnimating")) {
            playBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-background-radius: 50%; -fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;");
        }
        if (stopBtn != null && !stopBtn.getProperties().containsKey("isAnimating")) {
            stopBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-cursor: hand;");
        }

        if (attivo != null && attivo != playBtn && !attivo.getProperties().containsKey("isAnimating")) {
            attivo.setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-font-size: 24px; -fx-cursor: hand;");
        }
    }

    private void animateButtonClick(Button btn) {
        if (btn == null) return;
        
        if (btn.getProperties().containsKey("isAnimating")) {
            return;
        }
        btn.getProperties().put("isAnimating", true);

        String originalStyle;
        String greenStyle;
        
        if (btn == playBtn) {
            originalStyle = "-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-background-radius: 50%; -fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;";
            greenStyle = "-fx-background-color: #1DB954; -fx-text-fill: #ffffff; -fx-background-radius: 50%; -fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;";
        } else {
            originalStyle = "-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-cursor: hand;";
            greenStyle = "-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-font-size: 24px; -fx-cursor: hand;";
        }
        
        btn.setStyle(greenStyle);

        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), btn);
        scaleDown.setToX(0.85);
        scaleDown.setToY(0.85);
        scaleDown.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), btn);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        scaleUp.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        
        scaleDown.setOnFinished(e -> scaleUp.play());
        
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
        pause.setOnFinished(e -> {
            btn.setStyle(originalStyle);
            btn.getProperties().remove("isAnimating");
            
            // Ripristina il colore verde perenne se il bottone Stop è quello attivo
            if (gestoreRiproduzione != null && gestoreRiproduzione.getStato() instanceof StoppedState) {
                aggiornaStilePlayer(stopBtn);
            }
        });
        
        scaleDown.play();
        pause.play();
    }

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public void setTotalTimeLabel(int durataSecondi) {
        totalTimeLabel.setText(formatTime(durataSecondi));
        progressSlider.setMax(durataSecondi);
        if (gestoreRiproduzione == null || !gestoreRiproduzione.hasActiveMedia()) {
            progressSlider.setValue(0);
            currentTimeLabel.setText("00:00");
        }
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
        mostraStatoPausa(); // Mostro Pausa perché è in riproduzione
    }

    @Override
    public void onPausa() {
        mostraStatoPlay(); // Mostro Play perché è in pausa
    }

    @Override
    public void onStop() {
        mostraStatoPlay();
        aggiornaStilePlayer(stopBtn);
        progressSlider.setValue(0);
        currentTimeLabel.setText("00:00");
    }

    public void aggiornaBarra(float percentuale) {
        if (!progressSlider.isValueChanging()) {
            double max = progressSlider.getMax();
            progressSlider.setValue(percentuale * max / 100.0);
        }
    }

    public void aggiornaTimer(int secondi) {
        currentTimeLabel.setText(formatTime(secondi));
    }

    @Override
    public void onProgressoAggiornato(int secondi) {
        double max = progressSlider.getMax();
        float percentuale = max > 0 ? (float) (secondi * 100.0 / max) : 0.0f;
        aggiornaBarra(percentuale);
        aggiornaTimer(secondi);
    }

    @Override
    public void onBranoCambiato(String nuovoPercorso) {
        // Nessun aggiornamento necessario nel PlayerView, ci pensa LibreriaView
    }
}
