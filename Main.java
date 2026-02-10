import java.util.Scanner;

public class Main
{
    // create objects for database, controller, view, class
    Runnable send;
    Runnable receive;
    Thread sendThread;
    Thread receiveThread;

    public Main()
    {
        // initialize sending and receiving threads
        Runnable send = new udpSend();
        Runnable receive = new udpReceive();
        Thread sendThread = new Thread(send);
        Thread receiveThread = new Thread(receive);
        sendThread.start();
        receiveThread.start();
    }

    public static void main(String[] args)
    {
        Main main = new Main();
        // delay to allow threads to start before main thread ends, will need to change this in actual program
        Thread.sleep(1000);
        // send 202 to start python script
        System.out.println("Please enter 202 to start the game.");
        Scanner scanner = new Scanner(System.in);
        int input = scanner.nextInt();
        if (input == 202) {
            System.out.println("Hopefully python works!");
        } else {
            System.out.println("Invalid input. Please enter 202 to start the game.");
        }
    }
}
