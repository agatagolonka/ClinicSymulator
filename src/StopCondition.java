import desmoj.core.simulator.*;;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Agata
 */
class StopCondition extends Condition{
    private int warstopu;

    public StopCondition(Przychodnia model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

   //srop gdy obsluzy 50 pacjentow
    public boolean check() {
         if (Pacjent.warstopu==50)
        return true;
        else return false;
    }

    /**
     * overloading necessary method
     * @param entity
     * @return true
     */
    public boolean check(Entity entity) {
        return true;
    }

}

