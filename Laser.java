import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.IOException;

public class Laser
{
    // create objects for database, controller, view
    // Runnable send;
    // Runnable receive;
    // Thread sendThread;
    // Thread receiveThread;
    Database database;
    Gui gui;

    public Laser()
    {
        // // initialize sending and receiving threads
        // send = new udpSend();
        // receive = new udpReceive();
        // sendThread = new Thread(send);
        // receiveThread = new Thread(receive);
        // initialize database connection
        try 
        {
            database = new Database();
        }
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
        }
        // sendThread.start();
        // receiveThread.start();
    }

    public void run()
    {
        // this method is just to keep the main thread alive, since the connection threads and gui are running in the background
        while (true)
        {
            try 
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }

    // main method to run the program
    public static void main(String[] args)
    {
        // initialize laser and gui
        // also initializes the sending and receiving threads, and the database connection via the Laser constructor
        Laser laser = new Laser();
        laser.gui = new Gui(laser);
        laser.run();
    }
}
