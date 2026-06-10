package com.musicplayer.view;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;


import com.musicplayer.persistence.MetadataService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class FormBranoView {

    private final TextField importTitleField;
    private final TextField importAuthorField;
    private final TextField importYearField;
    private final Label lblImportTitle;
    private final Label lblImportFilename;
    private final Button btnConfermaImport;
    private final VBox genreContainer;
    private final VBox tagContainer;

    private final FlowPane genreButtonBox;
    private final FlowPane tagButtonBox;

    private final LibreriaController libreriaController;
    private final Runnable onComplete;
    private final Stage primaryStage;

    private File pendingImportFile = null;
    private Brano pendingEditBrano = null;
    private int pendingDuration = 0;
    private Genere selectedGenre = Genere.NESSUNO;
    private final Set<Tag> selectedTags = new LinkedHashSet<>();

    private static final String PILL_BASE = "-fx-background-radius: 30; -fx-border-radius: 30; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 6 16; -fx-font-size: 13px; -fx-font-weight: bold;";
    private static final String PILL_OFF = PILL_BASE + "-fx-background-color: #1e1e1e; -fx-text-fill: #a7a7a7; -fx-border-color: #444444;";
    private static final String PILL_ON_GREEN = PILL_BASE + "-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-border-color: #1DB954;";
    private static final String PILL_ON_TAG = PILL_BASE + "-fx-background-color: #4a90d9; -fx-text-fill: #ffffff; -fx-border-color: #4a90d9;";

    public FormBranoView(
            TextField importTitleField, TextField importAuthorField, TextField importYearField,
            Label lblImportTitle, Label lblImportFilename, Button btnConfermaImport,
            VBox genreContainer, VBox tagContainer,
            LibreriaController libreriaController, Runnable onComplete, Stage primaryStage) {

        this.importTitleField = importTitleField;
        this.importAuthorField = importAuthorField;
        this.importYearField = importYearField;
        this.lblImportTitle = lblImportTitle;
        this.lblImportFilename = lblImportFilename;
        this.btnConfermaImport = btnConfermaImport;
        this.genreContainer = genreContainer;
        this.tagContainer = tagContainer;
        this.libreriaController = libreriaController;
        this.onComplete = onComplete;
        this.primaryStage = primaryStage;

        this.genreButtonBox = new FlowPane();
        this.genreButtonBox.setHgap(8);
        this.genreButtonBox.setVgap(8);
        this.genreContainer.getChildren().add(this.genreButtonBox);

        this.tagButtonBox = new FlowPane();
        this.tagButtonBox.setHgap(8);
        this.tagButtonBox.setVgap(8);
        this.tagContainer.getChildren().add(this.tagButtonBox);

        inizializzaBottoniGenereTag();
    }

    private void inizializzaBottoniGenereTag() {
        for (Genere g : Genere.values()) {
            if (g == Genere.NESSUNO) continue;
            Button btn = new Button(g.getEtichetta());
            btn.setStyle(PILL_OFF);
            btn.setOnAction(e -> {
                selectedGenre = g;
                genreButtonBox.getChildren().forEach(n -> {
                    if (n instanceof Button b) {
                        b.setStyle(PILL_OFF);
                    }
                });
                btn.setStyle(PILL_ON_GREEN);
            });
            genreButtonBox.getChildren().add(btn);
        }

        for (Tag t : Tag.values()) {
            if (t == Tag.NESSUNO) continue;
            Button btn = new Button(t.getEtichetta());
            btn.setStyle(PILL_OFF);
            btn.setOnAction(e -> {
                if (selectedTags.contains(t)) {
                    selectedTags.remove(t);
                    btn.setStyle(PILL_OFF);
                } else {
                    selectedTags.add(t);
                    btn.setStyle(PILL_ON_TAG);
                }
            });
            tagButtonBox.getChildren().add(btn);
        }
    }

    public void resetImportView() {
        importTitleField.clear();
        importAuthorField.clear();
        importYearField.clear();
        selectedGenre = Genere.NESSUNO;
        selectedTags.clear();
        pendingImportFile = null;
        lblImportFilename.setText("");

        genreButtonBox.getChildren().forEach(n -> {
            if (n instanceof Button b) {
                b.setStyle(PILL_OFF);
            }
        });
        tagButtonBox.getChildren().forEach(n -> {
            if (n instanceof Button b) {
                b.setStyle(PILL_OFF);
            }
        });
    }

    public void addSong() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 files", "*.mp3"));
        File f = fc.showOpenDialog(primaryStage);
        if (f == null) return;

        resetImportView();
        pendingImportFile = f;
        pendingEditBrano = null;
        pendingDuration = 0;
        lblImportFilename.setText("📁  " + f.getAbsolutePath());
        lblImportTitle.setText("Importa brano");
        btnConfermaImport.setText("AGGIUNGI ALLA LIBRERIA");

        MetadataService.estraiMetadati(f, values -> Platform.runLater(() -> {
            String title = (values != null && values[0] != null && !values[0].isBlank()) ? values[0] : MetadataService.stripExtension(f.getName());
            String author = (values != null && values[1] != null) ? values[1] : "";
            if (values != null && values[3] != null && !values[3].isBlank()) {
                try { pendingDuration = Integer.parseInt(values[3].trim()); } catch (NumberFormatException ignored) {}
            }
            importTitleField.setText(title);
            importAuthorField.setText(author);
        }));
    }

    public void editBrano(Brano brano, com.musicplayer.persistence.SongMetadata m) {
        resetImportView();
        
        java.nio.file.Path libDir = java.nio.file.Path.of(System.getProperty("user.dir"), "Libreria");
        java.nio.file.Path pathDaUsare = java.nio.file.Path.of(brano.getPercorsoFile());
        if (!pathDaUsare.isAbsolute()) {
            pathDaUsare = libDir.resolve(pathDaUsare.getFileName());
        }
        
        pendingImportFile = pathDaUsare.toFile();
        pendingEditBrano = brano;
        pendingDuration = brano.getDurata();

        lblImportFilename.setText("📁  " + brano.getPercorsoFile());
        lblImportTitle.setText("Modifica brano: " + brano.getTitolo());
        btnConfermaImport.setText("SALVA MODIFICHE");

        importTitleField.setText(brano.getTitolo());
        importAuthorField.setText(brano.getAutore());
        if (brano.getAnno() > 0) importYearField.setText(String.valueOf(brano.getAnno()));

        String gen = brano.getGenere();
        if (gen != null && !gen.isBlank()) {
            for (Genere g : Genere.values()) {
                if (g.getEtichetta().equalsIgnoreCase(gen.trim())) {
                    selectedGenre = g;
                    genreButtonBox.getChildren().forEach(n -> {
                        if (n instanceof Button b && b.getText().equals(g.getEtichetta())) {
                            b.setStyle(PILL_ON_GREEN);
                        }
                    });
                    break;
                }
            }
        }

        String tg = (m != null && m.tag != null) ? m.tag : (brano.getTag() != null ? brano.getTag().getEtichetta() : "");
        if (tg != null && !tg.isBlank() && !tg.equalsIgnoreCase("NESSUNO")) {
            String[] tags = tg.split(",");
            for (String ts : tags) {
                String trimmed = ts.trim();
                for (Tag t : Tag.values()) {
                    if (t.getEtichetta().equalsIgnoreCase(trimmed) || t.name().equalsIgnoreCase(trimmed)) {
                        selectedTags.add(t);
                        tagButtonBox.getChildren().forEach(n -> {
                            if (n instanceof Button b && b.getText().equals(t.getEtichetta())) {
                                b.setStyle(PILL_ON_TAG);
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    public void confermaImportBrano() throws Exception {
        if (pendingImportFile == null) return;

        String tagRaw;
        if (selectedTags.isEmpty()) {
            tagRaw = Tag.NESSUNO.name();
        } else {
            tagRaw = selectedTags.stream()
                    .map(Tag::getEtichetta)
                    .collect(java.util.stream.Collectors.joining(","));
        }

        int anno = 0;
        String aTxt = importYearField.getText();
        if (aTxt != null && !aTxt.isBlank()) {
            try {
                anno = Integer.parseInt(aTxt.trim());
            } catch (NumberFormatException e) {
                throw new ValidazioneException("Anno non valido");
            }
        }

        if (pendingEditBrano != null) {
            Map<String, String> dati = new HashMap<>();
            dati.put("titolo", importTitleField.getText());
            dati.put("autore", importAuthorField.getText());
            dati.put("anno", String.valueOf(anno));
            dati.put("genere", selectedGenre == Genere.NESSUNO ? "" : selectedGenre.getEtichetta());
            dati.put("durata", String.valueOf(pendingDuration));
            dati.put("tag", tagRaw);

            libreriaController.modificaBrano(pendingEditBrano, dati);
        } else {
            libreriaController.aggiungiBrano(
                    importTitleField.getText(),
                    importAuthorField.getText(),
                    selectedGenre == Genere.NESSUNO ? "" : selectedGenre.getEtichetta(),
                    anno,
                    pendingImportFile.getAbsolutePath(),
                    pendingDuration,
                    tagRaw);
        }

        if (onComplete != null) onComplete.run();
    }
}
