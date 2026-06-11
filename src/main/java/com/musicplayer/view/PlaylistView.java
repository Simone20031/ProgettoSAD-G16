package com.musicplayer.view;

import com.musicplayer.model.*;
import com.musicplayer.controller.*;


import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

public class PlaylistView {

    private final ListView<String> playlistListView;
    private final VBox viewLista;
    private final VBox viewCreazione;
    private final TextField playlistNameField;
    private final Button createPlaylistBtn;
    private final Button btnAnnullaCreazione;
    private final Button btnApriCreazione;

    private final LibreriaController libreriaController;
    private final LibreriaView libreriaView; // Per chiamare switchToView, refreshList, mostraErrore

    private final Label lblGestionePlaylistTitle;
    private final VBox viewSelezioneBrano;
    private final ListView<String> branoSelectionListView;
    private final Button btnAnnullaSelezioneBrano;
    private final Button btnConfermaSelezioneBrano;
    
    private final TextField addSearchTitoloField;
    private final TextField addSearchAutoreField;
    private final ComboBox<String> addSearchAnnoCombo;
    private final ComboBox<String> addSearchGenereCombo;
    private final ComboBox<String> addSearchTagCombo;
    private final Button addResetSearchBtn;

    private String playlistDaRinominare = null;
    private String playlistPerAggiuntaBrani = null;
    private java.util.List<Brano> braniDisponibiliOriginali = new java.util.ArrayList<>();

    public PlaylistView(ListView<String> playlistListView, VBox viewLista, VBox viewCreazione,
            TextField playlistNameField, Button createPlaylistBtn, Button btnAnnullaCreazione,
            Button btnApriCreazione, Label lblGestionePlaylistTitle,
            VBox viewSelezioneBrano, ListView<String> branoSelectionListView,
            Button btnAnnullaSelezioneBrano, Button btnConfermaSelezioneBrano, 
            TextField addSearchTitoloField, TextField addSearchAutoreField, ComboBox<String> addSearchAnnoCombo,
            ComboBox<String> addSearchGenereCombo, ComboBox<String> addSearchTagCombo, Button addResetSearchBtn,
            LibreriaController libreriaController, LibreriaView libreriaView) {
        this.playlistListView = playlistListView;
        this.viewLista = viewLista;
        this.viewCreazione = viewCreazione;
        this.playlistNameField = playlistNameField;
        this.createPlaylistBtn = createPlaylistBtn;
        this.btnAnnullaCreazione = btnAnnullaCreazione;
        this.btnApriCreazione = btnApriCreazione;
        
        this.lblGestionePlaylistTitle = lblGestionePlaylistTitle;
        this.viewSelezioneBrano = viewSelezioneBrano;
        this.branoSelectionListView = branoSelectionListView;
        this.btnAnnullaSelezioneBrano = btnAnnullaSelezioneBrano;
        this.btnConfermaSelezioneBrano = btnConfermaSelezioneBrano;
        
        this.addSearchTitoloField = addSearchTitoloField;
        this.addSearchAutoreField = addSearchAutoreField;
        this.addSearchAnnoCombo = addSearchAnnoCombo;
        this.addSearchGenereCombo = addSearchGenereCombo;
        this.addSearchTagCombo = addSearchTagCombo;
        this.addResetSearchBtn = addResetSearchBtn;
        
        this.libreriaController = libreriaController;
        this.libreriaView = libreriaView;
    }

    public void initialize() {
        if (playlistListView == null)
            return;

        btnApriCreazione.setOnAction(e -> {
            playlistDaRinominare = null;
            if (lblGestionePlaylistTitle != null) {
                lblGestionePlaylistTitle.setText("Nuova Playlist");
            }
            createPlaylistBtn.setText("CREA");
            playlistNameField.clear();
            libreriaView.switchToView(viewCreazione);
        });

        btnAnnullaCreazione.setOnAction(e -> {
            playlistDaRinominare = null;
            libreriaView.switchToView(viewLista);
        });
        createPlaylistBtn.setOnAction(e -> handleSalvaPlaylist());

        if (btnAnnullaSelezioneBrano != null) {
            btnAnnullaSelezioneBrano.setOnAction(e -> {
                playlistPerAggiuntaBrani = null;
                libreriaView.switchToView(viewLista);
            });
        }

        if (btnConfermaSelezioneBrano != null) {
            btnConfermaSelezioneBrano.setOnAction(e -> eseguiAggiuntaBrani());
        }
        
        if (addSearchTitoloField != null) {
            addSearchTitoloField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltroAggiuntaBrani());
            addSearchAutoreField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltroAggiuntaBrani());
            addSearchAnnoCombo.valueProperty().addListener((obs, oldVal, newVal) -> applicaFiltroAggiuntaBrani());
            addSearchGenereCombo.valueProperty().addListener((obs, oldVal, newVal) -> applicaFiltroAggiuntaBrani());
            addSearchTagCombo.valueProperty().addListener((obs, oldVal, newVal) -> applicaFiltroAggiuntaBrani());
            
            addResetSearchBtn.setOnAction(e -> {
                addSearchTitoloField.clear();
                addSearchAutoreField.clear();
                addSearchAnnoCombo.setValue(null);
                addSearchGenereCombo.setValue(null);
                addSearchTagCombo.setValue(null);
                applicaFiltroAggiuntaBrani();
            });
        }

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                libreriaView.impostaPlaylist(n);
                libreriaView.switchToView(viewLista);
                libreriaView.refreshList();
            }
        });

        if (branoSelectionListView != null) {
            branoSelectionListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
                libreriaView.showDetails(n);
            });
        }

        playlistListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox container = new HBox();
            private final Label labelTesto = new Label();
            private final Button btnOpzioni = new Button("⋮");
            private final ContextMenu menu = new ContextMenu();

            {
                labelTesto.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(labelTesto, Priority.ALWAYS);

                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                container.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
                btnOpzioni.setFocusTraversable(false);

                container.getChildren().addAll(labelTesto, btnOpzioni);

                MenuItem aggiungiBraniItem = new MenuItem("➕ Aggiungi Brani");
                aggiungiBraniItem.setOnAction(e -> handleAggiungiBraniAPlaylist(getItem()));

                MenuItem rinominaItem = new MenuItem("✏️ Rinomina Playlist");
                rinominaItem.setOnAction(e -> handleRinominaPlaylist(getItem()));

                MenuItem eliminaItem = new MenuItem("🗑️ Elimina Playlist");
                eliminaItem.setStyle("-fx-text-fill: red;");
                eliminaItem.setOnAction(e -> handleEliminaPlaylist(getItem()));

                menu.getItems().addAll(aggiungiBraniItem, rinominaItem, eliminaItem);

                btnOpzioni.setOnAction(e -> {
                    e.consume();
                    menu.show(btnOpzioni, javafx.geometry.Side.BOTTOM, 0, 0);
                });

                selectedProperty().addListener((obs, o, isSelected) -> updateStyle(isSelected, isHover()));
                hoverProperty().addListener((obs, o, isHover) -> updateStyle(isSelected(), isHover));
            }

            private void updateStyle(boolean selected, boolean hovered) {
                if (selected) {
                    container.setStyle("-fx-background-color: #1DB954; -fx-background-radius: 6;");
                    labelTesto.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #000000; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
                } else if (hovered) {
                    container.setStyle("-fx-background-color: #282828; -fx-background-radius: 6;");
                    labelTesto.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: normal;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
                } else {
                    container.setStyle("-fx-background-color: transparent;");
                    labelTesto.setStyle("-fx-text-fill: #b3b3b3; -fx-font-weight: normal;");
                    btnOpzioni.setStyle(
                            "-fx-text-fill: #b3b3b3; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: transparent; -fx-padding: 2 6 2 6;");

                prefWidthProperty().bind(playlistListView.widthProperty().subtract(15));

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    labelTesto.setText(item);
                    setGraphic(container);
                    updateStyle(isSelected(), isHover());
                }
            }
        });
    }

    public void refreshPlaylistList(String selectedName) {
        if (playlistListView == null)
            return;
        playlistListView.getItems().clear();
        for (Playlist pl : libreriaController.getPlaylist()) {
            playlistListView.getItems().add(pl.getNome());
        }
        if (selectedName != null) {
            playlistListView.getSelectionModel().select(selectedName);
        }
    }

    private void handleSalvaPlaylist() {
        String nome = playlistNameField.getText();
        try {
            if (nome == null || nome.trim().isEmpty()) {
                throw new ValidazioneException("Il nome della playlist non può essere vuoto.",
                        ValidazioneException.TipoErrore.CAMPO_MANCANTE, "Nome");
            }
            
            String cleanInput = nome.trim();
            if (playlistDaRinominare != null) {
                // Rinomina
                libreriaController.rinominaPlaylist(playlistDaRinominare, cleanInput);
                if (playlistDaRinominare.equals(libreriaView.getPlaylistSelezionata())) {
                    libreriaView.impostaPlaylist(cleanInput);
                }
                libreriaView.refreshPlaylistList();
                playlistDaRinominare = null;
            } else {
                // Creazione nuova
                libreriaController.aggiungiAPlaylist(null, cleanInput);
                libreriaView.impostaPlaylist(cleanInput);
                libreriaView.refreshList();
                libreriaView.refreshPlaylistList();
            }
            
            libreriaView.switchToView(viewLista);
        } catch (ValidazioneException ex) {
            libreriaView.mostraErrore(ex);
        } catch (Exception ex) {
            libreriaView.mostraErrore(new ValidazioneException("Errore gestione playlist: " + ex.getMessage()));
        }
    }

    private void handleRinominaPlaylist(String vecchioNome) {
        if (vecchioNome == null)
            return;
        
        playlistDaRinominare = vecchioNome;
        if (lblGestionePlaylistTitle != null) {
            lblGestionePlaylistTitle.setText("Rinomina Playlist");
        }
        createPlaylistBtn.setText("SALVA");
        playlistNameField.setText(vecchioNome);
        libreriaView.switchToView(viewCreazione);
    }

    private void handleEliminaPlaylist(String nomePlaylist) {
        if (nomePlaylist == null)
            return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Eliminare '" + nomePlaylist + "'?", ButtonType.YES,
                ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                libreriaController.eliminaPlaylist(nomePlaylist);
                if (nomePlaylist.equals(libreriaView.getPlaylistSelezionata())) {
                    libreriaView.mostraLibreriaGenerale();
                }
                libreriaView.refreshPlaylistList();
            } catch (Exception ex) {
                libreriaView.mostraErrore(new ValidazioneException("Errore eliminazione: " + ex.getMessage()));
            }
        }
    }

    private void handleAggiungiBraniAPlaylist(String nomePlaylist) {
        if (nomePlaylist == null || viewSelezioneBrano == null)
            return;
        Playlist target = null;
        for (Playlist p : libreriaController.getPlaylist()) {
            if (p.getNome().equals(nomePlaylist)) {
                target = p;
                break;
            }
        }
        if (target == null)
            return;

        java.util.List<Brano> braniDisponibili = new java.util.ArrayList<>();
        java.util.Set<String> anni = new java.util.HashSet<>();
        java.util.Set<String> generi = new java.util.HashSet<>();
        java.util.Set<String> tags = new java.util.HashSet<>();

        for (IBrano ib : libreriaController.getBrani()) {
            if (ib instanceof Brano b) {
                boolean giaPresente = false;
                for (IBrano pBrano : target.getBrani()) {
                    if (pBrano instanceof Brano pb && pb.getPercorsoFile().equals(b.getPercorsoFile())) {
                        giaPresente = true;
                        break;
                    }
                }
                if (!giaPresente) {
                    braniDisponibili.add(b);
                    if (b.getAnno() > 0) anni.add(String.valueOf(b.getAnno()));
                    if (b.getGenere() != null && !b.getGenere().isEmpty()) generi.add(b.getGenere());
                    if (b.getTag() != null && b.getTag().getEtichetta() != null && !b.getTag().getEtichetta().isEmpty()) {
                        for (String t : b.getTag().getEtichetta().split(",")) {
                            tags.add(t.trim());
                        }
                    }
                }
            }
        }
        if (braniDisponibili.isEmpty()) {
            libreriaView.showAlert("Tutti i brani sono già presenti in " + nomePlaylist, Alert.AlertType.INFORMATION);
            return;
        }

        playlistPerAggiuntaBrani = nomePlaylist;
        braniDisponibiliOriginali = braniDisponibili;
        
        if (addSearchAnnoCombo != null) {
            java.util.List<String> listAnni = new java.util.ArrayList<>(anni);
            java.util.Collections.sort(listAnni);
            listAnni.add(0, "");
            addSearchAnnoCombo.getItems().setAll(listAnni);
            
            java.util.List<String> listGeneri = new java.util.ArrayList<>(generi);
            java.util.Collections.sort(listGeneri);
            listGeneri.add(0, "");
            addSearchGenereCombo.getItems().setAll(listGeneri);
            
            java.util.List<String> listTags = new java.util.ArrayList<>(tags);
            java.util.Collections.sort(listTags);
            listTags.add(0, "");
            addSearchTagCombo.getItems().setAll(listTags);
            
            addSearchTitoloField.clear();
            addSearchAutoreField.clear();
            addSearchAnnoCombo.setValue(null);
            addSearchGenereCombo.setValue(null);
            addSearchTagCombo.setValue(null);
        }
        applicaFiltroAggiuntaBrani();
        libreriaView.switchToView(viewSelezioneBrano);
    }
    
    private void applicaFiltroAggiuntaBrani() {
        if (addSearchTitoloField == null) return;
        
        String t = addSearchTitoloField.getText() == null ? "" : addSearchTitoloField.getText().toLowerCase().trim();
        String a = addSearchAutoreField.getText() == null ? "" : addSearchAutoreField.getText().toLowerCase().trim();
        String anno = addSearchAnnoCombo.getValue();
        String gen = addSearchGenereCombo.getValue();
        String tag = addSearchTagCombo.getValue();

        java.util.List<String> filtrati = new java.util.ArrayList<>();
        for (Brano b : braniDisponibiliOriginali) {
            boolean matches = true;
            if (!t.isEmpty() && !b.getTitolo().toLowerCase().contains(t)) matches = false;
            if (!a.isEmpty() && !b.getAutore().toLowerCase().contains(a)) matches = false;
            if (anno != null && !anno.isEmpty() && (b.getAnno() <= 0 || !String.valueOf(b.getAnno()).equals(anno))) matches = false;
            if (gen != null && !gen.isEmpty() && (b.getGenere() == null || !b.getGenere().equals(gen))) matches = false;
            if (tag != null && !tag.isEmpty()) {
                if (b.getTag() == null || b.getTag().getEtichetta() == null) {
                    matches = false;
                } else {
                    boolean hasTag = false;
                    for (String tk : b.getTag().getEtichetta().split(",")) {
                        if (tk.trim().equals(tag)) {
                            hasTag = true;
                            break;
                        }
                    }
                    if (!hasTag) matches = false;
                }
            }
            if (matches) {
                filtrati.add(b.getTitolo() + " — " + java.nio.file.Paths.get(b.getPercorsoFile()).getFileName().toString());
            }
        }
        branoSelectionListView.getItems().setAll(filtrati);
    }

    private void eseguiAggiuntaBrani() {
        if (playlistPerAggiuntaBrani == null) return;
        String sel = branoSelectionListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            libreriaView.showAlert("Nessun brano selezionato.", Alert.AlertType.WARNING);
            return;
        }
        
        String fn = sel.substring(sel.lastIndexOf("—") + 1).trim();
        Brano b = null;
        for (IBrano ib : libreriaController.getBrani()) {
            if (ib instanceof Brano && java.nio.file.Paths.get(((Brano)ib).getPercorsoFile()).getFileName().toString().equals(fn)) {
                b = (Brano) ib;
                break;
            }
        }
        
        if (b != null) {
            try {
                libreriaController.aggiungiAPlaylist(b, playlistPerAggiuntaBrani);
                libreriaView.impostaPlaylist(playlistPerAggiuntaBrani);
                playlistPerAggiuntaBrani = null;
            } catch (Exception ex) {
                libreriaView.mostraErrore(new ValidazioneException("Errore aggiunta: " + ex.getMessage()));
            }
        }
    }
}
