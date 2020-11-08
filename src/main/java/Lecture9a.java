import java.util.Scanner;
public class Lecture9a {

	public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        simpleCup cup1 = new simpleCup();

        int capacity;
        do {
            System.out.println("Please enter a capacity: ");
            capacity = sc.nextInt();
            cup1.setCapacity(capacity);
        } while(!(cup1.getCapacity() == capacity));

        int fill;
        do {
            System.out.println("Please enter a fill: ");
            fill = sc.nextInt();
            cup1.setFill(fill);
        } while(!(cup1.getFill() == fill));

        System.out.print(cup1.toString());
	}

}
