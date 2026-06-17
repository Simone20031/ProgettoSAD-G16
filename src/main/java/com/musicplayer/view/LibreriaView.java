package com.musicplayer.view;

import com.musicplayer.PathUtils;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;
import com.musicplayer.strategy.*;
import com.musicplayer.state.*;
import com.musicplayer.command.*;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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
    private ListView<String> topSongsListView;
    @FXML
    private ListView<String> playlistListView;

    @FXML
    private Button addBtn;
    @FXML
    private Button undoBtn;
    @FXML
    private Button btnMostraLibreria;
    @FXML
    private Button btnMostraPiuAscoltati;

    @FXML
    private Button playBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button skipBackBtn;
    @FXML
    private Button skipBtn;
    @FXML
    private Button shuffleBtn;
    @FXML
    private HBox playlistControlsBox;
    @FXML
    private Button playlistPlayBtn;
    @FXML
    private Button playlistStopBtn;
    @FXML
    private Button playlistShuffleBtn;
    @FXML
    private Button playlistLoopBtn;

    @FXML
    private Button loopBtn;
    @FXML
    private Slider progressSlider;
    @FXML
    private Slider volumeSlider;
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

    @FXML
    private Label lblHeaderTitolo;
    @FXML
    private Label lblHeaderAutore;
    @FXML
    private Label lblHeaderAnno;
    @FXML
    private Label lblHeaderGenere;
    @FXML
    private Label lblHeaderTag;

    private boolean playlistShuffleEnabled = false;
    private boolean playlistLoopEnabled = false;
    private String playingContext = "LIBRERIA";
    private java.util.Map<String, String> selectionsMap = new java.util.HashMap<>();
    private String currentlyPlayingFilename = null;
    private boolean isPlayerPaused = false;

    @FXML
    private Button btnApriCreazione;
    @FXML
    private VBox viewLista;
    @FXML
    private VBox viewCreazione;
    @FXML
    private VBox viewAggiuntaBrano;
    @FXML
    private javafx.scene.control.ScrollPane viewHome;
    @FXML
    private HBox topPlaylistsBox;
    @FXML
    private javafx.scene.control.ScrollPane scrollTopPlaylists;
    @FXML
    private Button btnScrollLeftTop;
    @FXML
    private Button btnScrollRightTop;

    @FXML
    private HBox smartPlaylistsBox;
    @FXML
    private javafx.scene.control.ScrollPane scrollSmartPlaylists;
    @FXML
    private Button btnScrollLeftSmart;
    @FXML
    private Button btnScrollRightSmart;

    @FXML
    private TextField playlistNameField;
    @FXML
    private Button createPlaylistBtn;
    @FXML
    private Button btnAnnullaCreazione;
    @FXML
    private Label lblGestionePlaylistTitle;

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

    @FXML
    private VBox viewSelezioneBrano;
    @FXML
    private Label lblSelezioneBranoTitle;
    @FXML
    private ListView<String> branoSelectionListView;
    @FXML
    private Button btnAnnullaSelezioneBrano;
    @FXML
    private Button btnConfermaSelezioneBrano;
    @FXML
    private TextField addSearchTitoloField;
    @FXML
    private TextField addSearchAutoreField;
    @FXML
    private ComboBox<String> addSearchAnnoCombo;
    @FXML
    private ComboBox<String> addSearchGenereCombo;
    @FXML
    private ComboBox<String> addSearchTagCombo;
    @FXML
    private Button addResetSearchBtn;

    @FXML
    private Button btnQueue;
    @FXML
    private VBox viewCoda;
    @FXML
    private VBox viewDettagli;
    @FXML
    private ListView<String> codaListView;
    @FXML
    private Label lblCodaStaiAscoltandoTitolo;
    @FXML
    private Label lblCodaStaiAscoltandoAutore;

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
    private List<Brano> braniInAttesaDiPlaylistMassivo = null;
    private TextField currentTitleField;
    private TextField currentAuthorField;
    private TextField currentGenreField;
    private TextField currentYearField;

    private final UndoManager undoManager = new UndoManager();

    public void resetHeaderLabels() {
        if (lblHeaderTitolo != null)
            lblHeaderTitolo.setText("TITOLO");
        if (lblHeaderAutore != null)
            lblHeaderAutore.setText("AUTORE");
        if (lblHeaderAnno != null)
            lblHeaderAnno.setText("ANNO");
        if (lblHeaderGenere != null)
            lblHeaderGenere.setText("GENERE");
        if (lblHeaderTag != null)
            lblHeaderTag.setText("TAG");
    }

    public void aggiornaStatoUndo() {
        if (undoBtn != null) {
            boolean canUndo = undoManager.canUndo();
            undoBtn.setDisable(!canUndo);
            if (canUndo) {
                undoBtn.setStyle(
                        "-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-padding: 8 20; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand; -fx-border-color: transparent;");
            } else {
                undoBtn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-padding: 8 20; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: default; -fx-border-color: #555555; -fx-border-width: 1;");
                undoBtn.setTooltip(null);
            }
        }
    }

    public void mostraNotificaUndo(String messaggio) {
        aggiornaStatoUndo();
        if (undoBtn != null && messaggio != null) {
            undoBtn.setTooltip(new Tooltip("Annulla: " + messaggio));
        }
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

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
                }, this::mostraNotificaUndo, primaryStage, undoManager);

        this.primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                    () -> {
                        if (undoBtn != null && !undoBtn.isDisable()) {
                            undoBtn.fire();
                        }
                    }
                );
            }
        });
    }

    private void animateScroll(javafx.scene.control.ScrollPane scrollPane, double targetHvalue) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyValue kv = new javafx.animation.KeyValue(scrollPane.hvalueProperty(), targetHvalue,
                javafx.animation.Interpolator.EASE_BOTH);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Files.createDirectories(Path.of(System.getProperty("user.dir"), "Libreria"));
        } catch (IOException ignored) {
            // Ignored intentionally
        }

        this.gestoreRiproduzione = GestoreRiproduzione.getInstance();
        libreriaController.addObserver(this);
        libreriaController.addObserver(gestoreRiproduzione);
        libreriaController.caricaDaCSV();
        MetadataService.caricaMappaDalCSV(metadataMap);

        this.playlistView = new PlaylistView(playlistListView, viewLista, viewCreazione,
                playlistNameField, createPlaylistBtn, btnAnnullaCreazione, btnApriCreazione,
                lblGestionePlaylistTitle, viewSelezioneBrano, branoSelectionListView,
                btnAnnullaSelezioneBrano, btnConfermaSelezioneBrano,
                addSearchTitoloField, addSearchAutoreField, addSearchAnnoCombo,
                addSearchGenereCombo, addSearchTagCombo, addResetSearchBtn,
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

        if (lblHeaderTitolo != null)
            lblHeaderTitolo.setOnMouseClicked(e -> handleOrdinamento(CampoOrdinamento.TITOLO, lblHeaderTitolo));
        if (lblHeaderAutore != null)
            lblHeaderAutore.setOnMouseClicked(e -> handleOrdinamento(CampoOrdinamento.AUTORE, lblHeaderAutore));
        if (lblHeaderAnno != null)
            lblHeaderAnno.setOnMouseClicked(e -> handleOrdinamento(CampoOrdinamento.ANNO, lblHeaderAnno));
        if (lblHeaderGenere != null)
            lblHeaderGenere.setOnMouseClicked(e -> handleOrdinamento(CampoOrdinamento.GENERE, lblHeaderGenere));
        if (lblHeaderTag != null)
            lblHeaderTag.setOnMouseClicked(e -> handleOrdinamento(CampoOrdinamento.TAG, lblHeaderTag));

        refreshList();
        refreshPlaylistList();

        if (btnQueue != null) {
            btnQueue.setOnAction(e -> {
                if (viewCoda != null && viewCoda.isVisible()) {
                    // Coda è visibile, nascondila e mostra i dettagli
                    viewCoda.setVisible(false);
                    viewCoda.setManaged(false);
                    if (viewDettagli != null) {
                        viewDettagli.setVisible(true);
                        viewDettagli.setManaged(true);
                    }
                } else {
                    // Coda non visibile, mostrala e nascondi i dettagli
                    if (viewDettagli != null) {
                        viewDettagli.setVisible(false);
                        viewDettagli.setManaged(false);
                    }
                    if (viewCoda != null) {
                        viewCoda.setVisible(true);
                        viewCoda.setManaged(true);
                        aggiornaVisualizzazioneCoda();
                    }
                }
            });
        }

        setupArrowButton(btnScrollLeftTop);
        setupArrowButton(btnScrollRightTop);
        setupArrowButton(btnScrollLeftSmart);
        setupArrowButton(btnScrollRightSmart);

        btnScrollLeftTop.setOnAction(e -> {
            double contentW = topPlaylistsBox.getWidth();
            double viewportW = scrollTopPlaylists.getViewportBounds().getWidth();
            if (contentW > viewportW) {
                double currentPx = scrollTopPlaylists.getHvalue() * (contentW - viewportW);
                double newPx = Math.max(0, Math.round((currentPx - 216) / 216.0) * 216.0);
                animateScroll(scrollTopPlaylists, newPx / (contentW - viewportW));
            }
        });
        btnScrollRightTop.setOnAction(e -> {
            double contentW = topPlaylistsBox.getWidth();
            double viewportW = scrollTopPlaylists.getViewportBounds().getWidth();
            if (contentW > viewportW) {
                double currentPx = scrollTopPlaylists.getHvalue() * (contentW - viewportW);
                double newPx = Math.min(contentW - viewportW, Math.round((currentPx + 216) / 216.0) * 216.0);
                animateScroll(scrollTopPlaylists, newPx / (contentW - viewportW));
            }
        });

        btnScrollLeftSmart.setOnAction(e -> {
            double contentW = smartPlaylistsBox.getWidth();
            double viewportW = scrollSmartPlaylists.getViewportBounds().getWidth();
            if (contentW > viewportW) {
                double currentPx = scrollSmartPlaylists.getHvalue() * (contentW - viewportW);
                double newPx = Math.max(0, Math.round((currentPx - 216) / 216.0) * 216.0);
                animateScroll(scrollSmartPlaylists, newPx / (contentW - viewportW));
            }
        });
        btnScrollRightSmart.setOnAction(e -> {
            double contentW = smartPlaylistsBox.getWidth();
            double viewportW = scrollSmartPlaylists.getViewportBounds().getWidth();
            if (contentW > viewportW) {
                double currentPx = scrollSmartPlaylists.getHvalue() * (contentW - viewportW);
                double newPx = Math.min(contentW - viewportW, Math.round((currentPx + 216) / 216.0) * 216.0);
                animateScroll(scrollSmartPlaylists, newPx / (contentW - viewportW));
            }
        });

        javafx.util.Callback<ListView<String>, javafx.scene.control.ListCell<String>> selectionCellFactory = lv -> new javafx.scene.control.ListCell<String>() {
            {
                selectedProperty().addListener((obs, oldVal, newVal) -> updateStyle(newVal, isHover()));
                hoverProperty().addListener((obs, oldVal, newVal) -> updateStyle(isSelected(), newVal));
            }

            private void updateStyle(boolean selected, boolean hovered) {
                if (getItem() == null) {
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (selected) {
                    setStyle(
                            "-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 4;");
                } else if (hovered) {
                    setStyle(
                            "-fx-background-color: #282828; -fx-text-fill: #ffffff; -fx-padding: 10; -fx-background-radius: 4;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-padding: 10;");
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    updateStyle(isSelected(), isHover());
                }
            }
        };

        playlistSelectionListView.setCellFactory(selectionCellFactory);

        if (branoSelectionListView != null) {
            branoSelectionListView.setCellFactory(creaCellFactorySelezioneBrano());
        }

        btnMostraLibreria.setOnAction(e -> {
            impostaTabAttivo("LIBRERIA");
            switchToView(viewLista);
            mostraLibreriaGenerale();
        });

        btnMostraPiuAscoltati.setOnAction(e -> {
            impostaTabAttivo("PIU_ASCOLTATI");
            switchToView(viewHome);
            mostraPiuaAscoltati();
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
                        refreshPlaylistList(); // Aggiorna i contatori nel carosello
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

        if (undoBtn != null) {
            undoBtn.setOnAction(e -> {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.annullaUltimaOperazione();
                        refreshList();
                        refreshPlaylistList();
                        aggiornaStatoUndo();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Operazione annullata con successo!");
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Nessuna operazione da annullare.");
                        alert.show();
                    }
                } catch (Exception ex) {
                    mostraErrore(new ValidazioneException("Errore durante l'annullamento: " + ex.getMessage()));
                }
            });
            aggiornaStatoUndo();
        }

        btnAnnullaSelezione.setOnAction(e -> switchToView(viewLista));

        btnConfermaImport.setOnAction(e -> {
            try {
                if (formBranoView != null) {
                    formBranoView.confermaImportBrano();
                }
            } catch (Exception ve) {
                ve.printStackTrace();
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
            if (braniInAttesaDiPlaylistMassivo != null) {
                braniInAttesaDiPlaylistMassivo = null;
            }
            refreshList();
            switchToView(viewLista);
        });

        btnConfermaSelezione.setOnAction(e -> {
            String nomePlaylistSelezionato = playlistSelectionListView.getSelectionModel().getSelectedItem();

            if (nomePlaylistSelezionato == null) {
                showAlert("Seleziona una playlist dalla lista!", Alert.AlertType.WARNING);
                return;
            }

            String contestoProvenienza = playlistSelezionata;
            if (branoInAttesaDiPlaylist != null) {
                try {
                    Command cmd = new AggiungiAPlaylistCmd(libreriaController, branoInAttesaDiPlaylist,
                            nomePlaylistSelezionato);
                    cmd.esegui();
                    undoManager.aggiungiComando(cmd);
                    mostraNotificaUndo("Brano aggiunto alla playlist");
                    branoInAttesaDiPlaylist = null;
                    refreshList();
                    refreshPlaylistList();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Brano aggiunto con successo!");
                    alert.show();
                    switchToView(viewLista);
                    if (contestoProvenienza != null) {
                        impostaPlaylist(contestoProvenienza);
                    } else {
                        mostraLibreriaGenerale();
                    }
                } catch (ValidazioneException ve) {
                    ve.printStackTrace();
                    mostraErrore(ve);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostraErrore(new ValidazioneException("Errore durante l'aggiunta: " + ex.getMessage()));
                }
            } else if (braniInAttesaDiPlaylistMassivo != null && !braniInAttesaDiPlaylistMassivo.isEmpty()) {
                try {
                    Command cmd = new AggiungiMassivoCmd(libreriaController, braniInAttesaDiPlaylistMassivo,
                            nomePlaylistSelezionato);
                    cmd.esegui();
                    if (undoManager != null) {
                        undoManager.aggiungiComando(cmd);
                        mostraNotificaUndo("Brani aggiunti alla playlist");
                    }
                    braniInAttesaDiPlaylistMassivo = null;
                    refreshList();
                    refreshPlaylistList();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Brani aggiunti con successo!");
                    alert.show();
                    switchToView(viewLista);
                    if (contestoProvenienza != null) {
                        impostaPlaylist(contestoProvenienza);
                    } else {
                        mostraLibreriaGenerale();
                    }
                } catch (ValidazioneException ve) {
                    ve.printStackTrace();
                    mostraErrore(ve);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mostraErrore(new ValidazioneException("Errore durante l'aggiunta: " + ex.getMessage()));
                }
            }
        });
        // Inizializza PlayerView
        this.playerView = new PlayerView(playBtn, stopBtn, skipBackBtn, skipBtn, loopBtn, currentTimeLabel,
                totalTimeLabel,
                progressSlider, volumeSlider,
                gestoreRiproduzione, this);

        initPlaylistControlsHandlers();

        this.gestoreRiproduzione.addObserver(new RiproduzioneObserver() {
            @Override
            public void onPlayerReady(int durataSecondi) {
                javafx.application.Platform.runLater(() -> {
                    if (playerView != null) {
                        playerView.setTotalTimeLabel(durataSecondi);
                        playerView.setPlaybackControlsDisabled(false);
                    }
                });
            }

            @Override
            public void onPlay() {
                javafx.application.Platform.runLater(() -> {
                    isPlayerPaused = false;
                    updatePlaylistPlayButtonUI();
                    if (songListView != null)
                        songListView.refresh();
                    if (topSongsListView != null)
                        topSongsListView.refresh();
                });
            }

            @Override
            public void onPausa() {
                javafx.application.Platform.runLater(() -> {
                    isPlayerPaused = true;
                    updatePlaylistPlayButtonUI();
                    if (songListView != null)
                        songListView.refresh();
                    if (topSongsListView != null)
                        topSongsListView.refresh();
                });
            }

            @Override
            public void onStop() {
                javafx.application.Platform.runLater(() -> {
                    currentlyPlayingFilename = null;
                    playingTitleLabel.setText("Nessun brano in riproduzione");
                    playingAuthorLabel.setText("");
                    if (playerView != null) {
                        playerView.setTotalTimeLabel(0);
                        playerView.setPlaybackControlsDisabled(true);
                    }
                    aggiornaStatoCuore(null);
                    if (songListView != null)
                        songListView.refresh();
                    if (topSongsListView != null)
                        topSongsListView.refresh();
                    updatePlaylistPlayButtonUI();
                    aggiornaVisualizzazioneCoda();
                });
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

                    Brano b = findBranoByFilename(fn);
                    if (b != null) {
                        libreriaController.registraAscolto(b);
                        // Se stiamo suonando da una playlist, registriamo l'ascolto anche per la
                        // playlist
                        if (playlistSelezionata != null) {
                            Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                            if (pl != null) {
                                libreriaController.registraAscolto(pl);
                            }
                        }
                        // Aggiorna l'interfaccia Home se visibile
                        if (viewHome.isVisible()) {
                            refreshPiuaAscoltatiList();
                        }
                    }

                    currentlyPlayingFilename = fn;
                    if (songListView != null)
                        songListView.refresh();
                    if (topSongsListView != null)
                        topSongsListView.refresh();
                    aggiornaVisualizzazioneCoda();
                });
            }

            @Override
            public void onBranoRipetuto() {
                javafx.application.Platform.runLater(() -> {
                    if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()) {
                        String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
                        String fn = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName().toString();
                        Brano b = findBranoByFilename(fn);
                        if (b != null) {
                            libreriaController.registraAscolto(b);
                            if (playlistSelezionata != null) {
                                Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                                if (pl != null) {
                                    libreriaController.registraAscolto(pl);
                                }
                            }
                            if (viewHome.isVisible()) {
                                refreshPiuaAscoltatiList();
                            }
                        }
                    }
                });
            }

            @Override
            public void onCodaAggiornata() {
                javafx.application.Platform.runLater(() -> {
                    aggiornaVisualizzazioneCoda();
                });
            }
        });

        songListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        songListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    showDetails(newVal);
                    if (newVal != null && playlistSelezionata == null) {
                        selectionsMap.put("LIBRERIA", extractFilename(newVal));
                    }
                });

        topSongsListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    showDetails(newVal);
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

        // Set Home Page as default view
        javafx.application.Platform.runLater(() -> {
            impostaTabAttivo("PIU_ASCOLTATI");
            switchToView(viewHome);
            mostraPiuaAscoltati();
        });
    }

    public void switchToView(javafx.scene.Node viewToShow) {
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

        if (viewSelezioneBrano != null) {
            viewSelezioneBrano.setVisible(false);
            viewSelezioneBrano.setManaged(false);
        }

        if (viewHome != null) {
            viewHome.setVisible(false);
            viewHome.setManaged(false);
        }

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
            private final Label lblAnno = new Label();
            private final Label lblGenere = new Label();
            private final Label lblTag = new Label();
            private final Label lblDurata = new Label();
            private final Pane spacer = new Pane();
            private final Button btnOpzioni = new Button("⋮");
            private final ContextMenu menu = new ContextMenu();

            {
                container.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !isEmpty()) {
                        if (event.isControlDown() || event.isShiftDown()) {
                            // Let native JavaFX multiple selection handle it
                            return;
                        }
                        songListView.getSelectionModel().clearAndSelect(getIndex());
                    }
                });
                lblId.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !isEmpty()) {
                        if (event.isControlDown() || event.isShiftDown()) {
                            // Let native JavaFX multiple selection handle it
                            return;
                        }
                        songListView.getSelectionModel().select(getItem());
                        String fn = extractFilename(getItem());
                        String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
                        if (currentlyPlayingFilename != null && currentlyPlayingFilename.equals(fn)
                                && playingContext.equals(currentViewContext)) {
                            if (isPlayerPaused) {
                                gestoreRiproduzione.play();
                            } else {
                                gestoreRiproduzione.pausa();
                            }
                        } else {
                            playSelected();
                        }
                        event.consume();
                    }
                });
                lblId.setMinWidth(30);
                lblId.setPrefWidth(30);
                lblId.setMaxWidth(30);

                lblTitolo.setMinWidth(160);
                lblTitolo.setPrefWidth(160);
                lblTitolo.setMaxWidth(160);

                lblAutore.setMinWidth(120);
                lblAutore.setPrefWidth(120);
                lblAutore.setMaxWidth(120);

                lblAnno.setMinWidth(50);
                lblAnno.setPrefWidth(50);
                lblAnno.setMaxWidth(50);

                lblGenere.setMinWidth(90);
                lblGenere.setPrefWidth(90);
                lblGenere.setMaxWidth(90);

                lblTag.setMinWidth(100);
                lblTag.setPrefWidth(100);
                lblTag.setMaxWidth(100);

                lblDurata.setMinWidth(50);
                lblDurata.setPrefWidth(50);
                lblDurata.setMaxWidth(50);

                HBox.setHgrow(spacer, Priority.ALWAYS);

                btnOpzioni.setFocusTraversable(false);

                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
                container.setSpacing(10);

                container.getChildren().addAll(lblId, lblTitolo, lblAutore, lblAnno, lblGenere, lblTag, spacer,
                        lblDurata, btnOpzioni);

                btnOpzioni.setOnAction(e -> {
                    e.consume();
                    menu.getItems().clear();
                    List<String> selectedItems = new ArrayList<>(songListView.getSelectionModel().getSelectedItems());
                    String currentItem = getItem();
                    if (currentItem != null && !selectedItems.contains(currentItem)) {
                        selectedItems.clear();
                        selectedItems.add(currentItem);
                    }

                    if (selectedItems.size() > 1) {
                        MenuItem miAggiungi = new MenuItem(
                                playlistSelezionata == null ? "Aggiungi a playlist" : "Aggiungi a un'altra playlist");
                        miAggiungi.setOnAction(ev -> aggiungiBraniAPlaylistMassivo(selectedItems));

                        if (playlistSelezionata == null) {
                            MenuItem miElimina = new MenuItem("Elimina definitivamente dalla libreria");
                            miElimina.setOnAction(ev -> eliminaBraniMassivo(selectedItems));
                            menu.getItems().addAll(miElimina, miAggiungi);
                        } else {
                            menu.getItems().add(miAggiungi);
                            MenuItem miRimuovi = new MenuItem("Rimuovi dalla playlist corrente");
                            miRimuovi.setOnAction(
                                    ev -> rimuoviBraniDaPlaylistMassivo(selectedItems, playlistSelezionata));
                            menu.getItems().add(miRimuovi);
                        }
                    } else if (!selectedItems.isEmpty()) {
                        String sel = selectedItems.get(0);
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

                // Drag and drop event handlers for reordering
                setOnDragDetected(event -> {
                    if (getItem() == null) {
                        return;
                    }
                    if (playlistSelezionata != null) {
                        Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                        if (pl != null && !(pl instanceof SmartPlaylist)) {
                            javafx.scene.input.Dragboard db = startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                            content.putString(getItem());
                            db.setContent(content);
                            event.consume();
                        }
                    }
                });

                setOnDragOver(event -> {
                    if (event.getGestureSource() != this && event.getDragboard().hasString()
                            && playlistSelezionata != null) {
                        Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                        if (pl != null && !(pl instanceof SmartPlaylist)) {
                            event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                        }
                    }
                    event.consume();
                });

                setOnDragEntered(event -> {
                    if (event.getGestureSource() != this && event.getDragboard().hasString()
                            && playlistSelezionata != null) {
                        Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                        if (pl != null && !(pl instanceof SmartPlaylist)) {
                            setStyle(
                                    "-fx-background-color: #282828; -fx-border-color: #1DB954; -fx-border-width: 2 0 0 0;");
                        }
                    }
                });

                setOnDragExited(event -> {
                    if (event.getGestureSource() != this && event.getDragboard().hasString()
                            && playlistSelezionata != null) {
                        Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                        if (pl != null && !(pl instanceof SmartPlaylist)) {
                            setStyle("-fx-background-color: transparent; -fx-padding: 4 8 4 8;");
                        }
                    }
                });

                setOnDragDropped(event -> {
                    javafx.scene.input.Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString() && playlistSelezionata != null) {
                        Playlist pl = libreriaController.getPlaylistMap().get(playlistSelezionata);
                        if (pl != null && !(pl instanceof SmartPlaylist)) {
                            @SuppressWarnings("unchecked")
                            ListCell<String> sourceCell = (ListCell<String>) event.getGestureSource();
                            int idxFrom = sourceCell.getIndex();
                            int idxTo = getIndex();
                            if (idxFrom >= 0 && idxTo >= 0 && idxFrom != idxTo) {
                                try {
                                    String targetStr = db.getString();
                                    String fn = extractFilename(targetStr);
                                    Brano brano = findBranoByFilename(fn);
                                    if (brano != null) {
                                        libreriaController.spostaBranoInPlaylist(brano, playlistSelezionata, idxTo);
                                        success = true;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                setOnDragDone(javafx.scene.input.DragEvent::consume);
            }

            private void updateStyle(boolean selected, boolean hovered) {
                boolean isPlaying = false;
                if (!isEmpty()) {
                    String fn = extractFilename(getItem());
                    String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
                    isPlaying = (currentlyPlayingFilename != null && currentlyPlayingFilename.equals(fn)
                            && playingContext.equals(currentViewContext));

                    if (isPlaying) {
                        if (hovered) {
                            lblId.setText(isPlayerPaused ? "▶" : "⏸");
                            lblId.setStyle(
                                    "-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
                        } else {
                            lblId.setText(String.valueOf(getIndex() + 1));
                            lblId.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-font-size: 13px;");
                        }
                    } else {
                        if (hovered) {
                            lblId.setText("▶");
                            lblId.setStyle(
                                    "-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
                        } else {
                            lblId.setText(String.valueOf(getIndex() + 1));
                            lblId.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                        }
                    }
                }

                if (isPlaying) {
                    container.setStyle(
                            "-fx-background-color: #282828; -fx-border-color: #1DB954; -fx-border-width: 0 0 0 4; -fx-background-radius: 6;");
                    lblTitolo.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else if (selected) {
                    container.setStyle(
                            "-fx-background-color: #3e3e3e; -fx-border-color: #1DB954; -fx-border-width: 0 0 0 2; -fx-background-radius: 6;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else if (hovered) {
                    container.setStyle("-fx-background-color: #282828; -fx-background-radius: 6;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else {
                    container.setStyle("-fx-background-color: transparent;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
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
                        lblAnno.setText(m.year != null && !m.year.isBlank() ? m.year : "");
                        lblGenere.setText(m.genre != null ? m.genre : "");
                        lblTag.setText(
                                m.tag != null && !m.tag.isBlank() && !m.tag.equalsIgnoreCase("NESSUNO") ? m.tag : "");

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
                        lblAnno.setText("");
                        lblGenere.setText("");
                        lblTag.setText("");
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
        playlistControlsBox.setVisible(true);
        playlistControlsBox.setManaged(true);

        mainTitleLabel.setText("🎵 Libreria Generale. Seleziona un brano per i dettagli.");
    }

    public void mostraPiuaAscoltati() {
        playlistSelezionata = null;
        playlistListView.getSelectionModel().clearSelection();
        addBtn.setVisible(false);
        addBtn.setManaged(false);
        refreshPiuaAscoltatiList();
    }

    private void impostaTabAttivo(String tab) {
        if ("LIBRERIA".equals(tab)) {
            btnMostraLibreria.setStyle(
                    "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
            if (btnMostraPiuAscoltati != null) {
                btnMostraPiuAscoltati.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
            }
        } else if ("PIU_ASCOLTATI".equals(tab)) {
            if (btnMostraPiuAscoltati != null) {
                btnMostraPiuAscoltati.setStyle(
                        "-fx-background-color: #1a1a1a; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
            }
            btnMostraLibreria.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
        } else {
            btnMostraLibreria.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
            if (btnMostraPiuAscoltati != null) {
                btnMostraPiuAscoltati.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: transparent;");
            }
        }
    }

    public void impostaPlaylist(String pName) {
        impostaTabAttivo("PLAYLIST");
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
        refreshPiuaAscoltatiList();
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
        java.util.stream.Stream<String> stream = libreriaController.getPlaylist().stream()
                .filter(p -> !(p instanceof SmartPlaylist))
                .map(Playlist::getNome);
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
        playlistSelectionListView.setPrefHeight(Math.max(200, opzioni.size() * 50 + 10));
        playlistSelectionListView.setMinHeight(playlistSelectionListView.getPrefHeight());

        switchToView(viewSelezionePlaylist);
    }

    public void refreshPiuaAscoltatiList() {
        String savedSelection = null;
        if (topSongsListView.getSelectionModel().getSelectedItem() != null) {
            savedSelection = extractFilename(topSongsListView.getSelectionModel().getSelectedItem());
        }

        // Popola la sezione delle playlist
        topPlaylistsBox.getChildren().clear();
        if (smartPlaylistsBox != null) {
            smartPlaylistsBox.getChildren().clear();
        }

        List<Playlist> topPlaylists = libreriaController.getTopPlaylistsAscoltate();
        for (Playlist pl : topPlaylists) {
            if (pl instanceof SmartPlaylist)
                continue;

            VBox card = new VBox(5);
            card.setStyle(
                    "-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;");
            card.setPrefWidth(200);

            Label title = new Label(pl.getNome());
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label count = new Label("Ascolti: " + pl.getPlayCount());
            count.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold;");

            card.getChildren().addAll(title, count);

            card.setOnMouseClicked(e -> {
                impostaPlaylist(pl.getNome());
                switchToView(viewLista);
                refreshList();
            });

            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: #383838; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));

            topPlaylistsBox.getChildren().add(card);
        }

        if (topPlaylistsBox.getChildren().isEmpty()) {
            Label empty = new Label("Nessuna playlist presente.");
            empty.setStyle("-fx-text-fill: #a7a7a7;");
            topPlaylistsBox.getChildren().add(empty);
        }

        if (smartPlaylistsBox != null) {
            // -- Card Preferiti --
            VBox prefCard = new VBox(5);
            prefCard.setStyle("-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;");
            prefCard.setPrefWidth(200);
            prefCard.setMinWidth(200);

            Label prefTitle = new Label("Preferiti");
            prefTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

            java.util.List<Brano> braniPreferiti = new java.util.ArrayList<>();
            for (com.musicplayer.model.IBrano ib : libreriaController.getBrani()) {
                if (ib instanceof Brano b) {
                    com.musicplayer.persistence.SongMetadata m = metadataMap.get(com.musicplayer.PathUtils.filenameFromPath(b.getPercorsoFile()));
                    if (m != null && m.tag != null && m.tag.contains("Preferiti")) {
                        braniPreferiti.add(b);
                    } else if (b.getTag() != null && b.getTag().getEtichetta().contains("Preferiti")) {
                        // Fallback nel caso in cui metadataMap non fosse aggiornato
                        braniPreferiti.add(b);
                    }
                }
            }

            Label prefCount = new Label("Brani: " + braniPreferiti.size());
            prefCount.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold;");

            prefCard.getChildren().addAll(prefTitle, prefCount);

            prefCard.setOnMouseClicked(e -> {
                Playlist pPref = libreriaController.getPlaylistMap().get("Preferiti");
                if (pPref == null) {
                    pPref = new com.musicplayer.model.SmartPlaylist("pref-id", "Preferiti", null, null) {
                        @Override public void ricalcola() { }
                    };
                    libreriaController.getPlaylistMap().put("Preferiti", pPref);
                }
                pPref.rimuoviBrani(pPref.getBrani());
                pPref.aggiungiBrani(braniPreferiti);

                impostaPlaylist("Preferiti");
                switchToView(viewLista);
                refreshList();
            });

            prefCard.setOnMouseEntered(e -> prefCard.setStyle("-fx-background-color: #383838; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));
            prefCard.setOnMouseExited(e -> prefCard.setStyle("-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));
            
            smartPlaylistsBox.getChildren().add(prefCard);
            // ---------------------

            for (Playlist pl : libreriaController.getPlaylist()) {
                if (!(pl instanceof com.musicplayer.model.SmartPlaylist))
                    continue;

                VBox card = new VBox(5);
                card.setStyle(
                        "-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;");
                card.setPrefWidth(200);
                card.setMinWidth(200);

                Label title = new Label(pl.getNome());
                title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

                Label count = new Label("Brani: " + pl.getBrani().size());
                count.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold;");

                card.getChildren().addAll(title, count);

                card.setOnMouseClicked(e -> {
                    impostaPlaylist(pl.getNome());
                    switchToView(viewLista);
                    refreshList();
                });

                card.setOnMouseEntered(e -> card.setStyle(
                        "-fx-background-color: #383838; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));
                card.setOnMouseExited(e -> card.setStyle(
                        "-fx-background-color: #282828; -fx-padding: 16; -fx-background-radius: 8; -fx-cursor: hand;"));

                smartPlaylistsBox.getChildren().add(card);
            }
            if (smartPlaylistsBox.getChildren().isEmpty()) {
                Label empty = new Label("Nessuna smart playlist generata.");
                empty.setStyle("-fx-text-fill: #a7a7a7;");
                smartPlaylistsBox.getChildren().add(empty);
            }
        }

        // Popola la sezione dei brani
        topSongsListView.getItems().clear();
        topSongsListView.setCellFactory(creaCellFactoryTopSongs());

        List<IBrano> topBrani = libreriaController.getTopBraniAscoltati();

        for (IBrano ib : topBrani) {
            if (ib instanceof Brano b) {
                String fn = PathUtils.filenameFromPath(b.getPercorsoFile());
                String display = (b.getTitolo() != null && !b.getTitolo().isBlank())
                        ? b.getTitolo() + " — " + fn
                        : fn;
                topSongsListView.getItems().add(display);
            }
        }

        topSongsListView.setPrefHeight(Math.max(200, topSongsListView.getItems().size() * 65 + 20));
        topSongsListView.setMinHeight(topSongsListView.getPrefHeight());

        String itemToSelect = null;
        for (String item : topSongsListView.getItems()) {
            if (extractFilename(item).equals(savedSelection)) {
                itemToSelect = item;
                break;
            }
        }
        if (itemToSelect != null) {
            topSongsListView.getSelectionModel().select(itemToSelect);
        }
    }

    public void refreshList() {
        String currentContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
        String savedSelection = selectionsMap.get(currentContext);
        songListView.getItems().clear();

        List<IBrano> braniDaMostrare;
        if (playlistSelezionata == null) {
            mainTitleLabel.setText("Gestione brani");
            songListView.setCellFactory(creaCellFactoryTrePuntini(new StatoLibreria()));
            braniDaMostrare = libreriaController.cercaBrani(filtroAttivo);
        } else {
            playlistControlsBox.setVisible(true);
            playlistControlsBox.setManaged(true);

            mainTitleLabel.setText("🎼 Playlist: " + playlistSelezionata);
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

        for (IBrano ib : braniDaMostrare) {
            if (ib instanceof Brano b) {
                String fn = PathUtils.filenameFromPath(b.getPercorsoFile());
                String display = (b.getTitolo() != null && !b.getTitolo().isBlank())
                        ? b.getTitolo() + " — " + fn
                        : fn;
                songListView.getItems().add(display);
            }
        }

        songListView.setPrefHeight(Math.max(200, songListView.getItems().size() * 65 + 20));
        songListView.setMinHeight(songListView.getPrefHeight());

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

        if (playlistSelezionata == null && savedSelection != null) {
            for (String item : songListView.getItems()) {
                if (extractFilename(item).equals(savedSelection)) {
                    songListView.getSelectionModel().select(item);
                    break;
                }
            }
        } else if (playlistSelezionata != null && gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()
                && playingContext.equals(currentContext)) {
            String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
            if (currentMedia != null && !currentMedia.isEmpty()) {
                try {
                    String fn = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName().toString();
                    for (String item : songListView.getItems()) {
                        if (extractFilename(item).equals(fn)) {
                            songListView.getSelectionModel().select(item);
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // Aggiorna il pulsante Play/Pausa in base alla vista e allo stato
        updatePlaylistPlayButtonUI();

        // Alla fine di tutte le operazioni di refresh, il processo è completo.
    }

    private void updatePlaylistPlayButtonUI() {
        if (playlistPlayBtn == null)
            return;
        String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
        if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()
                && playingContext.equals(currentViewContext)) {
            if (!isPlayerPaused) {
                playlistPlayBtn.setText("⏸");
            } else {
                playlistPlayBtn.setText("▶");
            }
        } else {
            playlistPlayBtn.setText("▶");
        }
    }

    public void promptAggiungiBranoAPlaylist(Brano selezionato) {
        TextInputDialog textDialog = new TextInputDialog("");
        textDialog.setTitle("Aggiungi a Playlist");
        textDialog.setHeaderText("Associa il brano '" + selezionato.getTitolo() + "' a una playlist");
        textDialog.setContentText("Nome della playlist di destinazione:");

        textDialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(playlistTarget -> {
            try {
                libreriaController.aggiungiAPlaylist(selezionato, playlistTarget);
            } catch (PlaylistException pe) {
                pe.printStackTrace();
                mostraErrore(pe);
            } catch (Exception ex) {
                ex.printStackTrace();
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

    private void aggiornaIteratoreCorrente() {
        String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
        if (!playingContext.equals(currentViewContext)) {
            return;
        }

        java.util.List<IBrano> listaBrani = new java.util.ArrayList<>();
        for (String item : songListView.getItems()) {
            IBrano br = findBranoByFilename(extractFilename(item));
            if (br != null) {
                listaBrani.add(br);
            }
        }

        String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
        IBrano b = null;
        if (currentMedia != null && !currentMedia.isEmpty()) {
            try {
                String currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName()
                        .toString();
                b = findBranoByFilename(currentFilename);
            } catch (Exception ignored) {
            }
        }

        PlaylistIterator iter = null;
        if (playlistShuffleEnabled) {
            ShuffleStrategy strategy = new ShuffleStrategy();
            gestoreRiproduzione.setStrategia(strategy);
            ShuffleIterator sIter = new ShuffleIterator(listaBrani);
            if (b != null)
                sIter.impostaBranoCorrente(b);
            iter = sIter;
        } else if (playlistLoopEnabled) {
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

    void aggiornaVisualizzazioneCoda() {
        javafx.application.Platform.runLater(() -> {
            if (gestoreRiproduzione == null || nextSongLabel == null)
                return;
            PlaylistIterator iter = gestoreRiproduzione.getIterator();
            if (iter != null && gestoreRiproduzione.hasActiveMedia()) {
                IBrano branoCorrente = null;
                String currentMedia = gestoreRiproduzione.getCurrentMediaSource();
                try {
                    String currentFilename = java.nio.file.Path.of(java.net.URI.create(currentMedia)).getFileName()
                            .toString();
                    branoCorrente = findBranoByFilename(currentFilename);
                } catch (Exception ignored) {
                }

                IBrano prossimo;
                if (gestoreRiproduzione.isSingleSongLoop() && branoCorrente != null) {
                    prossimo = branoCorrente;
                } else {
                    prossimo = iter.peekNext();
                }

                if (prossimo != null) {
                    nextSongLabel.setText("Prossimo: " + prossimo.getTitolo());
                } else {
                    nextSongLabel.setText("Prossimo: Fine Coda");
                }

                if (lblCodaStaiAscoltandoTitolo != null && lblCodaStaiAscoltandoAutore != null) {
                    if (branoCorrente != null) {
                        lblCodaStaiAscoltandoTitolo.setText(branoCorrente.getTitolo());
                        lblCodaStaiAscoltandoAutore.setText(branoCorrente.getDettagli().getOrDefault("autore", "-"));
                    } else {
                        lblCodaStaiAscoltandoTitolo.setText("Nessun brano");
                        lblCodaStaiAscoltandoAutore.setText("-");
                    }
                }

                if (codaListView != null) {
                    List<String> codaItems = new ArrayList<>();
                    if (gestoreRiproduzione.isSingleSongLoop() && branoCorrente != null) {
                        codaItems.add(branoCorrente.getTitolo() + " - "
                                + branoCorrente.getDettagli().getOrDefault("autore", "-"));
                    } else {
                        List<IBrano> coda = iter.getCodaBrani(20);
                        for (IBrano cb : coda) {
                            codaItems.add(cb.getTitolo() + " - " + cb.getDettagli().getOrDefault("autore", "-"));
                        }
                    }
                    codaListView.getItems().setAll(codaItems);
                }

                // Se non c'è una selezione corrente nella ListView, mostra la coda nel pannello
                // dei dettagli (vecchio fallback, per retrocompatibilità testuale)
                if (songListView.getSelectionModel().getSelectedItem() == null) {
                    StringBuilder sb = new StringBuilder("--- CODA DI RIPRODUZIONE ---\n\n");
                    if (branoCorrente != null) {
                        sb.append("▶ Ora in riproduzione:\n   ").append(branoCorrente.getTitolo()).append("\n\n");
                    }

                    if (gestoreRiproduzione.isSingleSongLoop() && branoCorrente != null) {
                        sb.append("⏭ Brani successivi:\n");
                        sb.append("   ").append(branoCorrente.getTitolo()).append("\n");
                    } else {
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
                    }
                    detailsLabel.setText(sb.toString());
                }
            } else {
                nextSongLabel.setText("");
                if (lblCodaStaiAscoltandoTitolo != null && lblCodaStaiAscoltandoAutore != null) {
                    lblCodaStaiAscoltandoTitolo.setText("Nessun brano");
                    lblCodaStaiAscoltandoAutore.setText("-");
                }
                if (codaListView != null) {
                    codaListView.getItems().clear();
                }

                if (songListView.getSelectionModel().getSelectedItem() == null) {
                    detailsLabel.setText("Seleziona un brano per i dettagli.");
                }
            }
        });
    }

    public void playSelected() {
        String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;

        String sel = null;
        ListView<String> activeListView = null;

        if (viewHome != null && viewHome.isVisible()) {
            sel = topSongsListView.getSelectionModel().getSelectedItem();
            activeListView = topSongsListView;
        } else {
            sel = songListView.getSelectionModel().getSelectedItem();
            activeListView = songListView;
        }

        if (sel == null) {
            if (gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()
                    && playingContext.equals(currentViewContext)) {
                gestoreRiproduzione.play();
                return;
            } else if (gestoreRiproduzione != null) {
                playingContext = currentViewContext;
                aggiornaIteratoreCorrente();
                gestoreRiproduzione.playNext();
                return;
            } else {
                return;
            }
        }

        playingContext = currentViewContext;

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
            if (activeListView != null) {
                for (String item : activeListView.getItems()) {
                    IBrano br = findBranoByFilename(extractFilename(item));
                    if (br != null) {
                        listaBrani.add(br);
                    }
                }
            }

            PlaylistIterator iter = null;
            if (playlistShuffleEnabled) {
                ShuffleStrategy strategy = new ShuffleStrategy();
                gestoreRiproduzione.setStrategia(strategy);
                ShuffleIterator sIter = new ShuffleIterator(listaBrani);
                sIter.impostaBranoCorrente(brano);
                iter = sIter;
            } else if (playlistLoopEnabled) {
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

    // Cell Factory specifica per la schermata Selezione Brano (mostra i metadati
    // inline)
    private javafx.util.Callback<ListView<String>, ListCell<String>> creaCellFactorySelezioneBrano() {
        return lv -> new ListCell<>() {
            private final HBox container = new HBox();
            private final Label lblId = new Label();
            private final Label lblTitolo = new Label();
            private final Label lblAutore = new Label();
            private final Label lblAnno = new Label();
            private final Label lblGenere = new Label();
            private final Label lblTag = new Label();
            private final Label lblDurata = new Label();
            private final Pane spacer = new Pane();

            {
                lblId.setMinWidth(30);
                lblId.setPrefWidth(30);
                lblId.setMaxWidth(30);

                lblTitolo.setMinWidth(160);
                lblTitolo.setPrefWidth(160);
                lblTitolo.setMaxWidth(160);

                lblAutore.setMinWidth(120);
                lblAutore.setPrefWidth(120);
                lblAutore.setMaxWidth(120);

                lblAnno.setMinWidth(50);
                lblAnno.setPrefWidth(50);
                lblAnno.setMaxWidth(50);

                lblGenere.setMinWidth(90);
                lblGenere.setPrefWidth(90);
                lblGenere.setMaxWidth(90);

                lblTag.setMinWidth(100);
                lblTag.setPrefWidth(100);
                lblTag.setMaxWidth(100);

                lblDurata.setMinWidth(50);
                lblDurata.setPrefWidth(50);
                lblDurata.setMaxWidth(50);

                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
                container.setSpacing(10);

                container.getChildren().addAll(lblId, lblTitolo, lblAutore, lblAnno, lblGenere, lblTag, spacer,
                        lblDurata);

                selectedProperty().addListener((obs, o, isSelected) -> updateStyle(isSelected, isHover()));
                hoverProperty().addListener((obs, o, isHover) -> updateStyle(isSelected(), isHover));
            }

            private void updateStyle(boolean selected, boolean hovered) {
                if (selected) {
                    container.setStyle("-fx-background-color: #1DB954; -fx-background-radius: 6;");
                    lblId.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");
                } else if (hovered) {
                    container.setStyle("-fx-background-color: #282828; -fx-background-radius: 6;");
                    lblId.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
                } else {
                    container.setStyle("-fx-background-color: transparent;");
                    lblId.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                    lblAutore.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblAnno.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblGenere.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblTag.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
                    lblDurata.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
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
                        lblAnno.setText(m.year != null && !m.year.isBlank() ? m.year : "");
                        lblGenere.setText(m.genre != null ? m.genre : "");
                        lblTag.setText(
                                m.tag != null && !m.tag.isBlank() && !m.tag.equalsIgnoreCase("NESSUNO") ? m.tag : "");

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
                        lblAnno.setText("");
                        lblGenere.setText("");
                        lblTag.setText("");
                        lblDurata.setText("--:--");
                    }

                    setGraphic(container);
                    updateStyle(isSelected(), isHover());
                }
            }
        };
    }

    // Cell Factory specifica per la schermata Top Songs (mostra gli ascolti)
    private javafx.util.Callback<ListView<String>, ListCell<String>> creaCellFactoryTopSongs() {
        return lv -> new ListCell<>() {
            private final HBox root = new HBox(10);
            private final Label lblIdx = new Label();
            private final Label lblTitolo = new Label();
            private final Label lblAutore = new Label();
            private final Label lblGenere = new Label();
            private final Label lblAscolti = new Label();
            private final Label lblDurata = new Label();
            private final Pane spacer = new Pane();

            {
                root.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !isEmpty()) {
                        topSongsListView.getSelectionModel().select(getItem());
                    }
                });
                lblIdx.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !isEmpty()) {
                        topSongsListView.getSelectionModel().select(getItem());
                        String fn = extractFilename(getItem());
                        String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
                        if (currentlyPlayingFilename != null && currentlyPlayingFilename.equals(fn)
                                && playingContext.equals(currentViewContext)) {
                            if (isPlayerPaused) {
                                gestoreRiproduzione.play();
                            } else {
                                gestoreRiproduzione.pausa();
                            }
                        } else {
                            playSelected();
                        }
                        event.consume();
                    }
                });
                lblIdx.setPrefWidth(30);
                lblTitolo.setPrefWidth(200);
                lblAutore.setPrefWidth(130);
                lblGenere.setPrefWidth(100);
                lblAscolti.setMinWidth(70);
                lblAscolti.setMaxWidth(70);
                lblAscolti.setPrefWidth(70);
                lblAscolti.setAlignment(javafx.geometry.Pos.CENTER);

                lblDurata.setMinWidth(50);
                lblDurata.setMaxWidth(50);
                lblDurata.setPrefWidth(50);
                lblDurata.setAlignment(javafx.geometry.Pos.CENTER);

                lblIdx.setStyle("-fx-text-fill: #b3b3b3;");
                lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
                lblAutore.setStyle("-fx-text-fill: #b3b3b3;");
                lblGenere.setStyle("-fx-text-fill: #b3b3b3;");
                lblAscolti.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold;");
                lblDurata.setStyle("-fx-text-fill: #b3b3b3;");

                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                root.getChildren().addAll(lblIdx, lblTitolo, lblAutore, lblGenere, spacer, lblDurata, lblAscolti);
                root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                root.setPadding(new javafx.geometry.Insets(8, 8, 8, 8));

                selectedProperty().addListener((obs, o, isSelected) -> updateTopSongsStyle(isSelected, isHover()));
                hoverProperty().addListener((obs, o, isHover) -> updateTopSongsStyle(isSelected(), isHover));
            }

            private void updateTopSongsStyle(boolean selected, boolean hovered) {
                boolean isPlaying = false;
                if (!isEmpty()) {
                    String fn = extractFilename(getItem());
                    String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
                    isPlaying = (currentlyPlayingFilename != null && currentlyPlayingFilename.equals(fn)
                            && playingContext.equals(currentViewContext));

                    if (isPlaying) {
                        if (hovered) {
                            lblIdx.setText(isPlayerPaused ? "▶" : "⏸");
                            lblIdx.setStyle(
                                    "-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
                        } else {
                            lblIdx.setText(String.valueOf(getIndex() + 1));
                            lblIdx.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-font-size: 13px;");
                        }
                    } else {
                        if (hovered) {
                            lblIdx.setText("▶");
                            lblIdx.setStyle(
                                    "-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
                        } else {
                            lblIdx.setText(String.valueOf(getIndex() + 1));
                            lblIdx.setStyle("-fx-text-fill: #b3b3b3;");
                        }
                    }
                }

                if (isPlaying) {
                    root.setStyle(
                            "-fx-background-color: #282828; -fx-border-color: #1DB954; -fx-border-width: 0 0 0 4; -fx-background-radius: 4;");
                    lblTitolo.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold;");
                } else if (selected) {
                    root.setStyle("-fx-background-color: #333333; -fx-background-radius: 4;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
                } else if (hovered) {
                    root.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 4;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
                } else {
                    root.setStyle("-fx-background-color: transparent;");
                    lblTitolo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblIdx.setText(String.valueOf(getIndex() + 1));
                    String fn = extractFilename(item);
                    Brano b = findBranoByFilename(fn);
                    if (b != null) {
                        lblTitolo.setText(b.getTitolo());
                        lblAutore.setText(b.getAutore());
                        lblGenere.setText(b.getGenere());
                        lblAscolti.setText(String.valueOf(b.getPlayCount()));
                        int d = b.getDurata();
                        lblDurata.setText(String.format("%d:%02d", d / 60, d % 60));
                    } else {
                        lblTitolo.setText(fn);
                        lblAutore.setText("");
                        lblGenere.setText("");
                        lblAscolti.setText("0");
                        lblDurata.setText("--:--");
                    }
                    setGraphic(root);
                    updateTopSongsStyle(isSelected(), isHover());
                }
            }
        };
    }

    public void showDetails(String display) {
        if (display == null) {
            detailsLabel.setText("Nessun brano selezionato.");
            return;
        }

        String fn = extractFilename(display);
        SongMetadata m = metadataMap.get(fn);

        if (m == null) {
            detailsLabel.setText(fn);
        } else {
            String durStr = "N/D";
            try {
                if (m.duration != null && !m.duration.isBlank()) {
                    int sec = Integer.parseInt(m.duration);
                    durStr = String.format("%d:%02d", sec / 60, sec % 60);
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
            Command cmd = new RimuoviDaLibreriaCmd(libreriaController, branoDaEliminare);
            cmd.esegui();
            if (undoManager != null) {
                undoManager.aggiungiComando(cmd);
                mostraNotificaUndo("Brano rimosso dalla libreria");
            }
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
            } catch (PlaylistException e) {
                mostraErrore(e);
            }
        });
    }

    public void mostraErrore(PlaylistException ex) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Playlist");
            alert.setHeaderText("Operazione non consentita");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
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
        aggiornaIteratoreCorrente();
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
        aggiornaIteratoreCorrente();
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
            if (libreriaController.getUltimoCampoOrdinamento() == null) {
                resetHeaderLabels();
            }
            aggiornaStatoUndo();
        });
    }

    private void initPlaylistControlsHandlers() {
        if (playlistPlayBtn != null) {
            playlistPlayBtn.setOnAction(e -> {
                animatePlaylistButton(playlistPlayBtn);
                String currentViewContext = playlistSelezionata == null ? "LIBRERIA" : playlistSelezionata;
                boolean isPlayingContext = gestoreRiproduzione != null && gestoreRiproduzione.hasActiveMedia()
                        && playingContext.equals(currentViewContext);

                if (isPlayingContext) {
                    // Stesso contesto: toggle play/pausa, ignora la selezione nella lista
                    if (isPlayerPaused) {
                        gestoreRiproduzione.play();
                    } else {
                        gestoreRiproduzione.pausa();
                    }
                } else {
                    // Contesto diverso o nulla in riproduzione: avvia la playlist
                    ListView<String> activeListView = viewHome.isVisible() ? topSongsListView : songListView;
                    if (!activeListView.getItems().isEmpty()) {
                        if (playlistShuffleEnabled) {
                            int randomIdx = new java.util.Random().nextInt(activeListView.getItems().size());
                            activeListView.getSelectionModel().select(randomIdx);
                        } else {
                            activeListView.getSelectionModel().selectFirst();
                        }
                        playSelected();
                    }
                }
            });
        }

        if (playlistStopBtn != null) {
            playlistStopBtn.setOnAction(e -> {
                animatePlaylistButton(playlistStopBtn);
                if (gestoreRiproduzione != null) {
                    gestoreRiproduzione.eseguiStop();
                    gestoreRiproduzione.clearMedia(); // Rimuove il brano corrente
                    if (songListView != null) {
                        songListView.getSelectionModel().clearSelection(); // Deseleziona per ripartire dall'inizio
                    }
                    aggiornaIteratoreCorrente(); // Ricrea l'iteratore dall'inizio
                    gestoreRiproduzione.setStato(new StoppedState());
                }
            });
        }

        if (playlistShuffleBtn != null) {
            playlistShuffleBtn.setOnAction(e -> handleShuffleToggle());
        }

        if (shuffleBtn != null) {
            shuffleBtn.setOnAction(e -> handleShuffleToggle());
        }

        if (playlistLoopBtn != null) {
            playlistLoopBtn.setOnAction(e -> {
                playlistLoopEnabled = !playlistLoopEnabled;
                if (playlistLoopEnabled) {
                    playlistLoopBtn
                            .setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand;");
                    if (playlistShuffleEnabled) {
                        playlistShuffleEnabled = false;
                        if (playlistShuffleBtn != null) {
                            playlistShuffleBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                        }
                        if (shuffleBtn != null) {
                            shuffleBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                        }
                    }
                } else {
                    playlistLoopBtn
                            .setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                }
                aggiornaIteratoreCorrente();
            });
        }
    }

    private void handleShuffleToggle() {
        playlistShuffleEnabled = !playlistShuffleEnabled;
        if (playlistShuffleEnabled) {
            if (playlistShuffleBtn != null)
                playlistShuffleBtn
                        .setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand;");
            if (shuffleBtn != null)
                shuffleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-cursor: hand;");

            if (playlistLoopEnabled) {
                playlistLoopEnabled = false;
                if (playlistLoopBtn != null) {
                    playlistLoopBtn
                            .setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
                }
            }
        } else {
            if (playlistShuffleBtn != null)
                playlistShuffleBtn
                        .setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
            if (shuffleBtn != null)
                shuffleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-cursor: hand;");
        }
        aggiornaIteratoreCorrente();
    }

    private void setupArrowButton(Button btn) {
        if (btn == null)
            return;

        btn.setOnMousePressed(e -> {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold;");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100),
                    btn);
            st.setToX(0.8);
            st.setToY(0.8);
            st.play();
        });
        btn.setOnMouseReleased(e -> {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold;");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100),
                    btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold;");
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold;");
        });
    }

    private void animatePlaylistButton(Button btn) {
        if (btn == null)
            return;

        if (btn.getProperties().containsKey("isAnimating"))
            return;
        btn.getProperties().put("isAnimating", true);

        String originalStyle;
        String greenStyle;

        if (btn == playlistPlayBtn) {
            originalStyle = "-fx-background-color: #1DB954; -fx-text-fill: #000000; -fx-background-radius: 50%; -fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;";
            greenStyle = "-fx-background-color: #15883e; -fx-text-fill: #ffffff; -fx-background-radius: 50%; -fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42; -fx-cursor: hand;";
        } else {
            originalStyle = "-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-cursor: hand;";
            greenStyle = "-fx-background-color: transparent; -fx-text-fill: #1DB954; -fx-font-size: 24px; -fx-cursor: hand;";
        }

        btn.setStyle(greenStyle);

        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(100), btn);
        scaleDown.setToX(0.85);
        scaleDown.setToY(0.85);

        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(
                javafx.util.Duration.millis(100), btn);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        scaleDown.setOnFinished(e -> scaleUp.play());

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
        pause.setOnFinished(e -> {
            btn.setStyle(originalStyle);
            btn.getProperties().remove("isAnimating");
        });

        scaleDown.play();
        pause.play();
    }

    public void eliminaBraniMassivo(List<String> items) {
        if (items == null || items.isEmpty())
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare i " + items.size() + " brani selezionati?",
                ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                List<Brano> brani = new ArrayList<>();
                for (String item : items) {
                    Brano b = findBranoByFilename(extractFilename(item));
                    if (b != null) {
                        brani.add(b);
                    }
                }
                Command cmd = new RimuoviMassivoLibreriaCmd(libreriaController, brani);
                cmd.esegui();
                if (undoManager != null) {
                    undoManager.aggiungiComando(cmd);
                    mostraNotificaUndo("Rimozione massiva brani dalla libreria");
                }
                metadataMap.clear();
                MetadataService.caricaMappaDalCSV(metadataMap);
                refreshList();
                refreshPlaylistList();
                aggiornaCuorePreferiti();
            } catch (Exception ex) {
                mostraErrore(new ValidazioneException("Errore durante l'eliminazione di massa: " + ex.getMessage()));
            }
        }
    }

    public void aggiungiBraniAPlaylistMassivo(List<String> items) {
        if (items == null || items.isEmpty())
            return;
        List<Brano> brani = new ArrayList<>();
        for (String item : items) {
            Brano b = findBranoByFilename(extractFilename(item));
            if (b != null) {
                brani.add(b);
            }
        }
        if (!brani.isEmpty()) {
            apriSelezionePlaylistMassivo(brani);
        }
    }

    public void rimuoviBraniDaPlaylistMassivo(List<String> items, String playlistName) {
        if (items == null || items.isEmpty() || playlistName == null)
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Rimuovere i " + items.size() + " brani selezionati da questa playlist?", ButtonType.YES,
                ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                List<Brano> brani = new ArrayList<>();
                for (String item : items) {
                    Brano b = findBranoByFilename(extractFilename(item));
                    if (b != null) {
                        brani.add(b);
                    }
                }
                Command cmd = new RimuoviMassivoCmd(libreriaController, brani, playlistName);
                cmd.esegui();
                if (undoManager != null) {
                    undoManager.aggiungiComando(cmd);
                    mostraNotificaUndo("Rimozione massiva brani da playlist");
                }
                refreshList();
                refreshPlaylistList();
            } catch (ValidazioneException ve) {
                mostraErrore(ve);
            } catch (Exception ex) {
                mostraErrore(new ValidazioneException("Errore durante la rimozione di massa: " + ex.getMessage()));
            }
        }
    }

    public void apriSelezionePlaylistMassivo(List<Brano> brani) {
        this.braniInAttesaDiPlaylistMassivo = brani;
        this.branoInAttesaDiPlaylist = null;

        if (libreriaController.getPlaylist().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Nessuna playlist trovata. Creane una!");
            alert.showAndWait();
            switchToView(viewCreazione);
            return;
        }

        java.util.stream.Stream<String> stream = libreriaController.getPlaylist().stream()
                .filter(p -> !(p instanceof SmartPlaylist))
                .map(Playlist::getNome);
        if (playlistSelezionata != null) {
            stream = stream.filter(nome -> !nome.equals(playlistSelezionata));
        }

        java.util.List<String> opzioni = stream.toList();
        if (opzioni.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Non ci sono altre playlist disponibili.");
            alert.showAndWait();
            return;
        }

        playlistSelectionListView.getItems().setAll(opzioni);
        switchToView(viewSelezionePlaylist);
    }

    private void handleOrdinamento(CampoOrdinamento campo, Label labelCliccata) {
        libreriaController.ordinaLibreria(campo, playlistSelezionata);

        if (lblHeaderTitolo != null)
            lblHeaderTitolo.setText("TITOLO");
        if (lblHeaderAutore != null)
            lblHeaderAutore.setText("AUTORE");
        if (lblHeaderAnno != null)
            lblHeaderAnno.setText("ANNO");
        if (lblHeaderGenere != null)
            lblHeaderGenere.setText("GENERE");
        if (lblHeaderTag != null)
            lblHeaderTag.setText("TAG");

        CampoOrdinamento ultimo = libreriaController.getUltimoCampoOrdinamento();
        if (ultimo != null) {
            boolean crescente = libreriaController.isUltimoOrdineCrescente();
            labelCliccata.setText(campo.getEtichetta().toUpperCase() + (crescente ? " ▲" : " ▼"));
        }

        refreshList();
        aggiornaIteratoreCorrente();
    }
}
