public class Create extends Element {
    public Create(String nameOfElement, double delay, String distrib) {
        super(nameOfElement, delay, distrib);
    }

    @Override
    public void inAct(Item item) {
    }

    @Override
    public void outAct() {
        Item item = new Item();
        incrementQuantity();

        super.setTnext(super.getTcurr() + super.getDelay());

        if (!getNextElements().isEmpty()) {
            Element next = chooseNextElement();
            if (next != null) {
                next.inAct(item);
            } else {
                incrementFailure();
            }
        }
    }

    @Override
    public void doStatistics(double delta) {
    }
}