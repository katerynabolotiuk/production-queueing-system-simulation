import java.util.ArrayList;

public class SimModel {
    private static final double SIMULATION_TIME = 50000.0;
    private static final int NUM_RUNS = 100;

    private static final String DISTR = "exp";
    private static final double C_DELAY = 50.0;

    private static final double P1_DELAY = 40.0;
    private static final double P1_DEFECT_PROB = 0.04;

    private static final double P2_DELAY = 60.0;
    private static final double P2_DEFECT_PROB = 0.08;

    private static final double P3_DELAY = 100.0;
    private static final int P3_CHANNELS = 2;

    public static void main(String[] args) {
        simulateMultipleRuns(SIMULATION_TIME, NUM_RUNS);
    }

    private static void simulateMultipleRuns(double time, int runs) {
        ArrayList<Model> results = new ArrayList<>();
        
        for (int i = 0; i < runs; i++) {
            Model model = createModel();
            model.simulate(time);
            results.add(model);
            Element.resetNextId();
        }
        
        results.get(0).printAveragedResult(results, time, runs);
    }

    private static Model createModel() {
        Create c = new Create("CREATE", C_DELAY, DISTR);

        PrimaryProcess p1 = new PrimaryProcess("PRIMARY PROCESSING MACHINE 1", P1_DELAY, DISTR, "probability");
        PrimaryProcess p2 = new PrimaryProcess("PRIMARY PROCESSING MACHINE 2", P2_DELAY, DISTR, "probability");

        SecondaryProcess p3 = new SecondaryProcess("SECONDARY PROCESSING MACHINE", P3_DELAY, DISTR, P3_CHANNELS);

        c.addNextElement(p1);
        c.addNextElement(p2);

        p1.addNextElementWithProbability(p2, P1_DEFECT_PROB);
        p1.addNextElementWithProbability(p3, 1 - P1_DEFECT_PROB);

        p2.addNextElementWithProbability(p2, P2_DEFECT_PROB);
        p2.addNextElementWithProbability(p3, 1 - P2_DEFECT_PROB);

        p1.setQueue(1);
        p3.setQueue(12);

        ArrayList<Element> list = new ArrayList<>();
        list.add(c);
        list.add(p1);
        list.add(p2);
        list.add(p3);

        return new Model(list);
    }
}
