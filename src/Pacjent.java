import desmoj.core.simulator.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Agata
 */



public class Pacjent extends SimProcess {

    /**
     * referencja do modelu którego ten model pacjenta jest częścią
     */
    private Przychodnia myModel;
     public static int warstopu = 0;

    public Pacjent(Model owner, String name, boolean showInTrace) {
        // wywołanie superkonstruktora
	super(owner, name, showInTrace);
	// zapisanie modelu który jest właścicielem tego modelu pacjenta (właścielem jest model przychodni)
        myModel = (Przychodnia)owner;
    }

    /**
     * Opisuje cykl życia pacjenta w modelu przychodni.
     */
    @Override
    public void lifeCycle() {
        // ustawienie się w kolejce do rejestracji
	myModel.PacjentQueueToRec.insert(this);
	sendTraceNote("Przybył pacjent. Długość kolejki pacjentów do rejestracji: " + myModel.PacjentQueueToRec.length());

        // RECEPCJA
	// sprawdzenie czy recepjonistka jest dostępna
	if (!myModel.BezczynnaRecepcja.isEmpty()) {
            // referencja do pierwszego wolnego recepjonistki
            OkienkoRecepcji rec = myModel.BezczynnaRecepcja.first();
            // usuniecie recepjo nistki z kolejki wolnych recepjonistek
            myModel.BezczynnaRecepcja.remove(rec);
            // umieszczenie recepjonistki na liście zdarzeń zaraz po obecnym pacjencie
            rec.activateAfter(this);
	}

	// czekanie na przyjęcie pacjenta przez recepjonistkę
	passivate();
	// wysłąnie notki informującej
	sendTraceNote("Pacjent został zarejestrowany, karta zaniesiona do lekarza.");

        // LEKARZ
        // sprawdzenie czy lekarz jest dostępny
	if (!myModel.BezczynniLekarze.isEmpty()) {
            // referencja do pierwszego wolnego lekarza
            Lekarz Lekarz = myModel.BezczynniLekarze.first();
            // usuniecie recepjonistki z kolejki wolnych lekarzy
            myModel.BezczynniLekarze.remove(Lekarz);
            // umieszczenie lekarza na liście zdarzeń zaraz po obecnym pacjencie dla pewności że będzie on następnym obsługiwanym poacjentem
            Lekarz.activateAfter(this);
	}

	// czekanie na zbadanie pacjenta przez lekarza
	passivate();
        ++warstopu;
	sendTraceNote("Pacjent zbadany.");
    }

}
