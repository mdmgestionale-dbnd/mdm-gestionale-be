# Manuale utente - Gestionale elettricisti

Questo documento spiega in modo pratico come usare il gestionale aziendale.
È pensato per essere letto da persone non tecniche: amministratori, supervisori e dipendenti.

L'applicazione serve a gestire clienti, cantieri, task di lavoro, squadre, veicoli, materiali, allegati, ferie/malattie, notifiche e alcuni strumenti utili come report ore, unione PDF e creazione preventivi.

## Indice

- [1. A cosa serve il gestionale](#1-a-cosa-serve-il-gestionale)
- [2. Ruoli utente](#2-ruoli-utente)
- [3. Accesso all'applicazione](#3-accesso-allapplicazione)
- [4. Menu principale](#4-menu-principale)
- [Manuale amministratore e supervisore](#manuale-amministratore-e-supervisore)
- [5. Calendario e task](#5-calendario-e-task)
- [6. Creare un nuovo task](#6-creare-un-nuovo-task)
- [7. Dettaglio task](#7-dettaglio-task)
- [8. Modificare o eliminare un task](#8-modificare-o-eliminare-un-task)
- [9. Clienti e cantieri](#9-clienti-e-cantieri)
- [10. Allegati cantiere](#10-allegati-cantiere)
- [11. Magazzino e inventario](#11-magazzino-e-inventario)
- [12. Veicoli](#12-veicoli)
- [13. Ferie e malattie](#13-ferie-e-malattie)
- [14. Notifiche](#14-notifiche)
- [15. Utenze](#15-utenze)
- [16. Impostazioni](#16-impostazioni)
- [17. Utilità](#17-utilità)
- [Manuale dipendente](#manuale-dipendente)
- [18. Vedere il proprio lavoro](#18-vedere-il-proprio-lavoro)
- [19. Registrare materiale usato](#19-registrare-materiale-usato)
- [20. Caricare allegati dal cantiere](#20-caricare-allegati-dal-cantiere)
- [21. Richiedere ferie o malattia](#21-richiedere-ferie-o-malattia)
- [22. Consultare notifiche](#22-consultare-notifiche)
- [23. Consultare cantieri, veicoli e magazzino](#23-consultare-cantieri-veicoli-e-magazzino)
- [24. Cancellazione logica e ripristino](#24-cancellazione-logica-e-ripristino)
- [25. Disponibilità automatica](#25-disponibilità-automatica)
- [26. Aggiornamento in tempo reale](#26-aggiornamento-in-tempo-reale)
- [27. Buone pratiche operative](#27-buone-pratiche-operative)
- [28. Riepilogo rapido per ruolo](#28-riepilogo-rapido-per-ruolo)
- [29. Glossario](#29-glossario)
- [30. Note finali](#30-note-finali)

---

## 1. A cosa serve il gestionale

Il gestionale permette di:

- registrare i clienti e i loro cantieri;
- pianificare i lavori in calendario;
- assegnare dipendenti e veicoli ai task;
- controllare automaticamente se persone o mezzi sono già occupati;
- registrare materiali usati e scaricarli dal magazzino;
- caricare allegati sui cantieri;
- gestire scadenze dei veicoli;
- ricevere notifiche operative;
- richiedere e approvare ferie o malattie;
- generare report ore in Excel;
- creare preventivi in PDF;
- unire più PDF in un solo documento;
- recuperare elementi cancellati logicamente finché non vengono eliminati definitivamente.

L'app è pensata per essere usata sia da computer sia da smartphone. I dipendenti la useranno soprattutto da telefono, mentre amministratori e supervisori possono usarla comodamente anche da desktop.

---

## 2. Ruoli utente

L'applicazione prevede tre ruoli principali.

### Amministratore

L'amministratore ha accesso completo al gestionale.
Può creare e modificare clienti, cantieri, task, veicoli, magazzini, articoli, utenze, impostazioni e può usare tutte le funzioni di utilità.

Può anche:

- vedere la griglia completa del calendario;
- creare nuovi task;
- modificare o cancellare task;
- assegnare dipendenti e veicoli;
- approvare o rifiutare richieste di ferie/malattia;
- vedere notifiche operative e scadenze veicoli;
- gestire la pulizia definitiva dei dati eliminati.

### Supervisore

Il supervisore ha una vista ampia sull'operatività, ma con meno permessi dell'amministratore.
Può vedere le assegnazioni proprie e degli altri dipendenti, gestire alcune anagrafiche operative e approvare richieste se previsto dai permessi applicativi.

In generale il supervisore può:

- consultare calendario e riepiloghi;
- vedere task dei dipendenti;
- consultare clienti, cantieri, magazzino e veicoli;
- registrare materiali su assegnazioni;
- caricare allegati sui cantieri;
- usare alcune utilità come report ore, PDF e preventivi.

### Dipendente

Il dipendente vede soprattutto ciò che gli serve per lavorare.
Può:

- vedere il proprio task corrente o il prossimo task assegnato;
- consultare le informazioni del cantiere;
- vedere squadra, veicoli, materiali e note operative;
- registrare materiale usato sul task;
- caricare allegati relativi al cantiere;
- richiedere ferie o malattia;
- vedere l'esito delle proprie richieste;
- consultare alcune anagrafiche in sola lettura.

Il dipendente non gestisce la pianificazione generale e non può modificare task di altri.

---

## 3. Accesso all'applicazione

Per entrare nell'applicazione bisogna aprire la pagina di login e inserire username e password.

Dopo l'accesso, il menu laterale mostra le funzioni disponibili in base al ruolo dell'utente.
Se una pagina non è disponibile per il proprio ruolo, l'app mostra una schermata di accesso negato.

---

## 4. Menu principale

Il menu dell'applicazione contiene queste aree principali:

- **Calendario**: pianificazione task e riepilogo assegnazioni.
- **Notifiche**: avvisi operativi, richieste e scadenze.
- **Gestione Cantieri**: clienti, cantieri e allegati.
- **Gestione Inventario**: magazzini, articoli e movimenti.
- **Gestione Veicoli**: mezzi aziendali e relative scadenze.
- **Gestione Ferie**: richieste ferie/malattia e approvazioni.
- **Gestione Utenze**: utenti, ruoli e stato account.
- **Impostazioni**: dati aziendali e pulizia dati.
- **Utilità**: report ore, unione PDF e preventivi.

Alcune voci sono visibili o utilizzabili solo da amministratori o supervisori.

---

# Manuale amministratore e supervisore

Questa sezione descrive le funzioni operative principali per chi gestisce il lavoro dell'azienda.

---

## 5. Calendario e task

La pagina **Calendario** è il centro operativo della pianificazione.

### Vista amministratore

L'amministratore vede una griglia calendario con:

- giorni sull'asse orizzontale;
- orari sull'asse verticale;
- task visualizzati come blocchi nel calendario.

Su desktop viene mostrata la settimana. Su smartphone in verticale vengono mostrati tre giorni alla volta, così la griglia rimane leggibile.

Se più task sono sovrapposti nello stesso giorno e nella stessa fascia oraria, vengono mostrati affiancati. Se diventano troppo stretti, il testo viene nascosto ma il blocco resta cliccabile: le informazioni si possono vedere con hover o aprendo il dettaglio.

### Navigare nel calendario

In alto nella griglia sono presenti i pulsanti per andare avanti o indietro:

- su desktop: settimana precedente / settimana successiva;
- su mobile verticale: tre giorni prima / tre giorni dopo.

### Filtrare per dipendente

Nella vista amministratore è presente il filtro **Filtro griglia**.
Permette di visualizzare:

- tutti i task;
- solo i task assegnati a uno specifico dipendente.

Questo è utile quando ci sono molte squadre o molte attività nello stesso periodo.

### Vista "Le mie assegnazioni"

L'amministratore può cliccare **Le mie assegnazioni** per vedere una vista più simile a quella del dipendente.
Questa vista mostra i task personali o, se si seleziona un altro utente, quelli del dipendente scelto.

È utile quando l'amministratore vuole sapere rapidamente dove deve andare o cosa è previsto per una persona specifica, senza guardare tutta la griglia.

---

## 6. Creare un nuovo task

Un task rappresenta un'attività pianificata su un cantiere.

Per creare un task:

1. Vai su **Calendario**.
2. Clicca **Nuovo task** oppure clicca su uno slot libero della griglia.
3. Segui il wizard guidato.

Il wizard è diviso in tre passaggi.

### Passaggio 1: Quando e dove

Qui si scelgono:

- data e ora di inizio;
- data e ora di fine;
- durata rapida, ad esempio 2h, 4h, 6h, 8h;
- cliente;
- cantiere.

Il cantiere si può scegliere solo dopo aver selezionato il cliente.

### Passaggio 2: Squadra e veicoli

Qui si scelgono:

- dipendenti da assegnare;
- veicoli da usare.

Il sistema controlla automaticamente la disponibilità.
Se una persona o un veicolo è già occupato, oppure il dipendente è assente per ferie/malattia approvata, viene indicato come non disponibile.

### Passaggio 3: Materiali e note

Qui si possono inserire:

- note operative per la squadra;
- materiali da usare o portare.

Per ogni materiale si seleziona prima il magazzino, poi l'articolo di quel magazzino, poi la quantità.

Quando il task viene salvato, i materiali inseriti vengono registrati come scarico di magazzino.

---

## 7. Dettaglio task

Cliccando su un task si apre il dettaglio.

Nel dettaglio si vedono:

- orario di inizio e fine;
- squadra assegnata;
- veicoli assegnati;
- note operative;
- materiali usati;
- allegati del cantiere.

Gli allegati non sono mostrati subito: bisogna cliccare **Mostra** nella sezione allegati. Questo evita di appesantire la schermata quando un cantiere ha molti file.

Da dettaglio task l'amministratore può anche:

- modificare il task;
- eliminarlo;
- registrare altri materiali usati;
- caricare allegati sul cantiere.

---

## 8. Modificare o eliminare un task

Dal dettaglio task è possibile cliccare **Modifica** per cambiare pianificazione, squadra, veicoli o note.

Se si elimina un task:

- il task viene cancellato logicamente;
- i movimenti di magazzino collegati vengono annullati;
- le quantità dei materiali tornano disponibili in magazzino;
- il task può essere ripristinato dalla sezione **Task eliminati**.

Nella sezione **Task eliminati** vengono mostrati cantiere, data/ora di inizio, data/ora di fine e squadra, così si capisce quale task si sta ripristinando.

---

## 9. Clienti e cantieri

La pagina **Gestione Cantieri** permette di gestire l'anagrafica essenziale dei clienti e dei cantieri.

### Cliente

Per ogni cliente vengono gestiti:

- nome;
- telefono.

Dalla pagina si può:

- cercare un cliente per nome o telefono;
- creare un nuovo cliente;
- modificare un cliente;
- eliminare logicamente un cliente.

Se si elimina un cliente, vengono eliminati logicamente anche i suoi cantieri e i task collegati.

### Cantiere

Ogni cantiere appartiene a un cliente.
L'informazione principale del cantiere è il nome, normalmente usato come via/indirizzo operativo.

Per vedere i cantieri bisogna prima selezionare un cliente.
Da lì si può:

- creare un nuovo cantiere;
- modificare il nome/via del cantiere;
- eliminare logicamente il cantiere;
- aprire gli allegati del cantiere.

Se si elimina un cantiere, vengono eliminati logicamente anche i task collegati.

### Elementi eliminati

La pagina contiene una sezione **Elementi eliminati**.
Da qui si possono ripristinare clienti e cantieri cancellati logicamente.

Il ripristino è possibile finché i dati non vengono eliminati definitivamente dalla pulizia dati.

---

## 10. Allegati cantiere

Gli allegati sono sempre collegati al cantiere, non al singolo task.
Questo significa che se un cantiere ha più task, gli allegati restano condivisi sul cantiere.

Gli allegati si possono aprire da:

- pagina cantieri;
- dettaglio task;
- riepilogo assegnazione del dipendente.

### File ammessi

Sono ammessi solo questi formati:

- PDF;
- JPG;
- JPEG;
- PNG.

Limiti:

- massimo 2MB in upload;
- massimo 1MB salvato nello storage.

Le immagini vengono compresse automaticamente. Anche il backend controlla tipo e dimensione, quindi non è possibile caricare file non consentiti bypassando il frontend.

### Cosa si può fare sugli allegati

Nel componente allegati si può:

- vedere gli ultimi file caricati;
- cercare per nome file;
- filtrare per intervallo date;
- scaricare un file;
- eliminare un file, se si hanno i permessi.

---

## 11. Magazzino e inventario

La pagina **Gestione Inventario** serve a gestire depositi, articoli e movimenti di magazzino.

### Magazzini

Un magazzino rappresenta un deposito o una categoria fisica/logica di articoli.

Dalla pagina si può:

- selezionare il magazzino da consultare;
- creare un nuovo magazzino;
- modificare un magazzino;
- eliminare un magazzino, se consentito.

### Articoli

Gli articoli sono collegati a un magazzino.
Per ogni articolo vengono gestiti:

- nome;
- prezzo unitario;
- quantità disponibile.

La pagina mostra anche:

- numero articoli nel magazzino selezionato;
- valore stimato del magazzino;
- ricerca testuale sugli articoli.

### Movimenti

I movimenti servono ad aggiungere o togliere quantità dal magazzino.

Quando si registra un movimento, si sceglie:

- articolo;
- tipo movimento: carico o scarico;
- quantità;
- eventuale descrizione.

La quantità viene inserita sempre positiva. Il sistema decide se aggiungerla o scalarla in base alla scelta carico/scarico.

I materiali usati nei task generano automaticamente movimenti di scarico.

---

## 12. Veicoli

La pagina **Gestione Veicoli** permette di gestire i mezzi aziendali.

Per ogni veicolo si possono inserire:

- targa;
- marca;
- modello;
- scadenza assicurazione;
- scadenza revisione;
- scadenza bollo.

La pagina evidenzia le scadenze:

- verde se non sono vicine;
- giallo se sono in scadenza;
- rosso se sono scadute.

### Rinnovo scadenze

Con il pulsante **Rinnova** si può aggiornare una scadenza.

Si sceglie:

- cosa rinnovare: assicurazione, revisione o bollo;
- data rinnovo;
- durata: semestrale o annuale.

Il sistema calcola la nuova scadenza e aggiorna il veicolo.

Quando una scadenza viene rinnovata, la relativa notifica scompare automaticamente dal centro notifiche.

### Eliminazione e ripristino

Se si elimina un veicolo:

- il veicolo viene eliminato logicamente;
- i task collegati vengono eliminati logicamente;
- il veicolo può essere ripristinato dalla sezione **Veicoli eliminati**.

---

## 13. Ferie e malattie

La pagina **Gestione Ferie** permette di richiedere e approvare assenze.

Sono gestiti due tipi di richiesta:

- ferie;
- malattia.

I permessi a ore non sono gestiti.

### Dipendente

Il dipendente può inviare una richiesta indicando:

- tipo richiesta;
- data inizio;
- data fine;
- eventuali note.

La richiesta resta in attesa finché un amministratore o supervisore la approva o la rifiuta.

### Amministratore e supervisore

Gli utenti abilitati vedono:

- richieste da approvare;
- le proprie richieste;
- storico delle richieste concluse.

Per ogni richiesta in attesa possono:

- approvare;
- rifiutare;
- inserire una nota decisione.

Quando una richiesta viene approvata, il dipendente non risulta disponibile per i task in quel periodo. Se era già assegnato a un task sovrapposto, viene rimosso da quel task.

---

## 14. Notifiche

La pagina **Notifiche** mostra gli avvisi rilevanti.

Le notifiche possono riguardare:

- richieste ferie/malattia da gestire;
- esiti di richieste ferie/malattia;
- scadenze veicoli;
- altre comunicazioni operative.

### Come funzionano le notifiche

Le notifiche non sono uguali per tutti:

- l'amministratore vede le notifiche operative generali, come richieste da approvare e scadenze veicoli;
- il dipendente vede notifiche personali, ad esempio esito di una richiesta;
- il supervisore vede le notifiche previste dal suo ruolo.

### Rimuovere notifiche

Per le notifiche normali è possibile usare **Rimuovi** o **Elimina**.

Per le notifiche di scadenza veicolo, invece, non bisogna eliminarle manualmente: spariscono quando la scadenza viene aggiornata nel veicolo.

Il pulsante **Elimina tutto** rimuove le notifiche operative normali, ma non rimuove le scadenze veicoli ancora valide.

---

## 15. Utenze

La pagina **Gestione Utenze** serve a gestire gli utenti dell'applicazione.

Da qui si può:

- creare un nuovo utente;
- modificare un utente esistente;
- assegnare un ruolo;
- attivare o disattivare l'utente;
- eliminare logicamente un utente;
- ripristinare utenze cancellate.

I ruoli disponibili sono:

- amministratore;
- supervisore;
- dipendente.

L'utente tecnico `superadmin` non compare nei flussi di lavoro e non deve essere usato per attività operative.

---

## 16. Impostazioni

La pagina **Impostazioni** è riservata all'amministratore.

Contiene:

- dati aziendali;
- strumenti di pulizia database/storage.

### Dati aziendali

I dati aziendali usati dall'app includono:

- nome azienda;
- indirizzo azienda;
- partita IVA.

Queste informazioni vengono usate anche nella creazione dei preventivi.

### Pulizia database e storage

La sezione **Pulizia database e storage** serve a eliminare definitivamente dati vecchi o già cancellati logicamente.

Sono disponibili preset semplici:

- eliminare definitivamente elementi già cestinati;
- pulire notifiche lette vecchie;
- pulire allegati vecchi già eliminati.

La pulizia definitiva è un'operazione delicata: dopo l'esecuzione i dati rimossi non sono più ripristinabili dall'applicazione.

---

## 17. Utilità

La pagina **Utilità** contiene strumenti pratici non legati direttamente alla pianificazione giornaliera.

### Report ore

Il report ore permette di generare un file Excel con le ore lavorate.

Si sceglie un intervallo date. Di default viene proposto il mese corrente.

Il file Excel contiene:

- un foglio di riepilogo;
- un foglio per ogni dipendente;
- giorni del periodo in colonna;
- cantieri in riga;
- ore lavorate per dipendente, giorno e cantiere;
- totali ferie e malattia.

È possibile anche visualizzare un'anteprima tabellare prima del download.

### Unisci PDF

Questa utilità permette di selezionare più PDF e unirli in un unico file.

L'ordine mostrato nella pagina è l'ordine finale delle pagine nel PDF generato.

### Crea preventivo

Questa utilità permette di creare un preventivo PDF.

Si seleziona:

- cliente;
- data automatica;
- oggetto del preventivo;
- percentuale IVA;
- righe con descrizione, quantità e prezzo.

Il sistema calcola automaticamente:

- imponibile;
- IVA;
- totale.

Nel PDF vengono inseriti anche logo e dati aziendali configurati nelle impostazioni.

---

# Manuale dipendente

Questa sezione riassume le funzioni principali per chi usa l'app dal telefono durante il lavoro.

---

## 18. Vedere il proprio lavoro

Il dipendente entra nella pagina **Calendario** ma non vede la griglia completa dell'amministratore.

Vede invece il riepilogo del proprio task corrente o del prossimo task assegnato.

Nel riepilogo trova:

- cantiere;
- data e ora;
- squadra;
- veicoli;
- note operative;
- materiali previsti o già usati;
- allegati del cantiere.

Se non ci sono task correnti o futuri, viene mostrato un messaggio informativo.

---

## 19. Registrare materiale usato

Dal proprio riepilogo task, il dipendente può registrare materiale usato.

Per farlo:

1. clicca **Registra materiale usato**;
2. seleziona il magazzino;
3. seleziona il prodotto;
4. inserisce la quantità;
5. conferma.

Il materiale viene scaricato dal magazzino.

---

## 20. Caricare allegati dal cantiere

Dal riepilogo task, il dipendente può aprire la sezione allegati.

Gli allegati caricati vengono collegati al cantiere, non solo al task.
Questo permette ad amministratori, supervisori e altri operatori autorizzati di vedere tutta la documentazione dello stesso cantiere.

File ammessi:

- PDF;
- JPG;
- JPEG;
- PNG.

Limiti:

- massimo 2MB in upload;
- massimo 1MB salvato dopo compressione.

---

## 21. Richiedere ferie o malattia

Il dipendente può andare su **Gestione Ferie** e cliccare **Nuova richiesta**.

Deve indicare:

- ferie o malattia;
- data inizio;
- data fine;
- eventuali note.

Dopo l'invio, la richiesta resta in attesa.
Quando viene approvata o rifiutata, il dipendente riceve una notifica.

Se la richiesta viene approvata, il dipendente non potrà essere assegnato a task sovrapposti a quel periodo.

---

## 22. Consultare notifiche

La pagina **Notifiche** mostra solo gli avvisi utili all'utente.

Per un dipendente, ad esempio, può comparire l'esito di una richiesta ferie o malattia.

Le notifiche personali possono essere rimosse quando sono state lette.

---

## 23. Consultare cantieri, veicoli e magazzino

In base ai permessi, il dipendente può consultare alcune pagine in sola lettura:

- cantieri;
- veicoli;
- inventario.

Queste pagine servono soprattutto per consultazione e non per gestione amministrativa.

---

# Concetti importanti

---

## 24. Cancellazione logica e ripristino

Molte eliminazioni nell'app non cancellano subito definitivamente i dati.

Quando si elimina un cliente, cantiere, veicolo, task o utente, l'elemento viene messo in uno stato cancellato logicamente.
Questo permette di ripristinarlo in caso di errore.

Solo la pulizia definitiva nelle impostazioni rimuove i dati in modo permanente.

In caso di eliminazione a cascata:

- eliminare un cliente elimina logicamente anche cantieri e task collegati;
- eliminare un cantiere elimina logicamente anche i task collegati;
- eliminare un veicolo elimina logicamente i task collegati;
- ripristinare prova a riportare attivi anche gli elementi collegati, quando possibile.

---

## 25. Disponibilità automatica

Quando si crea o modifica un task, il sistema controlla automaticamente se dipendenti e veicoli sono disponibili.

Una persona non è disponibile se:

- è già assegnata a un altro task nello stesso periodo;
- ha ferie o malattia approvata nello stesso periodo.

Un veicolo non è disponibile se:

- è già assegnato a un altro task nello stesso periodo.

---

## 26. Aggiornamento in tempo reale

L'applicazione usa un sistema di aggiornamento in tempo reale.

Quando un utente modifica dati importanti, gli altri utenti collegati possono ricevere aggiornamenti e ricaricare automaticamente le informazioni della pagina.

Questo aiuta a mantenere allineate le viste tra ufficio e personale operativo.

---

## 27. Buone pratiche operative

Per usare bene il gestionale:

- creare sempre prima cliente e cantiere;
- pianificare i task dal calendario;
- usare note operative chiare per la squadra;
- registrare i materiali usati appena possibile;
- caricare allegati direttamente sul cantiere;
- aggiornare le scadenze veicoli appena rinnovate;
- controllare spesso le notifiche;
- usare la cancellazione logica con attenzione;
- eseguire la pulizia definitiva solo quando si è sicuri.

---

## 28. Riepilogo rapido per ruolo

### Amministratore

Usa principalmente:

- Calendario;
- Notifiche;
- Clienti e cantieri;
- Inventario;
- Veicoli;
- Ferie;
- Utenze;
- Impostazioni;
- Utilità.

### Supervisore

Usa principalmente:

- Calendario e riepiloghi dipendenti;
- Notifiche;
- Cantieri;
- Inventario;
- Veicoli;
- Ferie;
- Utilità.

### Dipendente

Usa principalmente:

- Calendario per vedere il task corrente o prossimo;
- Allegati e materiali dal task;
- Ferie/malattia;
- Notifiche;
- consultazione dati operativi.

---

## 29. Glossario

**Task**  
Attività pianificata su un cantiere in un determinato intervallo di tempo.

**Cantiere**  
Luogo o intervento associato a un cliente. Gli allegati sono collegati al cantiere.

**Squadra**  
Insieme di dipendenti assegnati a un task.

**Movimento di magazzino**  
Carico o scarico di quantità da un articolo di magazzino.

**Cancellazione logica**  
Eliminazione reversibile. Il dato non si vede più nelle liste principali ma può essere ripristinato.

**Pulizia definitiva**  
Eliminazione permanente dei dati, non ripristinabile dall'app.

**Notifica veicolo**  
Avviso legato a bollo, revisione o assicurazione in scadenza/scaduta. Scompare quando la scadenza viene aggiornata.

---

## 30. Note finali

Il gestionale è pensato per accompagnare il lavoro quotidiano dell'azienda: ufficio, supervisori e dipendenti devono trovare rapidamente le informazioni utili senza dover ricostruire a mano cosa è successo.

La regola pratica è semplice:

- l'amministratore pianifica e controlla;
- il supervisore monitora e coordina;
- il dipendente vede cosa deve fare, registra materiali e carica documenti dal campo.
