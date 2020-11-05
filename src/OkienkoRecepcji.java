import desmoj.core.simulator.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Agata
 */
public class OkienkoRecepcji extends SimProcess{
    /**
    * Kolejka czekających pacjentów którzy zostali już zarejestrowani
    */
    private desmoj.core.simulator.ProcessQueue<Pacjent> PacjentQueue;
    /**
     * referencja do modelu którego ten model pacjenta jest częścią
     */
    private Przychodnia myModel;

    public OkienkoRecepcji(Model owner, String name, boolean showInTrace) {
	super(owner, name, showInTrace);
        myModel = (Przychodnia)owner;
        // inicjalizacja kolejki pacjentów po rejestracji
        PacjentQueue = new ProcessQueue<Pacjent>(myModel, "Kolejka pacjentów po rejstracji.", false, false);
    }

    @Override
    public void lifeCycle() {
        while (true) {
            // jeśli recepjonistka ma już 5 albo więcej kart pacjentów lub kolejka czekających jest pusta
            // to idzie to lekarza z kartami
            if((PacjentQueue.length() >= Przychodnia.Pacjent_NR) || (myModel.PacjentQueueToRec.isEmpty()) ) {
                // jeśli żaden pacjent sie nie zgłosił jeszcze to jest wolna
                if(PacjentQueue.length() == 0) {
                    // recepjonistka jest bezczynna
                    myModel.BezczynnaRecepcja.insert(this);
                    // i czeka na wewanie
                    passivate();
                }
                // gdy są karty do zaniesienia
                else {
                    // kelekja recepcjonistek
                    myModel.Recepcjonistki.insert(this);
                    sendTraceNote("Recepjonistka przyszła do lekarza.");
                    // sprawdzenie czy jakiś lekarz jest bezczynny
                    if (!myModel.BezczynniLekarze.isEmpty()) {
                        // referencja do pierwszego wolnego lekarza
                        Lekarz Lekarz = myModel.BezczynniLekarze.first();
                        // usuniecie lekarza z kolejki wolnych lekarzy
                        myModel.BezczynniLekarze.remove(Lekarz);
                        Lekarz.activateAfter(this);
                    }
                    // zostawianie kart u lekarza
                    passivate();
                    // zerujemy liczbe kart które czekają aby je zanieśc do lekarza 
                    while (PacjentQueue.length() > 0) {
                        PacjentQueue.remove(PacjentQueue.first());
                    }
                }
            }
            // gdy <5 kart i sa pacjenci
            else {
                // pierwszy pacjent w kolejce proszony
                Pacjent nextPacjent = myModel.PacjentQueueToRec.first();
                // pacjent już nie teraz nie jest w kolejce więc go z niej usuwamy :)
                myModel.PacjentQueueToRec.remove(nextPacjent);

                // recepjonistka rejestruje pacjenta
                hold(new TimeInstant(myModel.getCzasRejestracji()));
                // pacjent zarejstrowany
                PacjentQueue.insert(nextPacjent);
                // karty przkazane
                nextPacjent.activate(TimeSpan.ZERO);
            }
        }
    }
    /**
     * @return kolejka zarejestrowanych pacjentów
     */
    ProcessQueue<Pacjent> getPacjents() {
        return this.PacjentQueue;
    }
}
