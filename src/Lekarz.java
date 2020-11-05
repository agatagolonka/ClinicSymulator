import desmoj.core.simulator.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Reprezentacja lekarza
 *
 * @author Agata
 */

public class Lekarz extends SimProcess{
    /**
     * referencja do modelu którego ten model lekarza jest częścią
     */
    private Przychodnia myModel;
   /**
	 * Constructor Lekarza
	 *
	 *
	 * @param owner the model this process belongs to
	 * @param showInTrace flag to indicate if this process shall produce
	 *                    output for the trace
	 */
    private int NR;

    public Lekarz(Model owner, String name, boolean showInTrace, int nr) {
        // wywołanie superkonstruktora
	super(owner, name, showInTrace);
	// store a reference to the model this 'lekarz' is associated with
        myModel = (Przychodnia)owner;
        NR = nr;
    }

    /**
     * Opisuje cykl życia lekarza w modelu przychodni.
     */
    @Override
    public void lifeCycle() {
	while (true) {
            // sprawdzenie czy ktoś oczekuje na lekarza
            // na początku sprawdzenie czy nie czeka recepjonistka
            if(myModel.Recepcjonistki.isEmpty()) {
                // sprawdzenie czy nie czekają pacjenci
                if (myModel.Poczekalnia[this.NR].isEmpty()) {
                    // lekarz jest bezczynny
                    myModel.BezczynniLekarze.insert(this);
                    // czeka aż ktoś ktoś wezwie
                    passivate();
                }
                else { //czekają pacjenci
                    // wchodzi pierwszy pacjent z kolejki
                    Pacjent nextPacjent = myModel.Poczekalnia[this.NR].first();
                    // pacjenta usuwamy z kolejki
                    myModel.Poczekalnia[this.NR].remove(nextPacjent);

                    // badanie pacjenta, diagnoza
                    // czas na badanie
                    hold(new TimeInstant(myModel.getCzasBadania()));
                    // aktywujemy pacjenta
                    nextPacjent.activate(TimeSpan.ZERO);
                }
            }
            else { // czeka recepjonistka
                OkienkoRecepcji nextRec = myModel.Recepcjonistki.first();
                // usuwamy ja z kolejki oczekujących
                myModel.Recepcjonistki.remove(nextRec);

                //czas na rozmowe
                hold(new TimeInstant(myModel.getCzasKarty()));
                // dodajemy pacjentów do kolejki czekających do tego lekarza
                for(int i=0; i < nextRec.getPacjents().length(); i++ ) {
                    myModel.Poczekalnia[this.NR].insert(nextRec.getPacjents().first());
                }
                // wysłanie notki informującej o tym że recepjonistka zaniosła karty lekarzowi
                nextRec.sendTraceNote("Przekazano karty pacjentów lekarzowi " + this.NR + ".");
                // aktywujemy recepcjonistke
                nextRec.activate(TimeSpan.ZERO);
            }
	}
    }
    public int getID() {
        return this.NR;
    }
}
