# ProgettoSAD-G16 - Sonic Wave

**Sonic Wave** è un'applicazione desktop sviluppata in **Java 17** con **JavaFX** per la gestione e la riproduzione di una libreria musicale locale (file MP3). Il progetto applica i principi dell'Ingegneria del Software e i Design Pattern per garantire una struttura modulare, manutenibile e scalabile.

## 🎵 Funzionalità Principali

- **Gestione Libreria:** Aggiunta, rimozione e organizzazione di brani MP3.
- **Playlist e Smart Playlist:** Creazione di playlist standard e "Smart Playlist" automatiche, che si popolano e si aggiornano in base a filtri specifici.
- **Modalità di Riproduzione:** Gestione della coda musicale con supporto per diverse modalità di ascolto (Sequenziale, Casuale/Shuffle, Loop continuo).
- **Ricerca e Filtraggio:** Ricerca avanzata nella libreria tramite testo o filtri specifici (`FiltroRicerca`), come genere, tag o artista.
- **Gestione Tag e Metadati:** Classificazione flessibile dei brani musicali tramite generi predefiniti e tag personalizzati.
- **Persistenza Dati:** Salvataggio permanente delle informazioni (metadati dei brani, configurazioni delle playlist) per mantenere lo stato tra le sessioni.

## 🛠️ Tecnologie Utilizzate

- **Linguaggio:** Java 17
- **Interfaccia Grafica (GUI):** JavaFX 17 (con file FXML per la definizione del layout)
- **Strumento di Build:** Maven
- **Testing:** JUnit 5.10.0

## 📐 Architettura e Design Pattern (GoF)

L'architettura del software si basa sul pattern **MVC** (Model-View-Controller), mantenendo separata la logica di dominio dall'interfaccia utente.
Per soddisfare requisiti specifici di progettazione, sono stati implementati i seguenti design pattern:

- **Factory Method:** Per la creazione strutturata degli oggetti musicali (es. `BranoFactory`, `BranoCreator`).
- **Observer:** Per mantenere la View sempre sincronizzata con i cambiamenti di stato nel Model (es. `LibreriaObserver`, `RiproduzioneObserver`).
- **Iterator:** Per gestire in modo trasparente l'attraversamento delle playlist secondo diverse strategie di riproduzione (`SequentialIterator`, `ShuffleIterator`, `LoopIterator`).
- **Command:** Utilizzato per incapsulare le richieste come oggetti, agevolando l'esecuzione di operazioni sulla libreria e ponendo le basi per funzonalità di undo/redo.
- **Strategy & State:** Impiegati per la gestione flessibile degli algoritmi di ricerca/filtraggio e per gestire gli stati interni del player musicale.

## 🚀 Come Eseguire il Progetto

### Requisiti
- **JDK 17** (o versione superiore) correttamente configurato nel PATH.
- **Apache Maven** installato e configurato.

### Compilazione e Avvio
1. Posizionati nella directory principale del progetto (dove si trova il file `pom.xml`).
2. Per compilare il progetto ed eseguire i test, lancia il comando:
   ```bash
   mvn clean install
   ```
3. Per avviare l'applicazione tramite il plugin JavaFX:
   ```bash
   mvn javafx:run
   ```

## 🧪 Esecuzione dei Test

Il progetto include test di unità sviluppati con JUnit 5 per validare la business logic. Puoi eseguirli in isolamento con il comando:
```bash
mvn test
```

## 👥 Autori (Gruppo 16)

- Simone Donato
- Alessio Ferrara
- Manuel De Vivo
- Aniello De Girolamo Del Mauro