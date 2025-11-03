import java.util.ArrayList;

public class Model {
    private ArrayList<Element> list = new ArrayList<>();
    double tnext, tcurr;
    int event;
    double time;

    public Model(ArrayList<Element> elements) {
        list = elements;
        tnext = 0.0;
        event = 0;
        tcurr = tnext;
    }

    public void simulate(double time) {
        this.time = time;

        while (tcurr < time) {
            tnext = Double.MAX_VALUE;
            for (Element e : list) {
                if (e.getTnext() < tnext) {
                    tnext = e.getTnext();
                    event = e.getId();
                }
            }

            System.out.println("\nIt's time for event in " +
                    list.get(event).getName() +
                    ", time = " + tnext);

            for (Element e : list) {
                e.doStatistics(tnext - tcurr);
            }

            tcurr = tnext;

            for (Element e : list) {
                e.setTcurr(tcurr);
            }

            list.get(event).outAct();

            for (Element e : list) {
                if (e.getTnext() == tcurr) {
                    e.outAct();
                }
            }

            printInfo();
        }
    }

    public void printInfo() {
        for (Element e : list) {
            e.printInfo();
        }
    }

    public void printAveragedResult(ArrayList<Model> models, double time, int numRuns) {
        System.out.println("\n\n=====================РЕЗУЛЬТАТИ=====================\n");
        System.out.printf("%-40s | %8.1f\n", "Час моделювання", time);
        System.out.printf("%-40s | %8d\n\n", "Кількість прогонів", numRuns);

        double avgSystemFailure = 0.0;
        double avgSystemSuccess = 0.0;

        for (int elementIndex = 0; elementIndex < list.size(); elementIndex++) {
            Element baseElement = models.get(0).list.get(elementIndex);
            
            System.out.println("----------------------------------------------------");
            System.out.println("Елемент " + baseElement.getName());
            System.out.println("----------------------------------------------------");

            if (baseElement instanceof Create) {
                long totalCreated = 0;
                for (Model m : models) {
                    totalCreated += m.list.get(elementIndex).getQuantity();
                }
                double avgCreated = (double) totalCreated / numRuns;
                System.out.printf("%-40s | %8.3f\n", "створено деталей", avgCreated);
                
            } else if (baseElement instanceof PrimaryProcess) {

                long totalQuantity = 0;
                long totalFailure = 0;
                double totalMeanQueue = 0.0;
                double totalBusyTime = 0.0; 
                
                for (Model m : models) {
                    PrimaryProcess p = (PrimaryProcess) m.list.get(elementIndex);
                    totalQuantity += p.getQuantity();
                    totalFailure += p.getFailure();
                    totalMeanQueue += p.getMeanQueue();
                    totalBusyTime += p.getBusyTime(); 
                }
                
                double avgQuantity = (double) totalQuantity / numRuns;
                double avgFailure = (double) totalFailure / numRuns;
                double avgMeanQueue = totalMeanQueue / numRuns;
                double avgLoad = (totalBusyTime / numRuns) / time; 
                double avgFailureProb = avgFailure / (avgQuantity + avgFailure);
                double avgWaitTime = avgMeanQueue / avgQuantity;

                avgSystemFailure += avgFailure;
                
                System.out.printf("%-40s | %8.3f\n", "обслуговано деталей", avgQuantity);
                System.out.printf("%-40s | %8.3f\n", "кількість відмов (відходів)", avgFailure);
                System.out.printf("%-40s | %8.3f\n", "ймовірність відмови (відходів)", avgFailureProb);
                System.out.printf("%-40s | %8.3f\n", "середня довжина черги", avgMeanQueue / time);
                System.out.printf("%-40s | %8.3f\n", "середнє завантаження пристрою", avgLoad);
                System.out.printf("%-40s | %8.3f\n", "середній час очікування", avgWaitTime);
                
            } else if (baseElement instanceof SecondaryProcess) {
                
                SecondaryProcess templateP = (SecondaryProcess) baseElement;

                long totalQuantity = 0;
                long totalFailure = 0;
                double totalMeanQueue = 0.0;
                double totalBusyTime = 0.0; 
                double[] totalChannelBusyTime = new double[templateP.getChannels()];
                int[] totalChannelProcessed = new int[templateP.getChannels()];
                
                for (Model m : models) {
                    SecondaryProcess p = (SecondaryProcess) m.list.get(elementIndex);
                    totalQuantity += p.getQuantity();
                    totalFailure += p.getFailure();
                    totalMeanQueue += p.getMeanQueue();
                    totalBusyTime += p.getBusyTime(); 
                    
                    for (int i = 0; i < templateP.getChannels(); i++) {
                        totalChannelBusyTime[i] += p.getChannelBusyTime()[i];
                        totalChannelProcessed[i] += p.getChannelProcessed()[i];
                    }
                }
                
                double avgQuantity = (double) totalQuantity / numRuns;
                double avgFailure = (double) totalFailure / numRuns;
                double avgMeanQueue = totalMeanQueue / numRuns;
                double avgLoad = (totalBusyTime / numRuns) / time; 
                double avgFailureProb = avgFailure / (avgQuantity + avgFailure);
                double avgWaitTime = avgMeanQueue / avgQuantity;

                avgSystemFailure += avgFailure;
                avgSystemSuccess += avgQuantity;
                
                System.out.printf("%-40s | %8.3f\n", "обслуговано деталей", avgQuantity);
                System.out.printf("%-40s | %8.3f\n", "кількість відмов (відходів)", avgFailure);
                System.out.printf("%-40s | %8.3f\n", "ймовірність відмови (відходів)", avgFailureProb);
                System.out.printf("%-40s | %8.3f\n", "середня довжина черги", avgMeanQueue / time);
                System.out.printf("%-40s | %8.3f\n", "середнє завантаження пристрою", avgLoad);
                System.out.printf("%-40s | %8.3f\n", "середній час очікування", avgWaitTime);
                
                if (templateP.getChannels() > 1) {
                    for (int i = 0; i < templateP.getChannels(); i++) {
                        
                        double avgProcessed = (double) totalChannelProcessed[i] / numRuns;
                        double avgChannelLoad = (totalChannelBusyTime[i] / numRuns) / time;
                        
                        String label1 = String.format("обслуговано деталей каналом %d", i + 1);
                        String label2 = String.format("середнє завантаження каналу %d", i + 1);
                        
                        System.out.printf("%-40s | %8.1f\n", label1, avgProcessed);
                        System.out.printf("%-40s | %8.3f\n", label2, avgChannelLoad);
                    }
                }
            }
            System.out.println("----------------------------------------------------\n\n");
        }

        double avgSystemFailureProb = avgSystemFailure / (avgSystemSuccess + avgSystemFailure);
        System.out.printf("%-40s | %8.3f\n", "Кількість оброблених деталей у системі", avgSystemSuccess);
        System.out.printf("%-40s | %8.3f\n", "Кількість відмов (відходів) у системі", avgSystemFailure);
        System.out.printf("%-40s | %8.3f\n", "Ймовірність відмови (відходів) у системі", avgSystemFailureProb);
    }
}
