// Java program to illustrate Server side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class udpReceive implements Runnable
{
    // Constructor so the connection can run in a thread in the actual program
    // might need to pass in the database (code controlling it), view, and controller to update the scores and action feed
    udpReceive()
    {
        // possibly need the below variables passed in as well
        // d = data;
        // v = view;
        // c = controller;

        // Step 1 : Create a socket to listen at port 7501
		DatagramSocket dsReceive = new DatagramSocket(7501);
		byte[] receive = new byte[65535];
        DatagramPacket DpReceive = null;
    }

    public void run()
    {
		while (true)
		{
			// Step 2 : create a DatgramPacket to receive the data.
			DpReceive = new DatagramPacket(receive, receive.length);

			// Step 3 : revieve the data in byte buffer.
			dsReceive.receive(DpReceive);
            // for now print data to console, will need to split the data and update scores and action feed in actual program
			System.out.println("Client:-" + data(receive));


            // FIX
            // Need code to split data into appropriate parts and update scores and action feed in actual program
            // only do this if we have started the game


			// Exit the server if the client sends "bye"
			if (data(receive).toString().equals("bye"))
			{
				System.out.println("Client sent bye.....EXITING");
				break;
			}

			// Clear the buffer after every message.
			receive = new byte[65535];
		}
	}

	// A utility method to convert the byte array
	// data into a string representation.
	public static StringBuilder data(byte[] a)
	{
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0)
		{
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}
}
