import java.util.LinkedList;
import java.util.Queue;

public class SecondaryProcess extends Element {
    private int queue, maxqueue;  
    private int channels;
    private int[] channelState;   
    private Item[] channelItems; 
    private Queue<Item> itemQueue;

    private double meanQueue;
    private double[] channelBusyTime; 
    private int[] channelProcessed; 

    public SecondaryProcess(String nameOfElement, double delay, String distrib) {
        super(nameOfElement, delay, distrib);

        maxqueue = Integer.MAX_VALUE;
        channels = 1;
        channelState = new int[channels];  
        channelState[0] = 0;
        channelItems = new Item[channels];
        itemQueue = new LinkedList<>();

        channelBusyTime = new double[channels];
        channelProcessed = new int[channels];
    }

    public SecondaryProcess(String nameOfElement, double delay, String distrib, String routingType) {
        super(nameOfElement, delay, distrib, routingType);

        maxqueue = Integer.MAX_VALUE;
        channels = 1;
        channelState = new int[channels];  
        channelState[0] = 0;
        channelItems = new Item[channels];
        itemQueue = new LinkedList<>();

        channelBusyTime = new double[channels];
        channelProcessed = new int[channels];
    }

    public SecondaryProcess(String nameOfElement, double delay, String distrib, int numChannels) {
        super(nameOfElement, delay, distrib);

        maxqueue = Integer.MAX_VALUE;
        channels = numChannels;
        channelState = new int[channels];
        channelItems = new Item[channels];
        for (int i = 0; i < channels; i++) {
            channelState[i] = 0;          
            channelItems[i] = null;
        }
        itemQueue = new LinkedList<>();

        channelBusyTime = new double[channels];
        channelProcessed = new int[channels];
    }

    public SecondaryProcess(String nameOfElement, double delay, String distrib, String routingType, int numChannels) {
        super(nameOfElement, delay, distrib, routingType);

        maxqueue = Integer.MAX_VALUE;
        channels = numChannels;
        channelState = new int[channels];
        channelItems = new Item[channels];
        for (int i = 0; i < channels; i++) {
            channelState[i] = 0;   
            channelItems[i] = null;       
        }
        itemQueue = new LinkedList<>();

        channelBusyTime = new double[channels];
        channelProcessed = new int[channels];
    }

    @Override
    public void inAct(Item item) {
        if (channels > 1) {
            if (queue <= 3) {
                channelState[1] = 2; 
            } else if (channelState[1] == 2) {
                channelState[1] = 0;
            }
        }


        int freeChannel = -1;
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 0) {
                freeChannel = i;
                break;
            }
        }

        if (freeChannel != -1) {
            channelState[freeChannel] = 1; 
            channelItems[freeChannel] = item; 
            item.setTfinish(getTcurr() + getDelay());
            if (getTnext() == Double.MAX_VALUE) {
                setTnext(item.getTfinish());
            }
        } else {
            if (queue < maxqueue) {
                itemQueue.add(item);
                queue++;
            } else {
                incrementFailure();
            }
        }
    }

    @Override
    public void outAct() {
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 1 && channelItems[i] != null && 
                channelItems[i].getTfinish() == getTcurr()) {

                incrementQuantity();
                
                channelItems[i] = null;
                channelProcessed[i]++;

                if (i == 1) { 
                    if (queue <= 3) {
                        channelState[i] = 2; 
                    } else {
                        channelState[i] = 0; 
                    }
                } else {
                    channelState[i] = 0;
                }

                if (queue > 0 && channelState[i] == 0) {
                    Item nextItem = itemQueue.poll();
                    queue--;
                    channelState[i] = 1;
                    channelItems[i] = nextItem;
                    nextItem.setTfinish(getTcurr() + getDelay());
                }
            }
        }
        
        double minTnext = Double.MAX_VALUE;
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 1 && channelItems[i] != null) {
                minTnext = Math.min(minTnext, channelItems[i].getTfinish());
            }
        }            
        setTnext(minTnext);
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getMaxqueue() {
        return maxqueue;
    }

    public void setMaxqueue(int maxqueue) {
        this.maxqueue = maxqueue;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        System.out.println("failure = " + getFailure());
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
        for (int i = 0; i < channels; i++) {
            if (channelState[i] == 1) {
                channelBusyTime[i] += delta;
            }
        }
    }

    public double getMeanQueue() {
        return meanQueue;
    }

    public double getBusyTime() {
        double totalBusy = 0.0;
        for (int i = 0; i < channels; i++) {
            totalBusy += channelBusyTime[i];
        }
        return totalBusy / channels;
    }

    public double getLoad(double tcurr) {
        double totalBusy = 0.0;
        for (int i = 0; i < channels; i++) {
            totalBusy += channelBusyTime[i];
        }
        return totalBusy / (tcurr * channels); 
    }


    public int[] getChannelProcessed() {
        return channelProcessed;
    }

    public double[] getChannelBusyTime() {
        return channelBusyTime;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int numChannels) {
        this.channels = numChannels;
        this.channelState = new int[numChannels];
        for (int i = 0; i < numChannels; i++) {
            this.channelState[i] = 0; 
        }
    }

    public int getChannelState(int i) {
        return channelState[i];
    }

    public void setChannelState(int i, int state) {
        this.channelState[i] = state;
    }

    public int getItemsInChannels() {
        int count = 0;
        for (int state : channelState) {
            if (state == 1) { 
                count++;
            }
        }
        return count;
    }
}