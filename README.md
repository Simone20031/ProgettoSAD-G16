# ProgettoSAD-G16 — Sonic Wave 🎵

**Sonic Wave** è un'applicazione desktop sviluppata in **Java 17** con **JavaFX** per la gestione e la riproduzione di una libreria musicale locale (file MP3).  
Il progetto applica i principi dell'Ingegneria del Software e i **Design Pattern (GoF)** per garantire una struttura modulare, manutenibile e scalabile.

---

## 🎵 Funzionalità Principali

| Funzionalità | Descrizione |
|---|---|
| **Gestione Libreria** | Aggiunta (singola e massiva), rimozione e organizzazione di brani MP3 con supporto Undo/Redo |
| **Playlist & Smart Playlist** | Creazione di playlist standard e *Smart Playlist* automatiche, che si aggiornano dinamicamente in base a filtri (`FiltroRicerca`) |
| **Riproduzione Musicale** | Player integrato con controlli Play / Pausa / Stop / Skip, barra di avanzamento e gestione della coda |
| **Modalità di Riproduzione** | Sequenziale, Casuale (Shuffle), Loop continuo — selezionabili a runtime |
| **Ricerca e Filtraggio** | Ricerca avanzata per testo libero o filtri specifici (genere, tag, artista) |
| **Ordinamento** | Ordinamento della libreria per titolo, autore, anno, genere o tag (ascendente/discendente) |
| **Gestione Tag e Metadati** | Classificazione tramite generi predefiniti (`Genere`) e tag personalizzati (`Tag`) |
| **Statistiche di Ascolto** | Classifica dei brani e delle playlist più ascoltati |
| **Menu Contestuale** | Opzioni dinamiche sul singolo brano in base allo stato corrente dell'interfaccia (Libreria vs Playlist) |
| **Persistenza Dati** | Salvataggio permanente su file CSV di metadati, playlist e contatori di ascolto |

---

## 🛠️ Tecnologie Utilizzate

| Tecnologia | Versione | Utilizzo |
|---|---|---|
| **Java** | 17 | Linguaggio principale |
| **JavaFX** | 17.0.13 | Interfaccia grafica (GUI) con file FXML |
| **Maven** | — | Build automation e gestione dipendenze |
| **JUnit 5** | 5.10.0 | Testing unitario |

---

## 📐 Architettura e Design Pattern (GoF)

L'architettura del software si basa sul pattern **MVC** (Model-View-Controller), mantenendo separata la logica di dominio dall'interfaccia utente.

### Pattern Implementati

| Pattern | Tipo | Classi Principali | Scopo |
|---|---|---|---|
| **Factory Method** | Creazionale | `BranoFactory`, `BranoCreator` | Creazione strutturata degli oggetti musicali (`Brano`) con validazione centralizzata |
| **Singleton** | Creazionale | `Libreria`, `GestoreRiproduzione` | Garantisce un'unica istanza globale per i componenti centrali del sistema (collezione brani e player audio) |
| **Proxy** | Strutturale | `BranoProxy`, `IBrano` | Virtual Proxy per il caricamento lazy dei metadati dal CSV — i dettagli vengono caricati solo quando effettivamente richiesti |
| **Adapter** | Strutturale | `Mp3Adapter`, `Mp3LibFile`, `Playable` | Adatta l'interfaccia di una libreria esterna per file MP3 (`Mp3LibFile`) all'interfaccia `Playable` del sistema |
| **Observer** | Comportamentale | `LibreriaObserver`, `RiproduzioneObserver`, `StatisticheAscolto`, `SmartPlaylist` | Notifica automatica dei cambiamenti nella libreria (aggiunta/rimozione brani) e nello stato del player |
| **Iterator** | Comportamentale | `PlaylistIterator`, `SequentialIterator`, `ShuffleIterator`, `LoopIterator` | Attraversamento trasparente delle playlist secondo diverse strategie di riproduzione |
| **Command** | Comportamentale | `Command`, `AggiungiALibreriaCmd`, `RimuoviDaLibreriaCmd`, `AggiungiAPlaylistCmd`, `RimuoviDaPlaylistCmd`, `AggiungiMassivoCmd`, `RimuoviMassivoCmd`, `RimuoviMassivoLibreriaCmd`, `UndoManager` | Incapsulamento delle operazioni come oggetti con pieno supporto Undo/Redo tramite `UndoManager` |
| **Strategy** | Comportamentale | `PlaybackStrategy`, `SequentialStrategy`, `ShuffleStrategy`, `LoopStrategy`, `OrdinamentoStrategy`, `OrdinaTitoloAsc/Desc`, `OrdinaAutoreAsc/Desc`, `OrdinaAnnoAsc/Desc`, `OrdinaGenereAsc/Desc`, `OrdinaTagAsc/Desc` | Selezione a runtime dell'algoritmo di riproduzione e dell'ordinamento della libreria |
| **State** | Comportamentale | `PlayerState`, `PlayingState`, `PausedState`, `StoppedState`, `StatoUI`, `StatoLibreria`, `StatoPlaylist`, `MenuContestuale` | Gestione degli stati del player musicale (Playing/Paused/Stopped) e personalizzazione del menu contestuale in base alla vista corrente |

---

## 📁 Struttura del Progetto

```
ProgettoSAD-G16/
├── pom.xml                         # Configurazione Maven
├── README.md
├── Documents/                      # Documentazione di progetto
│   ├── DiagrammaClassiGruppo16.pdf
│   └── Documento_Architettura_Libreria_Musicale_260528_102616.pdf
├── Libreria/                       # Dati persistenti dell'applicazione
│   ├── metadata.csv                #   Metadati dei brani (titolo, autore, anno, durata, genere, tag, playCount)
│   ├── lista_playlist.csv          #   Elenco delle playlist salvate
│   └── Playlist - <nome>/         #   Cartelle contenenti i brani associati a ciascuna playlist
└── src/
    ├── main/
    │   ├── java/com/musicplayer/
    │   │   ├── MainApp.java            # Entry point dell'applicazione JavaFX
    │   │   ├── PathUtils.java          # Utility per la gestione dei percorsi file
    │   │   ├── adapter/                # 🔌 Pattern Adapter
    │   │   │   ├── Mp3LibFile.java     #   Interfaccia libreria esterna MP3
    │   │   │   └── Mp3Adapter.java     #   Adattatore → Playable
    │   │   ├── command/                # 📋 Pattern Command + Undo/Redo
    │   │   │   ├── Command.java        #   Interfaccia Command (esegui/annulla)
    │   │   │   ├── UndoManager.java    #   Gestore storico comandi
    │   │   │   ├── AggiungiALibreriaCmd.java
    │   │   │   ├── RimuoviDaLibreriaCmd.java
    │   │   │   ├── AggiungiAPlaylistCmd.java
    │   │   │   ├── RimuoviDaPlaylistCmd.java
    │   │   │   ├── AggiungiMassivoCmd.java
    │   │   │   ├── RimuoviMassivoCmd.java
    │   │   │   ├── RimuoviMassivoLibreriaCmd.java
    │   │   │   ├── CreaPlaylistCmd.java
    │   │   │   ├── EliminaPlaylistCmd.java
    │   │   │   └── RinominaPlaylistCmd.java
    │   │   ├── controller/             # 🎮 Controller (logica applicativa)
    │   │   │   ├── LibreriaController.java      # Controller principale MVC
    │   │   │   ├── GestoreRiproduzione.java     # Gestione player + coda musicale
    │   │   │   └── RiproduzioneException.java
    │   │   ├── model/                  # 📦 Modello di dominio
    │   │   │   ├── IBrano.java         #   Interfaccia comune (Proxy pattern)
    │   │   │   ├── Brano.java          #   Entità brano musicale
    │   │   │   ├── BranoProxy.java     #   Virtual Proxy (lazy loading metadati)
    │   │   │   ├── BranoFactory.java   #   Factory per la creazione dei brani
    │   │   │   ├── BranoCreator.java   #   Creator astratto (Factory Method)
    │   │   │   ├── Playable.java       #   Interfaccia per elementi riproducibili
    │   │   │   ├── Libreria.java       #   Collezione principale dei brani
    │   │   │   ├── ICatalogo.java      #   Interfaccia catalogo (astrazione)
    │   │   │   ├── Playlist.java       #   Playlist standard
    │   │   │   ├── SmartPlaylist.java  #   Playlist dinamica con filtro automatico
    │   │   │   ├── FiltroRicerca.java  #   Filtri avanzati di ricerca
    │   │   │   ├── Genere.java         #   Enum generi musicali
    │   │   │   ├── Tag.java            #   Enum tag personalizzati
    │   │   │   ├── CampoOrdinamento.java        #   Enum campi di ordinamento
    │   │   │   ├── StatisticheAscolto.java      #   Classifica ascolti (Observer)
    │   │   │   ├── LibreriaObserver.java        #   Observer per eventi libreria
    │   │   │   ├── RiproduzioneObserver.java    #   Observer per eventi riproduzione
    │   │   │   ├── PlaylistIterator.java        #   Interfaccia Iterator
    │   │   │   ├── SequentialIterator.java      #   Iteratore sequenziale
    │   │   │   ├── ShuffleIterator.java         #   Iteratore casuale
    │   │   │   ├── LoopIterator.java            #   Iteratore ciclico
    │   │   │   ├── PlaylistException.java
    │   │   │   └── ValidazioneException.java
    │   │   ├── persistence/            # 💾 Persistenza dati (CSV)
    │   │   │   ├── MetadataService.java         # Lettura/scrittura metadata.csv
    │   │   │   ├── SongMetadata.java            # DTO per serializzazione brani
    │   │   │   └── PersistenzaException.java
    │   │   ├── state/                  # 🔄 Pattern State
    │   │   │   ├── PlayerState.java    #   Interfaccia stato player
    │   │   │   ├── PlayingState.java   #   Stato: in riproduzione
    │   │   │   ├── PausedState.java    #   Stato: in pausa
    │   │   │   ├── StoppedState.java   #   Stato: fermato
    │   │   │   ├── StatoUI.java        #   Interfaccia stato interfaccia
    │   │   │   ├── StatoLibreria.java  #   Stato UI: vista libreria
    │   │   │   ├── StatoPlaylist.java  #   Stato UI: vista playlist
    │   │   │   └── MenuContestuale.java #  Menu contestuale state-aware
    │   │   ├── strategy/               # ♟️ Pattern Strategy
    │   │   │   ├── PlaybackStrategy.java        # Strategia di riproduzione
    │   │   │   ├── SequentialStrategy.java
    │   │   │   ├── ShuffleStrategy.java
    │   │   │   ├── LoopStrategy.java
    │   │   │   ├── OrdinamentoStrategy.java     # Strategia di ordinamento
    │   │   │   ├── OrdinaTitoloAsc.java / Desc
    │   │   │   ├── OrdinaAutoreAsc.java / Desc
    │   │   │   ├── OrdinaAnnoAsc.java / Desc
    │   │   │   ├── OrdinaGenereAsc.java / Desc
    │   │   │   └── OrdinaTagAsc.java / Desc
    │   │   └── view/                   # 🖥️ Vista (JavaFX)
    │   │       ├── LibreriaView.java   #   Vista principale della libreria
    │   │       ├── PlaylistView.java   #   Vista gestione playlist
    │   │       ├── PlayerView.java     #   Vista controlli player
    │   │       └── FormBranoView.java  #   Form di inserimento/modifica brano
    │   └── resources/com/musicplayer/
    │       └── MusicPlayer.fxml        # Layout FXML dell'interfaccia
    └── test/java/com/musicplayer/     # 🧪 Test unitari
        ├── AggiungiBranoTest.java
        ├── BranoModificaTest.java
        ├── BranoProxyTest.java
        ├── BranoTest.java
        ├── ClassificaAscoltiTest.java
        ├── ComandiMassiviTest.java
        ├── CreaPlaylistTest.java
        ├── EliminaPlaylistTest.java
        ├── EliminaTest.java
        ├── FeaturesTest.java
        ├── FiltroRicercaTest.java
        ├── GestioneBraniRiproduzioneTest.java
        ├── LibreriaTest.java
        ├── MenuContestualeTest.java
        ├── Mp3AdapterTest.java
        ├── OperazioniCollezioniTest.java
        ├── PlaybackStrategyTest.java
        ├── RimuoviDaPlaylistTest.java
        ├── RinominaPlaylistTest.java
        ├── ShuffleIteratorTest.java
        ├── SpostaBranoTest.java
        ├── StatoRiproduzioneTest.java
        ├── UndoManagerTest.java
        └── strategy/
            └── OrdinaBraniTest.java
```

---

## 💾 Persistenza dei Dati

L'applicazione utilizza file **CSV** per la persistenza, gestiti dalla classe `MetadataService`:

| File | Contenuto |
|---|---|
| `Libreria/metadata.csv` | Metadati completi di ogni brano: filename, titolo, autore, anno, durata, genere, tag, playCount |
| `Libreria/lista_playlist.csv` | Elenco delle playlist create dall'utente |
| `Libreria/Playlist - <nome>/` | Cartella contenente i riferimenti ai brani di ciascuna playlist |

Il **`BranoProxy`** implementa il caricamento lazy: i metadati dal CSV vengono letti solo quando un brano viene effettivamente visualizzato o riprodotto, migliorando le performance all'avvio con librerie di grandi dimensioni.

---

## 🚀 Come Eseguire il Progetto

### Requisiti

- **JDK 17** (o versione superiore) correttamente configurato nel `PATH`
- **Apache Maven** installato e configurato

### Compilazione e Avvio

1. Posizionati nella directory principale del progetto (dove si trova il file `pom.xml`):

2. Compila il progetto ed esegui i test:
   ```bash
   mvn clean install
   ```

3. Avvia l'applicazione tramite il plugin JavaFX:
   ```bash
   mvn javafx:run
   ```

---

## 🧪 Esecuzione dei Test

Il progetto include **24 classi di test** sviluppate con JUnit 5 che coprono tutte le aree principali della business logic.

```bash
mvn test
```

### Aree Coperte dai Test

| Area | Classi di Test |
|---|---|
| **Modello (Brano)** | `BranoTest`, `BranoModificaTest`, `BranoProxyTest` |
| **Libreria** | `LibreriaTest`, `AggiungiBranoTest`, `EliminaTest`, `OperazioniCollezioniTest` |
| **Playlist** | `CreaPlaylistTest`, `EliminaPlaylistTest`, `RinominaPlaylistTest`, `RimuoviDaPlaylistTest`, `SpostaBranoTest` |
| **Ricerca e Filtri** | `FiltroRicercaTest` |
| **Riproduzione** | `GestioneBraniRiproduzioneTest`, `StatoRiproduzioneTest`, `PlaybackStrategyTest` |
| **Iterator** | `ShuffleIteratorTest` |
| **Command & Undo** | `UndoManagerTest`, `ComandiMassiviTest` |
| **Strategy (Ordinamento)** | `OrdinaBraniTest` |
| **Adapter** | `Mp3AdapterTest` |
| **Statistiche** | `ClassificaAscoltiTest` |
| **UI State** | `MenuContestualeTest` |
| **Funzionalità Integrate** | `FeaturesTest` |

---

## 📄 Documentazione

Nella cartella `Documents/` sono disponibili i seguenti documenti di progetto:

- **Diagramma delle Classi** — `DiagrammaClassiGruppo16.pdf`  
  Diagramma UML completo delle classi del sistema e delle relazioni tra i componenti.

- **Documento di Architettura** — `Documento_Architettura_Libreria_Musicale_260528_102616.pdf`  
  Documento architetturale che descrive le scelte progettuali, i pattern adottati e la struttura complessiva del software.

---

## 👥 Autori (Gruppo 16)

- **Simone Donato**
- **Alessio Ferrara**
- **Manuel De Vivo**
- **Aniello De Girolamo Del Mauro**