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

    public PlaylistView(ListView<String> playlistListView, VBox viewLista, VBox viewCreazione,
            TextField playlistNameField, Button createPlaylistBtn, Button btnAnnullaCreazione,
            Button btnApriCreazione, LibreriaController libreriaController, LibreriaView libreriaView) {
        this.playlistListView = playlistListView;
        this.viewLista = viewLista;
        this.viewCreazione = viewCreazione;
        this.playlistNameField = playlistNameField;
        this.createPlaylistBtn = createPlaylistBtn;
        this.btnAnnullaCreazione = btnAnnullaCreazione;
        this.btnApriCreazione = btnApriCreazione;
        this.libreriaController = libreriaController;
        this.libreriaView = libreriaView;
    }

    public void initialize() {
        if (playlistListView == null)
            return;

        btnApriCreazione.setOnAction(e -> {
            playlistNameField.clear();
            libreriaView.switchToView(viewCreazione);
        });

        btnAnnullaCreazione.setOnAction(e -> libreriaView.switchToView(viewLista));
        createPlaylistBtn.setOnAction(e -> handleNuovaPlaylist());

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                libreriaView.impostaPlaylist(n);
                libreriaView.switchToView(viewLista);
                libreriaView.refreshList();
            }
        });

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

    private void handleNuovaPlaylist() {
        String nome = playlistNameField.getText();
        try {
            if (nome == null || nome.trim().isEmpty()) {
                throw new ValidazioneException("Il nome della playlist non può essere vuoto.",
                        ValidazioneException.TipoErrore.CAMPO_MANCANTE, "Nome");
            }
            libreriaController.aggiungiAPlaylist(null, nome);
            libreriaView.switchToView(viewLista);
            libreriaView.impostaPlaylist(nome.trim());
            libreriaView.refreshList();
        } catch (ValidazioneException ex) {
            libreriaView.mostraErrore(ex);
        }
    }

    private void handleRinominaPlaylist(String vecchioNome) {
        if (vecchioNome == null)
            return;
        TextInputDialog dialog = new TextInputDialog(vecchioNome);
        dialog.setTitle("Rinomina Playlist");
        dialog.setHeaderText("Rinomina la playlist '" + vecchioNome + "'");
        dialog.setContentText("Nuovo nome:");

        dialog.showAndWait().ifPresent(inputNome -> {
            try {
                String cleanInput = inputNome.trim();
                if (cleanInput.isEmpty()) {
                    throw new ValidazioneException("Il nome della playlist non può essere vuoto.");
                }
                libreriaController.rinominaPlaylist(vecchioNome, cleanInput);
                if (vecchioNome.equals(libreriaView.getPlaylistSelezionata())) {
                    libreriaView.impostaPlaylist(cleanInput);
                }
                libreriaView.refreshPlaylistList();
            } catch (ValidazioneException ve) {
                libreriaView.mostraErrore(ve);
            } catch (Exception ex) {
                libreriaView.mostraErrore(new ValidazioneException("Errore rinomina: " + ex.getMessage()));
            }
        });
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
        if (nomePlaylist == null)
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
        for (IBrano ib : libreriaController.getBrani()) {
            if (ib instanceof Brano b) {
                boolean giaPresente = false;
                for (IBrano pBrano : target.getBrani()) {
                    if (pBrano instanceof Brano pb && pb.getPercorsoFile().equals(b.getPercorsoFile())) {
                        giaPresente = true;
                        break;
                    }
                }
                if (!giaPresente)
                    braniDisponibili.add(b);
            }
        }

        if (braniDisponibili.isEmpty()) {
            libreriaView.showAlert("Tutti i brani sono già presenti.", Alert.AlertType.INFORMATION);
            return;
        }

        Dialog<Brano> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi brani a " + nomePlaylist);
        dialog.setHeaderText("Seleziona il brano da aggiungere");

        ListView<Brano> listView = new ListView<>();
        listView.getItems().addAll(braniDisponibili);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Brano item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else
                    setText(item.getTitolo() + " — "
                            + java.nio.file.Paths.get(item.getPercorsoFile()).getFileName().toString());
            }
        });
        dialog.getDialogPane().setContent(listView);

        ButtonType btnAggiungi = new ButtonType("Aggiungi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAggiungi, ButtonType.CANCEL);

        dialog.setResultConverter(
                button -> button == btnAggiungi ? listView.getSelectionModel().getSelectedItem() : null);
        dialog.showAndWait().ifPresent(b -> {
            try {
                libreriaController.aggiungiAPlaylist(b, nomePlaylist);
            } catch (Exception ex) {
                libreriaView.mostraErrore(new ValidazioneException("Errore aggiunta: " + ex.getMessage()));
            }
        });
    }
}
