// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

// Client class to send the data to the server, implements runnable to run as a thread inside main program
public class udpSend implements Runnable
{
    // Need to be able to pass in internet address
    // might need to pass in the database (code controlling it), view, and controller to update the scores and action feed
    udpSend()
    {
        // possibly need the below variables passed in as well
        // d = data;
        // v = view;
        // c = controller;

		Scanner sc = new Scanner(System.in);

		// Step 1:Create the socket object for
		// carrying the data.
		DatagramSocket ds = new DatagramSocket();

		InetAddress ip = InetAddress.getLocalHost();
		byte buf[] = null;
    }

    public void run()
    {
		// loop while user not enters "bye"
		while (true)
		{
			String inp = sc.nextLine();

			// convert the String input into the byte array.
			buf = inp.getBytes();

			// Step 2 : Create the datagramPacket for sending
			// the data.
			DatagramPacket DpSend =
				new DatagramPacket(buf, buf.length, ip, 7500);

			// Step 3 : invoke the send call to actually send
			// the data.
			ds.send(DpSend);
			// for testing
			System.out.println("Sent data");

			// break the loop if user enters "bye"
			if (inp.equals("bye"))
				break;
		}
    }
}
