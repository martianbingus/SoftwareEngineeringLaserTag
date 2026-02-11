import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.IOException;

public class Laser
{
    // create objects for database, controller, view, class
    Runnable send;
    Runnable receive;
    Thread sendThread;
    Thread receiveThread;
    Database database;

    public Laser()
    {
        // initialize sending and receiving threads
        send = new udpSend();
        receive = new udpReceive();
        sendThread = new Thread(send);
        receiveThread = new Thread(receive);
        //try {database = new Database();}
        //catch (SQLException UwU) {UwU.printStackTrace();}
        sendThread.start();
        receiveThread.start();
    }

    public static void main(String[] args)
    {
        Laser laser = new Laser();

        try
       	{
            Thread.sleep(1000);
        }
       	catch (InterruptedException e)
       	{
            e.printStackTrace();
        }

        System.out.println("Please enter 202 to start the game.");
        Scanner scanner = new Scanner(System.in);

        if (scanner.hasNextInt())
       	{
            int input = scanner.nextInt();
            if (input == 202)
	    {
                System.out.println("Transmitting start code...");
            }
	    else
	    {
                System.out.println("Invalid input.");
            }
        }
    }
}
