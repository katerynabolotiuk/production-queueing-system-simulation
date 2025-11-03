public class Item {
    private int id;
    private static int nextId = 0;
    private int primaryProcessCount;
    private double tfinish; 

    public Item() {
        id = nextId;
        primaryProcessCount = 0;
    }

    public int getPrimaryProcessCount() {
        return primaryProcessCount;
    }

    public void incrementPrimaryProcessCount() {
        primaryProcessCount++;
    }

    public int getId() {
        return id;
    }

    public double getTfinish() {
        return tfinish;
    }

    public void setTfinish(double time) {
        tfinish = time;
    }
}
