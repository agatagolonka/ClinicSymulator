import desmoj.core.simulator.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Agata
 */
public class GeneratorPacjentow extends SimProcess{
    public GeneratorPacjentow(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }
    /**
     * Opisuje proces życia generatora : zapewnia ciągły napływ pacjentów.
     */
    @Override
    public void lifeCycle() {
        // get a reference to the model
	Przychodnia model = (Przychodnia)getModel();

	// endless loop:
	while (true) {

            // kreuje nowego pacjenta
            // Parameters:
            // model   = it's part of this model
            // "Pacjent" = name of the object
            // true    = tak, pokaż w truck
            Pacjent Pacjent = new Pacjent(model, "<span style=\"color:green\"><b>Pacjent</b> </span>", false);

            // Nowo powstały pacjent
            
            Pacjent.activateAfter(this);

            // Czekaj na nowego Pacjenta
            hold(new TimeInstant(model.getPacjentPrzychodzi()));
            // from inside to outside...
            // we draw a new inter-arrival time
            // we make a SimTime object out of it and
            // we wait for exactly this period of time
	}
    }

}
