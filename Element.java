import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Element {
    private String name;
    private double tnext; 
    private double tcurr;  
    private double delayMean, delayDev; 
    private String distribution; 
    private String routingType;
    private int quantity; 
    private int failure;  
    private int state; 
    private ArrayList<Element> nextElements;  
    private Map<Element, Double> probabilities;
    private static int nextId = 0;
    private int id;

    public Element() {
        tnext = Double.MAX_VALUE;
        delayMean = 1.0;
        distribution = "";
        routingType = "";
        tcurr = tnext;
        state = 0;
        nextElements = new ArrayList<>();
        id = nextId++;
        name = "element" + id;
        probabilities = new HashMap<>();
    }

    public Element(String nameOfElement, double delay, String distrib) {
        tnext = 0.0;
        delayMean = delay;
        distribution = distrib;
        routingType = "";
        tcurr = tnext;
        state = 0;
        nextElements = new ArrayList<>();
        id = nextId++;
        name = nameOfElement;
        probabilities = new HashMap<>();
    }

    public Element(String nameOfElement, double delay, String distrib, String routType) {
        tnext = 0.0;
        delayMean = delay;
        distribution = distrib;
        routingType = routType;
        tcurr = tnext;
        state = 0;
        nextElements = new ArrayList<>();
        id = nextId++;
        name = nameOfElement;
        probabilities = new HashMap<>();
    }

    public static void resetNextId() {
        nextId = 0; 
    }

     public double getDelayMean() {
        return delayMean;
    }

    public void setDelayMean(double delayMean) {
        this.delayMean = delayMean;
    }

    public double getDelayDev() { 
        return delayDev; 
    }

    public void setDelayDev(double delayDev) { 
        this.delayDev = delayDev; 
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getQuantity() {
        return quantity;
    }

    public void incrementQuantity() {
        quantity++;
    }

    public int getFailure() {
        return failure;
    }

    public void incrementFailure() {
        failure++;
    }

    public double getTcurr() {
        return tcurr;
    }

    public void setTcurr(double tcurr) {
        this.tcurr = tcurr;
    }


    public double getTnext() {
        return tnext;
    }

    public void setTnext(double tnext) {
        this.tnext = tnext;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getRoutingType() {
        return routingType;
    }

    public void setRoutingType(String routingType) {
        this.routingType = routingType;
    }

    public Map<Element, Double> getProbabilities() {
        return probabilities;
    }
 
    public void addNextElement(Element e) {
        nextElements.add(e);
    }

    public void addNextElementWithProbability(Element e, double probability) {
        nextElements.add(e);
        probabilities.put(e, probability);
        routingType = "probability";
    }

    public ArrayList<Element> getNextElements() {
        return nextElements;
    }

    public double getDelay() {
        String distribution = getDistribution().toLowerCase();
        double mean = getDelayMean();
        double dev = getDelayDev();

        return switch (distribution) {
            case "exp" -> FunRand.exp(mean);          
            case "norm" -> FunRand.norm(mean, dev);   
            case "unif" -> FunRand.unif(mean, dev);  
            default -> mean;                         
        };
    }

    public Element chooseNextElement() {
        if (nextElements.isEmpty()) return null;
        Element nextElement= null;

        switch (getRoutingType()) {
            case "probability" -> {
                double r = Math.random();
                double cumulative = 0.0;

                for (Element e : nextElements) {
                    cumulative += probabilities.get(e);
                    if (r <= cumulative) {
                        nextElement = e;
                        break;
                    }
                }
                return nextElement; 
            }
            default -> {
                int index = (int) (Math.random() * nextElements.size());
                return nextElements.get(index);
            }
        }
    }

    public void printResult() {
        System.out.println("Елемент " + getName());
        System.out.println("----------------------------------------------------");
        System.out.printf("%-40s | %8d\n", "створено деталей", quantity);
    }

    public void printInfo() {
        System.out.println(getName() + " state= " + state +
                " quantity = " + quantity +
                " tnext= " + tnext);
    }

    public abstract void inAct(Item item);
    public abstract void outAct();
    public abstract void doStatistics(double delta);
}