package com.musicplayer;

import com.musicplayer.persistence.MetadataService;
import com.musicplayer.persistence.SongMetadata;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

public class LibreriaView implements Initializable, LibreriaObserver {

    @FXML
    private Label mainTitleLabel;
    @FXML
    private Label currentTimeLabel; // Tempo che scorre
    @FXML
    private Label totalTimeLabel; // Durata totale
    @FXML
    private ListView<String> songListView;
    @FXML
    private ListView<String> playlistListView;

    @FXML
    private Button addBtn;
    @FXML
    private Button btnMostraLibreria;

    @FXML
    private Button playBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button shuffleBtn;
    @FXML
    private Button loopBtn;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label detailsLabel;
    @FXML
    private Label playingTitleLabel;
    @FXML
    private Button btnHeart;
    @FXML
    private Label playingAuthorLabel;
    @FXML
    private Label nextSongLabel;

    private boolean isProgrammaticSelection = false;

    @FXML
    private Button btnApriCreazione;
    @FXML
    private VBox viewLista;
    @FXML
    private VBox viewCreazione;
    @FXML
    private VBox viewAggiuntaBrano;
    @FXML
    private TextField playlistNameField;
    @FXML
    private Button createPlaylistBtn;
    @FXML
    private Button btnAnnullaCreazione;

    // -- Campi Ricerca --
    @FXML
    private TextField searchTitoloField;
    @FXML
    private TextField searchAutoreField;
    @FXML
    private ComboBox<String> searchAnnoCombo;
    @FXML
    private ComboBox<String> searchGenereCombo;
    @FXML
    private ComboBox<String> searchTagCombo;
    @FXML
    private Button resetSearchBtn;

    private final FiltroRicerca filtroAttivo = new FiltroRicerca();

    // -- Campi view aggiunta brano --
    @FXML
    private Label lblImportTitle;
    @FXML
    private Label lblImportFilename;
    @FXML
    private TextField importTitleField;
    @FXML
    private TextField importAuthorField;
    @FXML
    private TextField importYearField;
    @FXML
    private javafx.scene.layout.VBox genreContainer;
    @FXML
    private javafx.scene.layout.VBox tagContainer;
    @FXML
    private Button btnAnnullaImport;
    @FXML
    private Button btnConfermaImport;

    @FXML
    private VBox viewSelezionePlaylist;
    @FXML
    private ListView<String> playlistSelectionListView;
    @FXML
    private Button btnAnnullaSelezione;
    @FXML
    private Button btnConfermaSelezione;

    // Costruito dinamicamente per il form
    private FormBranoView formBranoView;

    private GestoreRiproduzione gestoreRiproduzione;
    private PlayerView playerView;
    private PlaylistView playlistView;
    private final Map<String, SongMetadata> metadataMap = new HashMap<>();
    private final LibreriaController libreriaController = new LibreriaController();
    private Stage primaryStage;
    private String playlistSelezionata = null;
    private Brano branoInAttesaDiPlaylist = null;
    private TextField currentTitleField;
    private TextField currentAuthorField;
    private TextField currentGenreField;
    private TextField currentYearField;

    public Map<String, SongMetadata> getMetadataMap() {
        return metadataMap;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        formBranoView = new FormBranoView(
                importTitleField, importAuthorField, importYearField,
                lblImportTitle, lblImportFilename, btnConfermaImport,
                genreContainer, tagContainer,
                libreriaController, () -> {
                    metadataMap.clear();
                    MetadataService.caricaMappaDalCSV(metadataMap);
                    switchToView(viewLista);
                    mostraLibreriaGenerale();
                }, primaryStage);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), "Libreria"));
        } catch (IOException ignored) {
            // Ignored intentionally
        }

        libreriaController.addObserver(this);
        libreriaController.caricaDaCSV();
        MetadataService.caricaMappaDalCSV(metadataMap);

        this.playlistView = new PlaylistView(playlistListView, viewLista, viewCreazione,
                playlistNameField, createPlaylistBtn, btnAnnullaCreazione, btnApriCreazione,
                libreriaController, this);
        this.playlistView.initialize();

        popolaComboAnno();
        popolaComboGenere();
        popolaComboTag();

        searchTitoloField.textProperty().addListener((obs, oldV, newV) -> applicaFiltro());
        searchAutoreField.textProperty().addListener((obs, oldV, newV) -> applicaFiltro());
        searchAnnoCombo.valueProperty().addListener((obs, oldV, newV) -> applicaFiltro());
        searchGenereCombo.valueProperty().addListener((obs, oldV, newV) -> applicaFiltro());
        searchTagCombo.valueProperty().addListener((obs, oldV, newV) -> applicaFiltro());

        resetSearchBtn.setOnAction(e -> azzeraFiltro());

        refreshList();
        refreshPlaylistList();

        playlistSelectionListView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    if (isSelected()) {
                        setStyle(
                                "-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 4;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-padding: 10;");
                    }
                }
            }
        });

        btnMostraLibreria.setOnAction(e -> {
            switchToView(viewLista);
            mostraLibreriaGenerale();
        });

        btnHeart.setOnAction(e -> {
            String currentFilename = null;
            if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
                currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName().toString();
            } else {
                String sel = songListView.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    currentFilename = extractFilename(sel);
                }
            }

            if (currentFilename != null) {
                try {
                    Brano brano = findBranoByFilename(currentFilename);
                    if (brano != null) {
                        SongMetadata m = metadataMap.get(currentFilename);
                        String tagCorrente = (m != null && m.tag != null) ? m.tag
                                : (brano.getTag() != null && brano.getTag() != Tag.NESSUNO
                                        ? brano.getTag().getEtichetta()
                                        : "");

                        String tagAggiornato;
                        if (tagCorrente.contains("Preferiti")) {
                            tagAggiornato = java.util.Arrays.stream(tagCorrente.split(","))
                                    .map(String::trim)
                                    .filter(t -> !t.equals("Preferiti"))
                                    .collect(java.util.stream.Collectors.joining(", "));
                            if (tagAggiornato.isEmpty())
                                tagAggiornato = "NESSUNO";
                        } else {
                            tagAggiornato = tagCorrente.isEmpty() || tagCorrente.equals("NESSUNO") ? "Preferiti"
                                    : tagCorrente + ", Preferiti";
                        }
                        libreriaController.modificaTagBrano(brano, tagAggiornato);

                        com.musicplayer.persistence.MetadataService.caricaMappaDalCSV(metadataMap);
                        refreshList();
                        aggiornaStatoCuore(currentFilename);
                    }
                } catch (Exception ex) {
                    mostraErrore(
                            new ValidazioneException("Errore nell'aggiornamento dei preferiti: " + ex.getMessage()));
                }
            }
        });

        addBtn.setOnAction(e -> {
            if (formBranoView != null) {
                formBranoView.addSong();
                switchToView(viewAggiuntaBrano);
            }
        });

        btnAnnullaImport.setOnAction(e -> {
            if (formBranoView != null)
                formBranoView.resetImportView();
            switchToView(viewLista);
            mostraLibreriaGenerale();
        });
        btnAnnullaSelezione.setOnAction(e -> switchToView(viewLista));

        btnConfermaSelezione.setOnAction(e -> {
            String nomePlaylist = playlistSelectionListView.getSelectionModel().getSelectedItem();
            if (nomePlaylist != null && branoInAttesaDiPlaylist != null) {
                try {
                    libreriaController.aggiungiAPlaylist(branoInAttesaDiPlaylist, nomePlaylist);
                    switchToView(viewLista);
                    detailsLabel.setText("Brano aggiunto a " + nomePlaylist);
                } catch (ValidazioneException ve) {
                    mostraErrore(ve);
                }
            }
        });
        btnConfermaImport.setOnAction(e -> {
            try {
                if (formBranoView != null) {
                    formBranoView.confermaImportBrano();
                }
            } catch (Exception ve) {
                if (ve instanceof ValidazioneException) {
                    mostraErrore((ValidazioneException) ve);
                } else {
                    mostraErrore(new ValidazioneException("Errore salvataggio: " + ve.getMessage()));
                }
            }
        });

        btnAnnullaSelezione.setOnAction(e -> {
            if (branoInAttesaDiPlaylist != null) {
                branoInAttesaDiPlaylist = null;
            }
            refreshList();
            switchToView(viewLista);
        });

        btnConfermaSelezione.setOnAction(e -> {
            String nomePlaylistSelezionato = playlistSelectionListView.getSelectionModel().getSelectedItem();

            // Verifica che abbiamo sia la playlist che il brano
            if (nomePlaylistSelezionato != null && branoInAttesaDiPlaylist != null) {
                // Salva il contesto di provenienza prima del reset
                String contestoProvenienza = playlistSelezionata;
                try {
                    // Usa il nome corretto del metodo che hai nel Controller: aggiungiAPlaylist
                    // Firma corretta: aggiungiAPlaylist(Brano, String)
                    libreriaController.aggiungiAPlaylist(branoInAttesaDiPlaylist, nomePlaylistSelezionato);

                    // Reset stato
                    branoInAttesaDiPlaylist = null;

                    // Aggiorna l'interfaccia
                    refreshList();
                    refreshPlaylistList();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Brano aggiunto con successo!");
                    alert.show();

                    switchToView(viewLista);
                    // Torna alla playlist di provenienza, oppure alla libreria generale
                    if (contestoProvenienza != null) {
                        impostaPlaylist(contestoProvenienza);
                    } else {
                        mostraLibreriaGenerale();
                    }

                } catch (ValidazioneException ve) {
                    mostraErrore(ve);
                } catch (Exception ex) {
                    mostraErrore(new ValidazioneException("Errore durante l'aggiunta: " + ex.getMessage()));
                }
            } else if (nomePlaylistSelezionato == null) {
                showAlert("Seleziona una playlist dalla lista!", Alert.AlertType.WARNING);
            }
        });
        // Inizializza GestoreRiproduzione e PlayerView
        this.gestoreRiproduzione = GestoreRiproduzione.getInstance();
        this.playerView = new PlayerView(playBtn, stopBtn, shuffleBtn, loopBtn, currentTimeLabel, totalTimeLabel,
                progressSlider,
                gestoreRiproduzione, this);

        this.gestoreRiproduzione.addObserver(new RiproduzioneObserver() {
            @Override
            public void onPlayerReady(int durataSecondi) {
            }

            @Override
            public void onPlay() {
                aggiornaVisualizzazioneCoda();
            }

            @Override
            public void onPausa() {
            }

            @Override
            public void onStop() {
                aggiornaVisualizzazioneCoda();
            }

            @Override
            public void onProgressoAggiornato(int secondi) {
            }

            @Override
            public void onBranoCambiato(String nuovoPercorso) {
                javafx.application.Platform.runLater(() -> {
                    Path p = Path.of(nuovoPercorso);
                    String fn = p.getFileName().toString();
                    SongMetadata m = metadataMap.get(fn);
                    if (m != null) {
                        playingTitleLabel.setText(m.title != null && !m.title.isBlank() ? m.title : fn);
                        playingAuthorLabel
                                .setText(m.author != null && !m.author.isBlank() ? m.author : "Autore sconosciuto");
                    } else {
                        playingTitleLabel.setText(fn);
                        playingAuthorLabel.setText("Autore sconosciuto");
                    }

                    aggiornaStatoCuore(fn);

                    String itemToSelect = null;
                    for (String item : songListView.getItems()) {
                        if (extractFilename(item).equals(fn)) {
                            itemToSelect = item;
                            break;
                        }
                    }
                    if (itemToSelect != null) {
                        isProgrammaticSelection = true;
                        songListView.getSelectionModel().select(itemToSelect);
                        isProgrammaticSelection = false;
                    }
                    aggiornaVisualizzazioneCoda();
                });
            }
        });

        songListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    showDetails(newVal);
                    if (isProgrammaticSelection)
                        return;
                    if (newVal != null && !newVal.equals(oldVal)) {
                        if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                            playSelected();
                        }
                    }
                });

        songListView.setCellFactory(creaCellFactoryTrePuntini(new StatoLibreria()));

        javafx.application.Platform.runLater(() -> {
            detailsLabel.setStyle(
                    "-fx-background-color: #181818; -fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-line-spacing: 8px; -fx-padding: 12px;");
            detailsLabel.setMinHeight(250);
            detailsLabel.setMaxWidth(Double.MAX_VALUE);

            javafx.scene.Parent parentNode = detailsLabel.getParent();
            while (parentNode != null) {
                if (parentNode instanceof ScrollPane sp) {
                    sp.setStyle(
                            "-fx-background: #181818; -fx-background-color: #181818; -fx-border-color: transparent;");
                    sp.setFitToHeight(true);
                    sp.setFitToWidth(true);
                    break;
                }
                parentNode = parentNode.getParent();
            }
        });
    }

    public void switchToView(VBox viewToShow) {
        // 1. Spegni e "nascondi" TUTTE le possibili viste
        viewLista.setVisible(false);
        viewLista.setManaged(false);

        viewCreazione.setVisible(false);
        viewCreazione.setManaged(false);

        viewAggiuntaBrano.setVisible(false);
        viewAggiuntaBrano.setManaged(false);

        // AGGIUNGI QUESTA RIGA:
        viewSelezionePlaylist.setVisible(false);
        viewSelezionePlaylist.setManaged(false);

        // 2. Accendi solo quella passata come parametro
        viewToShow.setVisible(true);
        viewToShow.setManaged(true);
    }

    private javafx.util.Callback<ListView<String>, ListCell<String>> creaCellFactoryTrePuntini(StatoUI statoAttuale) {
        return lv -> new ListCell<>() {
            private final HBox container = new HBox();

            private final Label lblId = new Label();
            private final Label lblTitolo = new Label();
            private final Label lblAutore = new Label();
            private final Label lblGenere = new Label();
            private final Label lblDurata = new Label();
            private final Pane spacer = new Pane();
            private final Button btnOpzioni = new Button("⋮");
            private final ContextMenu menu = new ContextMenu();

            {
                lblId.setMinWidth(40);
                lblId.setPrefWidth(40);
                lblId.setMaxWidth(40);

                lblTitolo.setMinWidth(280);
                lblTitolo.setPrefWidth(280);
                lblTitolo.setMaxWidth(280);

                lblAutore.setMinWidth(180);
                lblAutore.setPrefWidth(180);
                lblAutore.setMaxWidth(180);

                lblGenere.setMinWidth(120);
                lblGenere.setPrefWidth(120);
                lblGenere.setMaxWidth(120);

                lblDurata.setMinWidth(60);
                lblDurata.setPrefWidth(60);
                lblDurata.setMaxWidth(60);

                HBox.setHgrow(spacer, Priority.ALWAYS);

                btnOpzioni.setFocusTraversable(false);

                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
                container.setSpacing(10);

                container.getChildren().addAll(lblId, lblTitolo, lblAutore, lblGenere, spacer, lblDurata, btnOpzioni);

                btnOpzioni.setOnAction(e -> {
                    e.consume();
                    menu.getItems().clear();
                    String sel = getItem();
                    if (sel != null) {
                        Brano selezionato = findBranoByFilename(extractFilename(sel));
                        if (selezionato != null) {
                            for (String label : statoAttuale.getOpzioniSingolo()) {
                                String finalLabel = label;
                                if (label.equals("Aggiungi ai preferiti")) {
                                    boolean isPref = false;
                                    SongMetadata m = metadataMap.get(extractFilename(sel));
                                    if (m != null && m.tag != null)
                                        isPref = m.tag.contains("Preferiti");
                                    else if (selezionato.getTag() != null)
                                        isPref = selezionato.getTag().getEtichetta().contains("Preferiti");
                                    finalLabel = isPref ? "Togli dai preferiti" : "Aggiungi ai preferiti";
                                }
                                MenuItem mi = new MenuItem(finalLabel);
                                mi.setOnAction(ev -> {
                                    if (label.equals("Modifica")) {
                                        editBrano(selezionato);
                                    } else {
                                        statoAttuale.eseguiOpzione(label, selezionato, libreriaController,
                                                LibreriaView.this);
                                    }
                                });
                                menu.getItems().add(mi);
                            }
                        }
                    }
                    menu.show(btnOpzioni, javafx.geometry.Side.BOTTOM, 0, 0);
                });

                selectedProperty().addListener((obs, o, isSelected) -> updateStyle(isSelected, isHover()));
                hoverProperty().addListener((obs, o, isHover) -> updateStyle(isSelected(), isHover));
            }

            private void updateStyle(boolean selected, boolean hovered) {
                if (selected) {
                    container.setStyle("-fx-background-color: #1DB954; -fx-background-radius: 6;");
                    lblId.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #000000; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else if (hovered) {
                    container.setStyle("-fx-background-color: #282828; -fx-background-radius: 6;");
                    lblId.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else {
                    container.setStyle("-fx-background-color: transparent;");
                    lblId.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #b3b3b3; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 8 4 8;");
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblId.setText(String.valueOf(getIndex() + 1));
                    String fn = extractFilename(item);
                    SongMetadata m = metadataMap.get(fn);

                    String displayTitle = item;
                    if (displayTitle.contains(" — ")) {
                        displayTitle = displayTitle.substring(0, displayTitle.lastIndexOf(" — "));
                    }
                    lblTitolo.setText(displayTitle);

                    if (m != null) {
                        lblAutore.setText(m.author != null ? m.author : "");
                        lblGenere.setText(m.genre != null ? m.genre : "");

                        String dur = "--:--";
                        try {
                            if (m.duration != null && !m.duration.isBlank()) {
                                int sec = Integer.parseInt(m.duration);
                                dur = String.format("%d:%02d", sec / 60, sec % 60);
                            }
                        } catch (Exception ignored) {
                        }
                        lblDurata.setText(dur);
                    } else {
                        lblAutore.setText("");
                        lblGenere.setText("");
                        lblDurata.setText("--:--");
                    }

                    setGraphic(container);
                    updateStyle(isSelected(), isHover());
                }
            }
        };
    }

    public void aggiornaCuorePreferiti() {
        if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
            try {
                String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
                String currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName()
                        .toString();
                aggiornaStatoCuore(currentFilename);
            } catch (Exception e) {
            }
        }
    }

    private void aggiornaStatoCuore(String fn) {
        if (fn == null) {
            btnHeart.setVisible(false);
            return;
        }
        btnHeart.setVisible(true);
        SongMetadata m = metadataMap.get(fn);
        boolean isPreferito = false;
        if (m != null && m.tag != null) {
            isPreferito = m.tag.contains("Preferiti");
        } else {
            Brano b = findBranoByFilename(fn);
            if (b != null && b.getTag() != null) {
                isPreferito = b.getTag().getEtichetta().contains("Preferiti");
            }
        }

        if (isPreferito) {
            btnHeart.setText("❤");
            btnHeart.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 24px; -fx-max-width: 24px; -fx-alignment: center;");
        } else {
            btnHeart.setText("♡");
            btnHeart.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a7a7a7; -fx-cursor: hand; -fx-padding: 0; -fx-min-width: 24px; -fx-max-width: 24px; -fx-alignment: center;");
        }
    }

    public void mostraLibreriaGenerale() {
        playlistSelezionata = null;
        playlistListView.getSelectionModel().clearSelection();
        addBtn.setVisible(true);
        addBtn.setManaged(true);
        refreshList();
        detailsLabel.setText("Libreria generale. Seleziona un brano per i dettagli.");
    }

    public void impostaPlaylist(String pName) {
        playlistSelezionata = pName;
        addBtn.setVisible(false);
        addBtn.setManaged(false);
        refreshList();
        detailsLabel.setText("Playlist: " + pName + "\nSeleziona un brano.");
    }

    public String getPlaylistSelezionata() {
        return playlistSelezionata;
    }

    public void refreshPlaylistList() {
        if (playlistView != null) {
            playlistView.refreshPlaylistList(playlistSelezionata);
        }
    }

    public void apriSelezionePlaylist(Brano b) {
        this.branoInAttesaDiPlaylist = b;

        // Verifica se ci sono playlist
        if (libreriaController.getPlaylist().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nessuna playlist trovata. Creane una!");
            alert.showAndWait();
            switchToView(viewCreazione); // Ti porta alla schermata di creazione
            return;
        }

        // Filtra la playlist corrente se siamo dentro a una playlist
        java.util.stream.Stream<String> stream = libreriaController.getPlaylist().stream().map(Playlist::getNome);
        if (playlistSelezionata != null) {
            stream = stream.filter(nome -> !nome.equals(playlistSelezionata));
        }

        java.util.List<String> opzioni = stream.toList();
        if (opzioni.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Non ci sono altre playlist disponibili.");
            alert.showAndWait();
            return;
        }

        // Carica le playlist nel ListView
        playlistSelectionListView.getItems().setAll(opzioni);

        switchToView(viewSelezionePlaylist);
    }

    public void refreshList() {
        isProgrammaticSelection = true;
        String savedSelection = null;
        if (songListView.getSelectionModel().getSelectedItem() != null) {
            savedSelection = extractFilename(songListView.getSelectionModel().getSelectedItem());
        }
        songListView.getItems().clear();

        List<IBrano> braniDaMostrare;
        if (playlistSelezionata == null) {
            mainTitleLabel.setText("Gestione brani");
            songListView.setCellFactory(creaCellFactoryTrePuntini(new StatoLibreria()));
            braniDaMostrare = libreriaController.cercaBrani(filtroAttivo);
        } else {
            mainTitleLabel.setText("Playlist: " + playlistSelezionata);
            songListView.setCellFactory(creaCellFactoryTrePuntini(new StatoPlaylist(playlistSelezionata)));

            Playlist playlistCorrente = null;
            for (Playlist p : libreriaController.getPlaylist()) {
                if (p.getNome().equals(playlistSelezionata)) {
                    playlistCorrente = p;
                    break;
                }
            }

            if (playlistCorrente != null) {
                braniDaMostrare = filtroAttivo.applica(playlistCorrente.getBrani());
            } else {
                braniDaMostrare = new ArrayList<>();
            }
        }

        int tracciaId = 1;
        for (IBrano ib : braniDaMostrare) {
            if (ib instanceof Brano b) {
                String fn = PathUtils.filenameFromPath(b.getPercorsoFile());
                String prefix = playlistSelezionata != null ? "[Traccia " + tracciaId + "] " : "";
                String display = (b.getTitolo() != null && !b.getTitolo().isBlank())
                        ? prefix + b.getTitolo() + " — " + fn
                        : prefix + fn;
                songListView.getItems().add(display);
                tracciaId++;
            }
        }

        if (songListView.getItems().isEmpty()) {
            if (playlistSelezionata != null) {
                detailsLabel.setText(!filtroAttivo.isVuoto()
                        ? "Nessun brano nella playlist corrisponde ai criteri di ricerca."
                        : "La playlist è vuota");
            } else if (!filtroAttivo.isVuoto()) {
                detailsLabel.setText("Nessun brano corrisponde ai criteri di ricerca.");
            } else {
                detailsLabel.setText("La libreria è vuota");
            }
        } else {
            int tot = braniDaMostrare.size();
            if (playlistSelezionata != null) {
                detailsLabel.setText(!filtroAttivo.isVuoto()
                        ? "Playlist '" + playlistSelezionata + "' — Risultati: " + tot + " brano/i."
                        : "Playlist '" + playlistSelezionata + "' (" + tot + " brani). Seleziona un brano.");
            } else {
                detailsLabel.setText(filtroAttivo.isVuoto()
                        ? "Libreria generale (" + tot + " brani). Seleziona un brano per i dettagli."
                        : "Risultati ricerca: " + tot + " brano/i trovato/i.");
            }
        }

        if (savedSelection != null) {
            for (String item : songListView.getItems()) {
                if (extractFilename(item).equals(savedSelection)) {
                    songListView.getSelectionModel().select(item);
                    break;
                }
            }
        }

        // Alla fine di tutte le operazioni di refresh, riabilitiamo gli eventi
        isProgrammaticSelection = false;
    }

    public void promptAggiungiBranoAPlaylist(Brano selezionato) {
        TextInputDialog textDialog = new TextInputDialog("");
        textDialog.setTitle("Aggiungi a Playlist");
        textDialog.setHeaderText("Associa il brano '" + selezionato.getTitolo() + "' a una playlist");
        textDialog.setContentText("Nome della playlist di destinazione:");

        textDialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(playlistTarget -> {
            try {
                libreriaController.aggiungiAPlaylist(selezionato, playlistTarget);
            } catch (ValidazioneException ve) {
                mostraErrore(ve);
            } catch (Exception ex) {
                mostraErrore(new ValidazioneException("Errore durante l'aggiunta: " + ex.getMessage()));
            }
        });
    }

    public void promptAggiungiTag(Brano selezionato, String displayString) {
        String fn = extractFilename(displayString);
        SongMetadata m = metadataMap.get(fn);
        String tagEsistente = (m != null && m.tag != null && !m.tag.isBlank() && !m.tag.equalsIgnoreCase("NESSUNO"))
                ? m.tag
                : "";

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Aggiungi Tag");
        dialog.setHeaderText(
                "Brano: " + selezionato.getTitolo() + (tagEsistente.isEmpty() ? "" : "\nTag attuali: " + tagEsistente));
        dialog.setContentText("Nuovo tag (es. Preferiti):");

        dialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(nuovoTag -> {
            String stringaFinale = tagEsistente.isEmpty() ? nuovoTag : tagEsistente + ", " + nuovoTag;
            try {
                libreriaController.modificaTagBrano(selezionato, stringaFinale);
                MetadataService.caricaMappaDalCSV(metadataMap);
                showDetails(displayString);
            } catch (Exception ex) {
                mostraErrore(new ValidazioneException("Impossibile aggiornare il tag: " + ex.getMessage()));
            }
        });
    }

    public void onShuffleToggled(boolean enabled) {
        if (gestoreRiproduzione == null)
            return;
        aggiornaIteratoreCorrente();
    }

    public void onLoopToggled(boolean enabled) {
        if (gestoreRiproduzione == null)
            return;
        aggiornaIteratoreCorrente();
    }

    private void aggiornaIteratoreCorrente() {
        if (!gestoreRiproduzione.hasActiveMedia())
            return;

        java.util.List<IBrano> listaBrani = new java.util.ArrayList<>();
        for (String item : songListView.getItems()) {
            IBrano br = findBranoByFilename(extractFilename(item));
            if (br != null) {
                listaBrani.add(br);
            }
        }

        String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
        IBrano b = null;
        try {
            String currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName().toString();
            b = findBranoByFilename(currentFilename);
        } catch (Exception ignored) {
        }

        PlaylistIterator iter = null;
        if (playerView.isShuffleEnabled()) {
            ShuffleStrategy strategy = new ShuffleStrategy();
            gestoreRiproduzione.setStrategia(strategy);
            ShuffleIterator sIter = new ShuffleIterator(listaBrani);
            if (b != null)
                sIter.impostaBranoCorrente(b);
            iter = sIter;
        } else if (playerView.isLoopEnabled()) {
            LoopStrategy strategy = new LoopStrategy();
            gestoreRiproduzione.setStrategia(strategy);
            LoopIterator lIter = new LoopIterator(listaBrani);
            if (b != null)
                lIter.impostaBranoCorrente(b);
            iter = lIter;
        } else {
            SequentialStrategy strategy = new SequentialStrategy();
            gestoreRiproduzione.setStrategia(strategy);
            SequentialIterator seqIter = new SequentialIterator(listaBrani);
            if (b != null)
                seqIter.impostaBranoCorrente(b);
            iter = seqIter;
        }

        gestoreRiproduzione.setIterator(iter);
        aggiornaVisualizzazioneCoda();
    }

    private void aggiornaVisualizzazioneCoda() {
        javafx.application.Platform.runLater(() -> {
            if (gestoreRiproduzione == null || nextSongLabel == null)
                return;
            PlaylistIterator iter = gestoreRiproduzione.getIterator();
            if (iter != null && gestoreRiproduzione.hasActiveMedia()) {
                IBrano prossimo = iter.peekNext();
                if (prossimo != null) {
                    nextSongLabel.setText("Prossimo: " + prossimo.getTitolo());
                } else {
                    nextSongLabel.setText("Prossimo: Fine Coda");
                }

                // Se non c'è una selezione corrente nella ListView, mostra la coda nel pannello
                // dei dettagli
                if (songListView.getSelectionModel().getSelectedItem() == null) {
                    StringBuilder sb = new StringBuilder("--- CODA DI RIPRODUZIONE ---\n\n");
                    // Brano corrente
                    String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
                    try {
                        String currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName()
                                .toString();
                        IBrano b = findBranoByFilename(currentFilename);
                        if (b != null) {
                            sb.append("▶ Ora in riproduzione:\n   ").append(b.getTitolo()).append("\n\n");
                        }
                    } catch (Exception ignored) {
                    }

                    List<IBrano> coda = iter.getCodaBrani(5);
                    if (!coda.isEmpty()) {
                        sb.append("⏭ Brani successivi:\n");
                        int idx = 1;
                        for (IBrano cb : coda) {
                            sb.append(" ").append(idx).append(". ").append(cb.getTitolo()).append("\n");
                            idx++;
                        }
                    } else {
                        sb.append("Nessun altro brano in coda.");
                    }
                    detailsLabel.setText(sb.toString());
                }
            } else {
                nextSongLabel.setText("");
                if (songListView.getSelectionModel().getSelectedItem() == null) {
                    detailsLabel.setText("Seleziona un brano per i dettagli.");
                }
            }
        });
    }

    public void playSelected() {
        String sel = songListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                gestoreRiproduzione.play();
                return;
            } else if (!songListView.getItems().isEmpty()) {
                songListView.getSelectionModel().selectFirst();
                sel = songListView.getSelectionModel().getSelectedItem();
            } else {
                return;
            }
        }

        Brano brano = findBranoByFilename(extractFilename(sel));
        if (brano == null)
            return;

        // 1. Definiamo dove si trova la cartella Libreria
        Path libDir = Path.of(System.getProperty("user.dir"), "Libreria");

        // 2. Proviamo a capire se il percorso salvato è assoluto o solo il nome del
        // file
        Path pathDaUsare = Path.of(brano.getPercorsoFile());
        if (!pathDaUsare.isAbsolute()) {
            // Se non è assoluto, lo cerchiamo dentro la cartella Libreria
            pathDaUsare = libDir.resolve(pathDaUsare.getFileName());
        }

        // 3. Verifica esistenza
        if (Files.exists(pathDaUsare)) {
            String fn = extractFilename(sel);
            SongMetadata m = metadataMap.get(fn);
            if (m != null) {
                playingTitleLabel.setText(m.title != null && !m.title.isBlank() ? m.title : fn);
                playingAuthorLabel.setText(m.author != null && !m.author.isBlank() ? m.author : "Autore sconosciuto");
            } else {
                playingTitleLabel.setText(fn);
                playingAuthorLabel.setText("Autore sconosciuto");
            }

            java.util.List<IBrano> listaBrani = new java.util.ArrayList<>();
            for (String item : songListView.getItems()) {
                IBrano br = findBranoByFilename(extractFilename(item));
                if (br != null) {
                    listaBrani.add(br);
                }
            }

            PlaylistIterator iter = null;
            if (playerView.isShuffleEnabled()) {
                ShuffleStrategy strategy = new ShuffleStrategy();
                gestoreRiproduzione.setStrategia(strategy);
                ShuffleIterator sIter = new ShuffleIterator(listaBrani);
                sIter.impostaBranoCorrente(brano);
                iter = sIter;
            } else if (playerView.isLoopEnabled()) {
                LoopStrategy strategy = new LoopStrategy();
                gestoreRiproduzione.setStrategia(strategy);
                LoopIterator lIter = new LoopIterator(listaBrani);
                lIter.impostaBranoCorrente(brano);
                iter = lIter;
            } else {
                SequentialStrategy strategy = new SequentialStrategy();
                gestoreRiproduzione.setStrategia(strategy);
                SequentialIterator seqIter = new SequentialIterator(listaBrani);
                seqIter.impostaBranoCorrente(brano);
                iter = seqIter;
            }
            gestoreRiproduzione.setIterator(iter);

            gestoreRiproduzione.playFile(pathDaUsare);
            aggiornaVisualizzazioneCoda();
        } else {
            System.err.println("File non trovato in: " + pathDaUsare.toAbsolutePath());
            showAlert("File non trovato: " + brano.getPercorsoFile(), Alert.AlertType.ERROR);
        }
    }

    public void showDetails(String display) {
        if (display == null) {
            aggiornaVisualizzazioneCoda();
            if (gestoreRiproduzione == null || !gestoreRiproduzione.hasActiveMedia()) {
                playingTitleLabel.setText("Nessun brano in riproduzione");
                playingAuthorLabel.setText("");
                if (playerView != null) {
                    playerView.setTotalTimeLabel(0);
                    playerView.setPlaybackControlsDisabled(true);
                }
                aggiornaStatoCuore(null);
            }
            return;
        }
        if (playerView != null)
            playerView.setPlaybackControlsDisabled(false);
        String fn = extractFilename(display);
        SongMetadata m = metadataMap.get(fn);

        // Aggiorna anche le label del player in basso a sinistra quando selezioni
        if (m != null) {
            playingTitleLabel.setText(m.title != null && !m.title.isBlank() ? m.title : fn);
            playingAuthorLabel.setText(m.author != null && !m.author.isBlank() ? m.author : "Autore sconosciuto");
        } else {
            playingTitleLabel.setText(fn);
            playingAuthorLabel.setText("Autore sconosciuto");
        }

        if (m == null) {
            detailsLabel.setText(fn);
        } else {
            String durStr = "N/D";
            try {
                if (m.duration != null && !m.duration.isBlank()) {
                    int sec = Integer.parseInt(m.duration);
                    durStr = String.format("%d:%02d", sec / 60, sec % 60);
                    if (playerView != null) {
                        playerView.setTotalTimeLabel(sec);
                    }
                }
            } catch (Exception ignored) {
            }

            detailsLabel.setText(
                    "Titolo: " + m.title +
                            "\nAutore: " + m.author +
                            "\nGenere: " + m.genre +
                            "\nAnno: " + (m.year == null || m.year.isBlank() ? "N/D" : m.year) +
                            "\nDurata: " + durStr +
                            "\nTag: " + (m.tag == null || m.tag.isBlank() ? "Nessuno" : m.tag));
        }
        aggiornaStatoCuore(fn);
    }

    public void editBrano(Brano branoDaModificare) {
        if (formBranoView != null) {
            String fn = PathUtils.filenameFromPath(branoDaModificare.getPercorsoFile());
            formBranoView.editBrano(branoDaModificare, metadataMap.get(fn));
            switchToView(viewAggiuntaBrano);
        }
    }

    public void deleteBrano(Brano branoDaEliminare) {
        if (branoDaEliminare == null) {
            showAlert("Seleziona prima un brano.", Alert.AlertType.ERROR);
            return;
        }

        String fn = PathUtils.filenameFromPath(branoDaEliminare.getPercorsoFile());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare '" + fn + "' dalla libreria?",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES)
            return;

        // Ferma il player se sta riproducendo questo brano
        if (gestoreRiproduzione != null) {
            gestoreRiproduzione.stop();
        }

        try {
            // Rimuove solo il riferimento dalla libreria (nessun file fisico eliminato)
            libreriaController.eliminaBranoPerFilename(fn);
            detailsLabel.setText("Brano rimosso dalla libreria.");
            syncAndRefresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostraErrore(new ValidazioneException("Errore rimozione: " + ex.getMessage()));
        }
    }

    public void chiediNomePlaylistEAggiungi(Brano brano) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Aggiungi a Playlist");
        dialog.setHeaderText("Aggiungi '" + brano.getTitolo() + "' a una playlist");
        dialog.setContentText("Nome playlist:");

        dialog.showAndWait().ifPresent(nome -> {
            try {
                libreriaController.aggiungiAPlaylist(brano, nome);
                refreshPlaylistList();
                detailsLabel.setText("Brano aggiunto alla playlist '" + nome + "'.");
            } catch (ValidazioneException e) {
                mostraErrore(e);
            }
        });
    }

    public void mostraErrore(ValidazioneException ex) {
        javafx.application.Platform.runLater(() -> {
            resetBordiCampi();
            String header;
            if (ex.getTipo() == ValidazioneException.TipoErrore.CAMPO_MANCANTE)
                header = "Campo Obbligatorio Mancante";
            else if (ex.getTipo() == ValidazioneException.TipoErrore.FORMATO_NON_VALIDO)
                header = "Formato o Range Non Valido";
            else
                header = "Errore di Validazione";

            if (ex.getCampoErrato() != null) {
                switch (ex.getCampoErrato().toLowerCase()) {
                    case "titolo" -> lightenRosso(currentTitleField);
                    case "autore" -> lightenRosso(currentAuthorField);
                    case "genere" -> lightenRosso(currentGenreField);
                    case "anno" -> lightenRosso(currentYearField);
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
        if (tf != null)
            tf.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 3px;");
    }

    private void resetBordiCampi() {
        String s = "-fx-border-color: transparent; -fx-border-width: 0;";
        if (currentTitleField != null)
            currentTitleField.setStyle(s);
        if (currentAuthorField != null)
            currentAuthorField.setStyle(s);
        if (currentYearField != null)
            currentYearField.setStyle(s);
        if (currentGenreField != null)
            currentGenreField.setStyle(s);
    }

    public String extractFilename(String display) {
        if (display == null)
            return "";
        if (display.startsWith("[Traccia ")) {
            display = display.substring(display.indexOf("] ") + 2);
        }
        String raw = display.contains(" — ") ? display.substring(display.lastIndexOf(" — ") + 3) : display;
        return PathUtils.filenameFromPath(raw);
    }

    private Brano findBranoByFilename(String fn) {
        for (IBrano ib : libreriaController.getBrani()) {
            // Controlla se l'oggetto è un Brano e se il percorso corrisponde
            if (ib instanceof Brano b) {
                String filenameBrano = PathUtils.filenameFromPath(b.getPercorsoFile());
                if (filenameBrano.equals(fn)) {
                    return b;
                }
            }
        }
        return null;
    }

    private void syncAndRefresh() {
        // 1. Ricarica la mappa dei metadati dal CSV aggiornato
        MetadataService.caricaMappaDalCSV(metadataMap);

        popolaComboAnno();
        popolaComboGenere();

        // 2. Rinfresca la lista visualizzata (ListView)
        refreshList();

        // Aggiorna l'iteratore corrente del player
        aggiornaIteratoreCorrente();

        // 3. SELEZIONA IL NIENTE per forzare il reset delle label dei dettagli
        songListView.getSelectionModel().clearSelection();
        detailsLabel.setText("Seleziona un brano per i dettagli.");
    }

    // =========================================================================
    // Ricerca e Filtri
    // =========================================================================

    private void popolaComboAnno() {
        if (searchAnnoCombo == null)
            return;
        String selected = searchAnnoCombo.getValue();
        searchAnnoCombo.getItems().clear();
        searchAnnoCombo.getItems().add(""); // voce "Tutti"
        libreriaController.getBrani().stream()
                .filter(ib -> ib instanceof Brano)
                .map(ib -> ((Brano) ib).getAnno())
                .filter(a -> a > 0)
                .distinct()
                .sorted()
                .forEach(a -> searchAnnoCombo.getItems().add(String.valueOf(a)));
        if (selected != null && searchAnnoCombo.getItems().contains(selected)) {
            searchAnnoCombo.setValue(selected);
        }
    }

    private void popolaComboGenere() {
        if (searchGenereCombo == null)
            return;
        String selected = searchGenereCombo.getValue();
        searchGenereCombo.getItems().clear();
        searchGenereCombo.getItems().add(""); // voce "Tutti"
        libreriaController.getBrani().stream()
                .filter(ib -> ib instanceof Brano)
                .map(ib -> ((Brano) ib).getGenere())
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .sorted()
                .forEach(g -> searchGenereCombo.getItems().add(g));
        if (selected != null && searchGenereCombo.getItems().contains(selected)) {
            searchGenereCombo.setValue(selected);
        }
    }

    private void popolaComboTag() {
        if (searchTagCombo == null)
            return;
        searchTagCombo.getItems().clear();
        searchTagCombo.getItems().add(""); // voce "Tutti"
        for (Tag t : Tag.values()) {
            if (t != Tag.NESSUNO) {
                searchTagCombo.getItems().add(t.getEtichetta());
            }
        }
    }

    private void applicaFiltro() {
        filtroAttivo.setTitolo(searchTitoloField != null ? searchTitoloField.getText() : "");
        filtroAttivo.setAutore(searchAutoreField != null ? searchAutoreField.getText() : "");

        int annoSel = 0;
        if (searchAnnoCombo != null) {
            String annoStr = searchAnnoCombo.getValue();
            if (annoStr != null && !annoStr.isBlank()) {
                try {
                    annoSel = Integer.parseInt(annoStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        filtroAttivo.setAnno(annoSel);

        String genereSel = (searchGenereCombo != null && searchGenereCombo.getValue() != null)
                ? searchGenereCombo.getValue()
                : "";
        filtroAttivo.setGenere(Genere.fromString(genereSel));

        Tag tagSel = Tag.NESSUNO;
        if (searchTagCombo != null && searchTagCombo.getValue() != null && !searchTagCombo.getValue().isBlank()) {
            tagSel = Tag.fromString(searchTagCombo.getValue());
        }
        filtroAttivo.setTag(tagSel);

        refreshList();
    }

    private void azzeraFiltro() {
        filtroAttivo.reset();
        if (searchTitoloField != null)
            searchTitoloField.clear();
        if (searchAutoreField != null)
            searchAutoreField.clear();
        if (searchAnnoCombo != null)
            searchAnnoCombo.setValue("");
        if (searchGenereCombo != null)
            searchGenereCombo.setValue("");
        if (searchTagCombo != null)
            searchTagCombo.setValue("");
        refreshList();
    }

    public void showAlert(String msg, Alert.AlertType type) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    @Override
    public void onBranoAggiunto(IBrano brano) {
        javafx.application.Platform.runLater(this::syncAndRefresh);
    }

    @Override
    public void onBranoEliminato(IBrano brano) {
        javafx.application.Platform.runLater(() -> {
            // 1. Rinfresca la lista principale
            syncAndRefresh();

            if (brano instanceof Brano b) {
                String fnEliminato = PathUtils.filenameFromPath(b.getPercorsoFile());

                // 2. Controllo di riproduzione (quello che avevamo già fatto prima)
                if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                    String sourceCorrente = gestoreRiproduzione.getCurrentMediaSource();
                    String filenameInRiproduzione = "";
                    try {
                        filenameInRiproduzione = Path.of(java.net.URI.create(sourceCorrente)).getFileName().toString();
                    } catch (Exception e) {
                        filenameInRiproduzione = sourceCorrente;
                    }

                    if (fnEliminato.equals(filenameInRiproduzione)) {
                        gestoreRiproduzione.stop();

                        playingTitleLabel.setText("Nessun brano in riproduzione");
                        playingAuthorLabel.setText("");

                        if (playerView != null) {
                            playerView.setTotalTimeLabel(0);
                            playerView.setPlaybackControlsDisabled(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onPlaylistAggiornata(Playlist playlist) {
        javafx.application.Platform.runLater(() -> {
            refreshPlaylistList();
            refreshList();
            aggiornaIteratoreCorrente();
        });
    }
}