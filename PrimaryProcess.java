import java.util.LinkedList;
import java.util.Queue;

public class PrimaryProcess extends Element {
    private int queue, maxqueue; 
    private Item currentItem;
    private Queue<Item> itemQueue;

    private double meanQueue;
    private double busyTime;

    public PrimaryProcess(String nameOfElement, double delay, String distrib) {
        super(nameOfElement, delay, distrib);

        setTnext(Double.MAX_VALUE);
        maxqueue = Integer.MAX_VALUE;
        itemQueue = new LinkedList<>();
    }

    public PrimaryProcess(String nameOfElement, double delay, String distrib, String routingType) {
        super(nameOfElement, delay, distrib, routingType);

        setTnext(Double.MAX_VALUE);
        maxqueue = Integer.MAX_VALUE;
        itemQueue = new LinkedList<>();
    }

    @Override
    public void inAct(Item item) {
        if (getState() == 0) {
            setState(1);
            currentItem = item;
            currentItem.incrementPrimaryProcessCount();

            setTnext(getTcurr() + getDelay());
        } else {
            if (queue < maxqueue) {
                itemQueue.add(item);
                incrementQueue();
            } else {
                incrementFailure();
            }
        }
    }

    @Override
    public void outAct() {
        Item finishedItem = currentItem;

        incrementQuantity();
        setTnext(Double.MAX_VALUE);
        setState(0);

        if (queue > 0) {
            Item nextItem = itemQueue.poll();
            decrementQueue();
            setState(1);

            currentItem = nextItem;
            currentItem.incrementPrimaryProcessCount();
            setTnext(getTcurr() + getDelay());
        } else {
            currentItem = null;
        }

        if (!getNextElements().isEmpty()) {
            if (finishedItem.getPrimaryProcessCount() >= 2) {
                incrementFailure();
            } else {
                Element next = chooseNextElement();
                if (next != null) {
                    next.inAct(finishedItem);
                } else {
                    incrementFailure();
                }
            }
        }
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public void incrementQueue() {
        queue++;
    }

    public void decrementQueue() {
        queue--;
    }

    public int getMaxqueue() {
        return maxqueue;
    }

    public void setMaxqueue(int maxqueue) {
        this.maxqueue = maxqueue;
    }


    public double getMeanQueue() {
        return meanQueue;
    }

    public double getBusyTime() {
        return busyTime;
    }

    public double getLoad(double tcurr) {
        return busyTime / tcurr;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("failure = " + this.getFailure());
    }

    @Override
    public void printResult() {
        System.out.println("Елемент " + getName());
        System.out.println("----------------------------------------------------");
        System.out.printf("%-40s | %8d\n", "обслуговано деталей", getQuantity());
    }

    @Override
    public void doStatistics(double delta) {
        meanQueue += queue * delta;

        if (getState() == 1) {
            busyTime += delta;
        }
    }
}