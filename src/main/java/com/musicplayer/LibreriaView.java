package com.musicplayer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.musicplayer.persistence.MetadataService;
import com.musicplayer.persistence.SongMetadata;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LibreriaView implements Initializable, LibreriaObserver {

    @FXML private ListView<String> songListView;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Label detailsLabel;
    @FXML private Label mainTitleLabel;

    @FXML private Button playBtn;
    @FXML private Button pauseBtn;
    @FXML private Button stopBtn;
    @FXML private Slider progressSlider;

    @FXML private ListView<String> playlistListView;
    @FXML private TextField playlistNameField;
    @FXML private Button createPlaylistBtn;

    private Media media;
    private MediaPlayer mediaPlayer;

    private final Map<String, SongMetadata> metadataMap = new HashMap<>();
    private boolean isUpdatingPlaylists = false;
    private final LibreriaController libreriaController = new LibreriaController();
    private Stage primaryStage;
    
    private Playlist currentPlaylist;

    // Riferimenti ai TextField attivi nel Dialog corrente (per evidenziazione errori)
    private TextField currentTitleField;
    private TextField currentAuthorField;
    private TextField currentGenreField;
    private TextField currentYearField;
    private TextField currentDurationField;

    public void setPrimaryStage(Stage s) { this.primaryStage = s; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), "Libreria"));
        } catch (IOException ignored) {}

        libreriaController.setView(this);
        Libreria.getInstance().addObserver(this);
        libreriaController.caricaDaCSV();
        MetadataService.caricaMappaDalCSV(metadataMap);
        refreshList();

        addBtn.setOnAction(e -> addSong());
        editBtn.setOnAction(e -> editSelected());
        deleteBtn.setOnAction(e -> deleteSelected());
        playBtn.setOnAction(e -> playSelected());
        pauseBtn.setOnAction(e -> { if (mediaPlayer != null) mediaPlayer.pause(); });
        stopBtn.setOnAction(e -> stopPlayback());

        createPlaylistBtn.setOnAction(e -> {
            String nome = playlistNameField.getText();
            libreriaController.creaPlaylist(nome);
        });
        
        mostraPlaylist(libreriaController.getPlaylist());

        setPlaybackControlsDisabled(true);

        songListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            showDetails(n);
            setPlaybackControlsDisabled(n == null);
        });

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingPlaylists) return;
            if (newVal != null) {
                currentPlaylist = findPlaylistByName(extractPlaylistName(newVal));
            } else {
                currentPlaylist = null;
            }
            updatePlaylistHeader();
            refreshList();
        });

        // CellFactory: inserisce il pulsante "⋮" in ogni riga
        songListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                private final HBox container    = new HBox();
                private final Label labelTesto  = new Label();
                private final Button btnOpzioni = new Button("⋮");
                private final ContextMenu menu  = new ContextMenu();

                {
                    btnOpzioni.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
                    HBox.setHgrow(labelTesto, Priority.ALWAYS);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    btnOpzioni.setFocusTraversable(false);
                    container.getChildren().addAll(labelTesto, btnOpzioni);

                    btnOpzioni.setOnAction(e -> {
                        songListView.getSelectionModel().select(getIndex());
                        
                        Stato stato = (currentPlaylist == null) ? new StatoLibreria() : new StatoPlaylist(currentPlaylist);
                        MenuContestuale menuC = new MenuContestuale(stato);
                        
                        // Chiama il metodo per generare le voci dinamicamente e aprirlo sul btnOpzioni
                        menuC.apriMenuSingolo(
                            findBranoByFilename(extractFilename(getItem())), 
                            btnOpzioni, 
                            LibreriaView.this::handleMenuAzione
                        );
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); }
                    else { labelTesto.setText(item); setGraphic(container); }
                }
            };
            return cell;
        });

        // CellFactory: inserisce il pulsante "⋮" anche nelle playlist
        playlistListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                private final HBox container    = new HBox();
                private final Label labelTesto  = new Label();
                private final Button btnOpzioni = new Button("⋮");
                private final ContextMenu menu  = new ContextMenu();

                {
                    btnOpzioni.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
                    HBox.setHgrow(labelTesto, Priority.ALWAYS);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    btnOpzioni.setFocusTraversable(false);
                    container.getChildren().addAll(labelTesto, btnOpzioni);

                    MenuItem miRinomina = new MenuItem("Rinomina");
                    miRinomina.setOnAction(e -> handleRinominaPlaylist());
                    
                    MenuItem miElimina = new MenuItem("Elimina");
                    miElimina.setOnAction(e -> handleEliminaPlaylist());

                    MenuItem miAggiungi = new MenuItem("Aggiungi Brano");
                    miAggiungi.setOnAction(e -> handleAggiungiBranoAllaPlaylist());

                    menu.getItems().addAll(miRinomina, miElimina, miAggiungi);

                    btnOpzioni.setOnAction(e -> {
                        playlistListView.getSelectionModel().select(getIndex());
                        menu.show(btnOpzioni, javafx.geometry.Side.BOTTOM, 0, 0);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); }
                    else { labelTesto.setText(item); setGraphic(container); }
                }
            };
            return cell;
        });
    }

    // =========================================================================
    // Riproduzione
    // =========================================================================

    private void setPlaybackControlsDisabled(boolean disabled) {
        playBtn.setDisable(disabled);
        pauseBtn.setDisable(disabled);
        stopBtn.setDisable(disabled);
        progressSlider.setDisable(disabled);
    }

    private void playSelected() {
        String sel = songListView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleziona prima un brano.", Alert.AlertType.WARNING); return; }

        String fn = extractFilename(sel);
        Path file = Path.of(System.getProperty("user.dir"), "Libreria", fn);
        if (!Files.exists(file)) { showAlert("File non trovato: " + file, Alert.AlertType.ERROR); return; }

        try {
            String uri = file.toUri().toString();
            if (mediaPlayer != null && !uri.equals(mediaPlayer.getMedia().getSource())) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                media = null;
            }
            if (mediaPlayer == null) {
                media = new Media(uri);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setOnReady(() -> progressSlider.setMax(media.getDuration().toSeconds()));
                mediaPlayer.currentTimeProperty().addListener((obs, o, n) -> {
                    if (!progressSlider.isValueChanging()) progressSlider.setValue(n.toSeconds());
                });
                progressSlider.valueProperty().addListener((obs, o, n) -> {
                    if (progressSlider.isValueChanging() && mediaPlayer != null)
                        mediaPlayer.seek(Duration.seconds(n.doubleValue()));
                });
                mediaPlayer.setOnEndOfMedia(this::stopPlayback);
            }
            mediaPlayer.play();
            setPlaybackControlsDisabled(false);
        } catch (Exception ex) {
            mostraErrore(new ValidazioneException("Errore riproduzione: " + ex.getMessage()));
        }
    }

    private void stopPlayback() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                media = null;
            }
        } finally {
            setPlaybackControlsDisabled(true);
        }
    }

    // =========================================================================
    // Lista e dettagli
    // =========================================================================

    private void refreshList() {
        songListView.getItems().clear();
        List<IBrano> braniDaMostrare = (currentPlaylist != null) ? currentPlaylist.getBrani() : libreriaController.getBrani();
        for (IBrano ib : braniDaMostrare) {
            if (ib instanceof Brano b) {
                String fn = PathUtils.filenameFromPath(b.getPercorsoFile());
                String display = (b.getTitolo() != null && !b.getTitolo().isBlank())
                        ? b.getTitolo() + " — " + fn : fn;
                songListView.getItems().add(display);
            }
        }

        if (songListView.getItems().isEmpty()) {
            detailsLabel.setText(currentPlaylist != null ? "La playlist è vuota" : "La libreria è vuota");
        } else {
            detailsLabel.setText(currentPlaylist != null ? 
                "Playlist '" + currentPlaylist.getNome() + "' (" + braniDaMostrare.size() + " brani)" : 
                "Libreria generale (" + braniDaMostrare.size() + " brani). Seleziona un brano per i dettagli.");
        }
    }

    private void showDetails(String display) {
        if (display == null) { detailsLabel.setText("Seleziona un brano per i dettagli."); return; }
        String fn = extractFilename(display);
        SongMetadata m = metadataMap.get(fn);
        if (m == null) {
            detailsLabel.setText(fn);
        } else {
            detailsLabel.setText(
                "Titolo: "  + m.title +
                "\nAutore: " + m.author +
                "\nGenere: " + m.genre +
                "\nAnno: "   + (m.year  == null || m.year.isBlank()  ? "N/D"    : m.year) +
                "\nTag: "    + (m.tag   == null || m.tag.isBlank()   ? "Nessuno" : m.tag));
        }
    }

    // =========================================================================
    // CRUD brani
    // =========================================================================

    private void addSong() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 files", "*.mp3"));
        File f = fc.showOpenDialog(primaryStage);
        if (f == null) return;

        final String[] values = new String[4];
        MetadataService.estraiMetadati(f, v -> {
            for (int i = 0; i < Math.min(v.length, values.length); i++) values[i] = v[i];
        });

        String title    = values[0] != null ? values[0] : MetadataService.stripExtension(f.getName());
        String author   = values[1] != null ? values[1] : "";
        String genre    = values[2] != null ? values[2] : "";
        String duration = values[3] != null ? values[3] : "0";

        Dialog<ButtonType> d = buildDialog("Importa brano");
        GridPaneHelper g = new GridPaneHelper();
        TextField tFilename = g.addRow("Filename:", f.getName()); tFilename.setEditable(false);
        currentTitleField    = g.addRow("Titolo:",   title);
        currentAuthorField   = g.addRow("Autore:",   author);
        currentYearField     = g.addRow("Anno:",     "");
        currentDurationField = g.addRow("Durata (s):", duration);
        currentGenreField    = g.addRow("Genere:",   genre);
        TextField tTag       = g.addRow("Tag:",      "");
        d.getDialogPane().setContent(g.grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (d.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                int annoParsed  = parseIntField(currentYearField.getText(),     "anno",   "L'anno deve essere un numero intero.");
                int durataParsed = parseIntField(currentDurationField.getText(), "durata", "La durata deve essere in secondi numerici.");
                libreriaController.aggiungiBrano(
                        currentTitleField.getText(), currentAuthorField.getText(),
                        currentGenreField.getText(), annoParsed,
                        f.getAbsolutePath(), durataParsed,
                        Tag.fromString(tTag.getText())
                );
                syncAndRefresh();
            } catch (ValidazioneException ve) { mostraErrore(ve); }
            catch (Exception ex)              { mostraErrore(new ValidazioneException("Errore importazione: " + ex.getMessage())); }
        }
    }

    private void editSelected() {
        String sel = songListView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleziona prima un brano.", Alert.AlertType.ERROR); return; }

        String fn = extractFilename(sel);
        SongMetadata m = metadataMap.get(fn);
        if (m == null) m = new SongMetadata(fn, MetadataService.stripExtension(fn), "", "", "");

        Dialog<ButtonType> d = buildDialog("Modifica brano");
        GridPaneHelper g = new GridPaneHelper();
        currentTitleField    = g.addRow("Titolo:",     m.title);
        currentAuthorField   = g.addRow("Autore:",     m.author);
        currentYearField     = g.addRow("Anno:",       m.year     == null ? "" : m.year);
        currentGenreField    = g.addRow("Genere:",     m.genre);
        currentDurationField = g.addRow("Durata (s):", m.duration == null ? "" : m.duration);
        // Pre-popola il campo Tag con la stringa RAW esistente (es. "RELAX, Preferiti")
        TextField tTag       = g.addRow("Tag:",        m.tag      == null ? "" : m.tag);
        d.getDialogPane().setContent(g.grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (d.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Brano branoTarget = findBranoByFilename(fn);
                if (branoTarget == null) { mostraErrore(new ValidazioneException("Brano non trovato in memoria.")); return; }

                Map<String, String> dati = new HashMap<>();
                dati.put("titolo",  currentTitleField.getText());
                dati.put("autore",  currentAuthorField.getText());
                dati.put("anno",    currentYearField.getText());
                dati.put("genere",  currentGenreField.getText());
                dati.put("durata",  currentDurationField.getText());
                dati.put("tag",     tTag.getText());   // stringa RAW passata direttamente

                libreriaController.modificaBrano(branoTarget, dati);
                syncAndRefresh();
                showDetails(songListView.getSelectionModel().getSelectedItem());
            } catch (ValidazioneException ve) { mostraErrore(ve); }
            catch (Exception ex)              { mostraErrore(new ValidazioneException("Errore aggiornamento: " + ex.getMessage())); }
        }
    }

    private void deleteSelected() {
        String sel = songListView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Seleziona prima un brano.", Alert.AlertType.ERROR); return; }

        String fn = extractFilename(sel);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Eliminare definitivamente '" + fn + "'?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (mediaPlayer != null) stopPlayback();
                libreriaController.eliminaBranoPerFilename(fn);
                syncAndRefresh();
            } catch (Exception ex) {
                mostraErrore(new ValidazioneException("Errore eliminazione: " + ex.getMessage()));
            }
        }
    }

    // =========================================================================
    // Menu contestuale (⋮)
    // =========================================================================

    private void handleMenuAzione(String opzione) {
        Brano selezionato = getBranoSelezionatoDallaLista();
        if (selezionato == null) return;

        switch (opzione) {
            case "Modifica"          -> editSelected();
            case "Elimina brano"     -> deleteSelected();
            case "Aggiungi a playlist" -> new StatoLibreria().eseguiOpzione(opzione, selezionato, libreriaController);
            case "Rimuovi da questa playlist" -> {
                if (currentPlaylist != null) {
                    new StatoPlaylist(currentPlaylist).eseguiOpzione(opzione, selezionato, libreriaController);
                }
            }

            case "Aggiungi tag" -> {
                // Mostra i tag già presenti come suggerimento
                String fn = extractFilename(songListView.getSelectionModel().getSelectedItem());
                SongMetadata m = metadataMap.get(fn);
                String tagEsistente = (m != null && m.tag != null && !m.tag.isBlank()
                        && !m.tag.equalsIgnoreCase("NESSUNO")) ? m.tag : "";

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Aggiungi Tag");
                dialog.setHeaderText("Brano: " + selezionato.getTitolo()
                        + (tagEsistente.isEmpty() ? "" : "\nTag attuali: " + tagEsistente));
                dialog.setContentText("Nuovo tag (es. Preferiti):");

                dialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(nuovoTag -> {
                    // Concatena alla stringa esistente
                    String stringaFinale = tagEsistente.isEmpty()
                            ? nuovoTag
                            : tagEsistente + ", " + nuovoTag;
                    try {
                        // Usa il metodo dedicato che scrive la stringa RAW nel CSV
                        libreriaController.modificaTagBrano(selezionato, stringaFinale);
                        // Ricarica metadataMap e aggiorna i dettagli (NON ri-renderizza la lista)
                        MetadataService.caricaMappaDalCSV(metadataMap);
                        showDetails(songListView.getSelectionModel().getSelectedItem());
                    } catch (Exception ex) {
                        mostraErrore(new ValidazioneException("Impossibile aggiornare il tag: " + ex.getMessage()));
                    }
                });
            }
        }
    }

    // =========================================================================
    // Azioni Playlist
    // =========================================================================

    private void handleRinominaPlaylist() {
        String sel = playlistListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        String name = extractPlaylistName(sel);
        Playlist p = findPlaylistByName(name);
        if (p == null) return;

        TextInputDialog dialog = new TextInputDialog(p.getNome());
        dialog.setTitle("Rinomina Playlist");
        dialog.setHeaderText("Modifica il nome della playlist");
        dialog.setContentText("Nome:");
        
        // Il pulsante OK c'è di default in TextInputDialog, ed equivale a Conferma
        // Il pulsante Annulla c'è di default e chiude il dialog
        dialog.showAndWait().ifPresent(nuovoNome -> {
            libreriaController.rinominaPlaylist(p, nuovoNome);
        });
    }

    private void handleEliminaPlaylist() {
        String sel = playlistListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        String name = extractPlaylistName(sel);
        Playlist p = findPlaylistByName(name);
        if (p == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Elimina Playlist");
        alert.setHeaderText("Conferma eliminazione");
        alert.setContentText("Sei sicuro di voler eliminare la playlist '" + p.getNome() + "'?");

        ButtonType btnElimina = new ButtonType("Elimina", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnElimina, btnAnnulla);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnElimina) {
                libreriaController.eliminaPlaylist(p);
            }
        });
    }

    private void handleAggiungiBranoAllaPlaylist() {
        String sel = playlistListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        String name = extractPlaylistName(sel);
        Playlist p = findPlaylistByName(name);
        if (p == null) return;

        // Trova i brani non ancora nella playlist
        List<IBrano> disponibili = libreriaController.getBrani().stream()
                .filter(b -> !p.contieneBrano(b))
                .toList();

        if (disponibili.isEmpty()) {
            mostraErrore(new ValidazioneException("Tutti i brani sono già presenti in questa playlist.", ValidazioneException.TipoErrore.GENERICO, ""));
            return;
        }

        Dialog<IBrano> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi Brano");
        dialog.setHeaderText("Seleziona il brano da aggiungere alla playlist '" + p.getNome() + "'");

        ListView<String> lv = new ListView<>();
        List<String> displayNames = new java.util.ArrayList<>();
        for (IBrano ib : disponibili) {
            if (ib instanceof Brano brano) {
                String fn = PathUtils.filenameFromPath(brano.getPercorsoFile());
                displayNames.add((brano.getTitolo() != null && !brano.getTitolo().isBlank()) ? brano.getTitolo() + " — " + fn : fn);
            }
        }
        lv.getItems().addAll(displayNames);
        lv.setPrefSize(300, 250);

        dialog.getDialogPane().setContent(lv);

        ButtonType btnAggiungi = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAggiungi, btnAnnulla);

        // Disabilita "Aggiungi" finché non si seleziona qualcosa
        javafx.scene.Node btnAgg = dialog.getDialogPane().lookupButton(btnAggiungi);
        btnAgg.setDisable(true);
        lv.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            btnAgg.setDisable(newV == null);
        });

        btnAgg.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            int idx = lv.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                IBrano branoScelto = disponibili.get(idx);
                try {
                    libreriaController.aggiungiBranoAPlaylist(p, branoScelto);
                    // Se successo, non consumiamo l'evento: il dialog si chiude da solo.
                } catch (ValidazioneException ex) {
                    // Se c'è un errore (es. DUPLICATO), fermiamo la chiusura del dialog
                    event.consume();
                    mostraErrore(ex);
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnAggiungi) {
                int idx = lv.getSelectionModel().getSelectedIndex();
                if (idx >= 0) return disponibili.get(idx);
            }
            return null;
        });

        dialog.showAndWait();
    }

    // =========================================================================
    // Gestione errori
    // =========================================================================

    public void mostraErrore(ValidazioneException ex) {
        javafx.application.Platform.runLater(() -> {
            resetBordiCampi();
            String header;
            if (ex.getTipo() == ValidazioneException.TipoErrore.CAMPO_MANCANTE)      header = "Campo Obbligatorio Mancante";
            else if (ex.getTipo() == ValidazioneException.TipoErrore.FORMATO_NON_VALIDO) header = "Formato o Range Non Valido";
            else                                                                       header = "Errore di Validazione";

            if (ex.getCampoErrato() != null) {
                switch (ex.getCampoErrato().toLowerCase()) {
                    case "titolo" -> lightenRosso(currentTitleField);
                    case "autore" -> lightenRosso(currentAuthorField);
                    case "genere" -> lightenRosso(currentGenreField);
                    case "anno"   -> lightenRosso(currentYearField);
                    case "durata" -> lightenRosso(currentDurationField);
                }
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore nei Dati");
            alert.setHeaderText(header);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        });
    }

    private void lightenRosso(TextField tf) {
        if (tf != null) tf.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 3px;");
    }

    private void resetBordiCampi() {
        String s = "-fx-border-color: transparent; -fx-border-width: 0;";
        if (currentTitleField    != null) currentTitleField.setStyle(s);
        if (currentAuthorField   != null) currentAuthorField.setStyle(s);
        if (currentYearField     != null) currentYearField.setStyle(s);
        if (currentDurationField != null) currentDurationField.setStyle(s);
        if (currentGenreField    != null) currentGenreField.setStyle(s);
    }

    @Override
    public void onPlaylistAggiornata() {
        javafx.application.Platform.runLater(() -> {
            mostraPlaylist(libreriaController.getPlaylist());
            playlistNameField.clear();
            showAlert("Playlist aggiornata con successo", Alert.AlertType.INFORMATION);
        });
    }

    public void mostraPlaylist(List<Playlist> playlists) {
        isUpdatingPlaylists = true;
        Playlist toSelect = currentPlaylist;
        playlistListView.getItems().clear();
        boolean found = false;
        for (Playlist p : playlists) {
            String display = p.getNome() + " (" + p.getBrani().size() + " brani)";
            playlistListView.getItems().add(display);
            if (toSelect != null && p == toSelect) {
                found = true;
                playlistListView.getSelectionModel().select(display);
            }
        }
        isUpdatingPlaylists = false;

        if (!found && toSelect != null) {
            currentPlaylist = null;
            javafx.application.Platform.runLater(this::refreshList);
        } else if (found) {
            // Selezionato di nuovo la playlist corrente: forziamo l'aggiornamento
            // della tabella brani senza essere passati da null (che caricherebbe tutto)
            javafx.application.Platform.runLater(this::refreshList);
        }
        updatePlaylistHeader();
    }

    private void updatePlaylistHeader() {
        if (mainTitleLabel != null) {
            if (currentPlaylist != null) {
                mainTitleLabel.setText("Playlist: " + currentPlaylist.getNome());
            } else {
                mainTitleLabel.setText("SonicWave — Gestione brani");
            }
        }
    }

    // =========================================================================
    // Utility privati
    // =========================================================================

    /** Estrae il filename dall'elemento visualizzato nella lista (formato "Titolo — filename"). */
    private String extractFilename(String display) {
        if (display == null) return "";
        String raw = display.contains(" — ") ? display.substring(display.lastIndexOf(" — ") + 3) : display;
        return PathUtils.filenameFromPath(raw);
    }

    /** Trova l'oggetto Brano in RAM a partire dal filename. */
    private Brano findBranoByFilename(String fn) {
        for (IBrano ib : libreriaController.getBrani()) {
            if (ib instanceof Brano b && PathUtils.filenameFromPath(b.getPercorsoFile()).equals(fn))
                return b;
        }
        return null;
    }

    /** Brano corrispondente all'elemento selezionato nella lista. */
    private Brano getBranoSelezionatoDallaLista() {
        String sel = songListView.getSelectionModel().getSelectedItem();
        if (sel == null) return null;
        return findBranoByFilename(extractFilename(sel));
    }

    private String extractPlaylistName(String display) {
        if (display == null) return "";
        int idx = display.lastIndexOf(" (");
        if (idx != -1) {
            return display.substring(0, idx);
        }
        return display;
    }
    
    private Playlist findPlaylistByName(String name) {
        for (Playlist p : libreriaController.getPlaylist()) {
            if (p.getNome().equals(name)) return p;
        }
        return null;
    }

    /** Ricarica metadataMap dal CSV e aggiorna la lista. */
    private void syncAndRefresh() {
        MetadataService.caricaMappaDalCSV(metadataMap);
        refreshList();
    }

    /** Parsing int con ValidazioneException tipizzata. */
    private int parseIntField(String text, String campo, String messaggio)
            throws ValidazioneException {
        if (text == null || text.isBlank()) return 0;
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) {
            throw new ValidazioneException(messaggio,
                    ValidazioneException.TipoErrore.FORMATO_NON_VALIDO, campo);
        }
    }

    private Dialog<ButtonType> buildDialog(String titolo) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(titolo);
        return d;
    }

    private void showAlert(String msg, Alert.AlertType type) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    // =========================================================================
    // GridPaneHelper
    // =========================================================================

    private static class GridPaneHelper {
        final javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        private int row = 0;
        GridPaneHelper() {
            grid.setHgap(8);
            grid.setVgap(8);
            grid.setPadding(new javafx.geometry.Insets(12));
        }
        TextField addRow(String label, String value) {
            TextField tf = new TextField(value == null ? "" : value);
            grid.addRow(row++, new Label(label), tf);
            return tf;
        }
    }
}