import desmoj.core.simulator.*;
import desmoj.core.dist.*;
import java.util.concurrent.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Agata
 */


public class Przychodnia extends Model{


      /**
     * parametr modelu: liczba recepjonistek
     */
    protected static int OkienkoRecepcji_NR = 1;
     /**
     * parametr modelu: co ilu pacjentów recepjonistka zanosi karty
     */
    protected static int Pacjent_NR = 7;
    /**
     * parametr modelu: liczba lekarzy
     */
    protected static int Lekarz_NR = 2;
  
   /**
     * parametr modelu: średni czas przyjscia pacjenta do przychodni
     */
    protected static double TIME_Przybyl = 15.0;
       /**
     * parametr modelu: maksymalny czas rejestrowania pacjenta
     */
    protected static double MAX_Rejestracja = 1.0;
    /**
     * parametr modelu: minimalny czas rejestrowania pacjenta
     */
    protected static double MIN_Rejestracja = 1.0;

    /**
     * parametr modelu: maksymalny czas badania pacjenta
     */
    protected static double MAX_Badanie = 30.0;
    /**
     * parametr modelu: minimalny czas badania pacjenta
     */
    protected static double MIN_Badanie = 1.0;
 
     /**
     * parametr modelu: średni czas przebywania recepjonistki u lekarza
     */
    protected static double TIME_Karty = 2.0;
    /**
    * Losowa liczba określająca czas przybycia następnego pacjenta.
    */
    private desmoj.core.dist.RealDistExponential PacjentPrzychodzi;
    /**
    * Losowa liczba określająca czas obsłużenia pacjenta przez lekarza.
    */
    private desmoj.core.dist.RealDistUniform CzasBadania;
    /**
    * Losowa liczba określająca czas rejestrowania się pacjenta.
    */
    private desmoj.core.dist.RealDistUniform CzasRejestracji;
    /**
    * Losowa liczba określająca czas przekazu kart lekarzowi
    */
    private desmoj.core.dist.RealDistExponential CzasKarty;
    /**
    * Kolejka czekających pacjentów do recepcji. Symuluje proces czekania do rejestracji przez pacjentów w przychodni.
    */
    protected desmoj.core.simulator.ProcessQueue<Pacjent> PacjentQueueToRec;
    /**
    * Kolejka czekających pacjentów do lekarza. Symuluje poczekalnię w przychodni.
    */
    protected desmoj.core.simulator.ProcessQueue<Pacjent> [] Poczekalnia;
    /**
    * Kolejka czekających recjonistek do lekarza. 
    */
    protected desmoj.core.simulator.ProcessQueue<OkienkoRecepcji> Recepcjonistki;
    /**
    * Kolejka czekających lekarzy. 
    */
    protected desmoj.core.simulator.ProcessQueue<Lekarz> BezczynniLekarze;
    /**
    * Kolejka czekających recepjonistek. Jeśli nie ma pacjentów w kolejce do rejestracji model recepjonistki
    * zostaje zapisany w tej kolejce. Recepjonistka czeka w niej aż przyjdzie pacjent.
    */
    protected desmoj.core.simulator.ProcessQueue<OkienkoRecepcji> BezczynnaRecepcja;


    /**
    * Przychodnia konstruktor
    *
    * Kontruuje nowy model procesowy przychodni (obiekt Przychodnia) poprzez wywołanie konstuktora klasy nadrzędnej.
    *
    * @param owner to model którego ten model jest częścią (null jeśli nie ma takiego modelu)
    * @param modelName to nazwa modelu
    * @param showInReport flaga określająca czy model ma tworzyć wyjściowy raport w pliku
    * @param showInTrace flaga określająca czy model ma tworzyć wyjściowy trace file
    */
    public Przychodnia(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
    * zwraca opis modelu przychodni użytego w raporcie.
    * @return opis modelu w postaci stringu
    */
    @Override
    public String description() {
        return "Ten model opisuje działanie przychodni. " +
                "Losowi pacjenci kierowani są do losowego lekarza, a ich obsługa odbywa się wg kolejności zgłoszeń). " +
                "Lekarze pracują wymiennie. Symulacja kończy się w momencie zakończenia sukcesem obsługi wszystkich pacjentów.";
    }

    /**
    * Aktywuje dynamiczne komponenty modelu.
    * W tej metodzie są ustawiane wszystkie zdarzenia i procesy które są potrzebne do rozpoczącia symulacji.
    * Generator pacjentów, tworzenie modeli
    */
    @Override
    public void doInitialSchedules() {
        // stworzenie i aktywowanie lekarzy
	for (int i=0; i < Lekarz_NR; i++){
            Lekarz Lekarz = new Lekarz(this, "Lekarz " + Integer.toString(i+1) + " ", true, i);
            Lekarz.activate(TimeSpan.ZERO);
	}
        // stworzenie i aktywowanie recepjonistki(ek)
	for (int i=0; i < OkienkoRecepcji_NR; i++){
            OkienkoRecepcji rec = new OkienkoRecepcji(this, "Recepjonistka " + Integer.toString(i+1) + " ", true);
            rec.activate(TimeSpan.ZERO);
	}
	GeneratorPacjentow generator = new GeneratorPacjentow(this, "Przybycie pacjenta ", false);
        generator.activate(TimeSpan.ZERO);
    }

    /**
    * Inicjalizuje statyczne komponenty modelu.
    */
    @Override
    public void init() {
        // inicjalizacja czasu badanie przez lekarza
        this.CzasBadania = new RealDistUniform(this, "Czas badania przez lekarza. ", Przychodnia.MIN_Badanie, Przychodnia.MAX_Badanie, true, false);
        // inicjalizacja czasu rejestrowania pacjenta przez recepcjonistkę
        this.CzasRejestracji = new RealDistUniform(this, "Czas rejestrowania pacjenta przez recepcjonistkę. ", Przychodnia.MIN_Rejestracja, Przychodnia.MAX_Rejestracja, true, false);
        // inicjalizacja czasu przybycia pacjenta do przychodni
        this.PacjentPrzychodzi = new RealDistExponential(this, "Czas przybycia pacjenta do przychodni. ", Przychodnia.TIME_Przybyl, true, false);
        // niezbędna bo czas między przybyciami pacjentów nie może być ujemny
        this.PacjentPrzychodzi.setNonNegative(true);
        // inicjalizacja czasu przebywania recepjonistki u lekarza
        this.CzasKarty = new RealDistExponential(this, "Czas przebywania recepjonistki u lekarza. ", Przychodnia.TIME_Karty, true, false);
        this.CzasKarty.setNonNegative(true);
        // inicjalizacja kolejki pacjentów do recepcji
        this.PacjentQueueToRec = new ProcessQueue<Pacjent>(this, "Kolejka pacjentów do recepcji. ", true, true);
         // inicjalizacja kolejek pacjentów lekarzy
        this.Poczekalnia = new ProcessQueue[Przychodnia.Lekarz_NR];
        for(int i=0; i < Przychodnia.Lekarz_NR; i++ ) {
            this.Poczekalnia[i] = new ProcessQueue<Pacjent>(this, "Kolejka pacjentów do lekarza(y). " + Integer.toString(i+1) + ". ", true, true);
        }
        // inicjalizacja kolejki ecpcjonistki(ek) do lekarza(y)
        this.Recepcjonistki = new ProcessQueue<OkienkoRecepcji>(this, "Kolejka recpcjonistki(ek) do lekarza(y). ", true, true);
        // inicjalizacja kolejki wolnych lekarzy
        this.BezczynniLekarze = new ProcessQueue<Lekarz>(this, "Kolejka wolnych lekarzy. ", true, true);
        // inicjalizacja kolejki wolnych recepjocnistek
        this.BezczynnaRecepcja = new ProcessQueue<OkienkoRecepcji>(this, "Kolejka wolnych recepjocnistek. ", true, true);
    }
    /**
     * @return czas do przyjścia następnego pacjenta
     */
    public double getPacjentPrzychodzi() {
        return PacjentPrzychodzi.sample();
    }
    /**
     * @return czas rejestracji pacjenta
     */
    public double getCzasRejestracji() {
	return CzasRejestracji.sample();
    }

    /**
     * @return czas badania pacjenta
     */
    public double getCzasBadania() {
	return CzasBadania.sample();
    }

    /**
     * @return pczas wizyty recepcjonistki u lekarza
     */
    public double getCzasKarty() {
        return CzasKarty.sample();
    }

    /**
     * 
     * @param args 
     */
    public static void main(java.lang.String[] args) {
        Przychodnia model = new Przychodnia(null, "Przychodnia", true, true);
        Experiment exp = new Experiment("Symulacja działania przychodni");
        model.connectToExperiment(exp);

        // ustawienie parametrów symulacji
        exp.setShowProgressBar(false); //ukrycie progressBar
        exp.stop(new StopCondition(model, "StopCondition", true)); //warunki końca
        exp.stop(new TimeInstant(100.0));
        exp.tracePeriod(new TimeInstant(0.0), new TimeInstant(100.0)); 
        exp.start();
        //SYMULACJA
        // wygenerowanie raportu i trace'a
        exp.report();
        // zakończenie wszystkich procesów i calej symulacji
        exp.finish();
    }

}
