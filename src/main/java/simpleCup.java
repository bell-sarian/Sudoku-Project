public class simpleCup {
    public int capacity, fill; // initialize variables / attributes

    // Constructor
    public void cup() {
        capacity = 1;
        fill = 1;
    }

    // Getters
    public int getCapacity() {
        return capacity;
    }
    public int getFill() {
        return fill;
    }

    // Setters
    public void setCapacity(int c) {
        if( c > 100){
            System.out.println("Yo, that's too much!");
        }
        else{
            capacity = c;
        }
    }

    public void setFill(int f) {
        if (f <= 0 || f > capacity) {
            System.out.println("Nope! Try a different fill.");
        }
        else {
            fill = f;
        }
    }

    public String toString() {
        return "This is the cup that you entered with " + capacity + " capacity, and " + fill + " fill.";
    }


}
