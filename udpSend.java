// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.net.SocketException;
import java.net.UnknownHostException;

// Client class to send the data to the server, implements runnable to run as a thread inside main program
public class udpSend implements Runnable
{
    // Need to be able to pass in internet address
    // might need to pass in the database (code controlling it), view, and controller to update the scores and action feed
	Scanner sc;
	DatagramSocket ds;
	InetAddress ip;
    udpSend()
    {
		try
		{
			sc = new Scanner(System.in);
			// Step 1:Create the socket object for
			// carrying the data
			ds = new DatagramSocket();
			ip = InetAddress.getLocalHost();
		}
		//two possible exceptions
		catch (SocketException | UnknownHostException e)
		{
			e.printStackTrace();
		}

        // possibly need the below variables passed in as well
        // d = data;
        // v = view;
        // c = controller;
    }


	@Override
    public void run()
    {
		System.out.println("Thread started; type 'bye' to exit.");
		// loop while user not enters "bye"
		while (true)
		{
			try
			{
				String inp = sc.nextLine();
				// convert the String input into the byte array.
				byte[] buf = inp.getBytes();

				// Step 2 : Create the datagramPacket for sending
				// the data.
				DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 7500);

				// Step 3 : invoke the send call to actually send
				// the data.
				ds.send(DpSend);

				// for testing -- send input
				System.out.println("Sent: " + inp);

				// break the loop if user enters "bye"
				if (inp.equalsIgnoreCase("bye"))
				{
					System.out.println("Closing sending thread.");
					break;
				}
			}
			catch (IOException e)
			{
				System.err.println("Error sending packet: " + e.getMessage());
				break;
			}
    	}
		//cleanup :)
		if (ds != null && !ds.isClosed())
		{
			ds.close();
		}
		System.out.println("Sender thread closed");

	}
}
