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
    DatagramSocket dsReceive;
	byte[] receive;
	DatagramPacket DpReceive;
	
	udpReceive()
    {
        // possibly need the below variables passed in as well
        // d = data;
        // v = view;
        // c = controller;

        // Step 1 : Create a socket to listen at port 7501
		try 
		{
			dsReceive = new DatagramSocket(7501);
			receive = new byte[65535];
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
        //DatagramPacket DpReceive = null;
    }

    public void run()
    {
		try
		{
			while (true)
			{
				// Step 2 : create a DatgramPacket to receive the data.
				DpReceive = new DatagramPacket(receive, receive.length);

				// Step 3 : revieve the data in byte buffer.
				dsReceive.receive(DpReceive);

				// extract string, trim any white space
				String receivedString = new String(DpReceive.getData(), 0, DpReceive.getLength()).trim();

				// for now print data to console, will need to split the data and update scores and action feed in actual program
				System.out.println("Client:-" + receivedString);


				// FIX
				// later need code to split data into appropriate parts and update scores and action feed in actual program
				// only do this if we have started the game


				// Exit the server if the client sends "bye" not case sensitive
				if (receivedString.equalsIgnoreCase("bye"))
				{
					System.out.println("Client sent bye.....EXITING");
					break;
				}

				// Clear the buffer after every message.
				// (matt) line commented, apparently in udp you dont need to clear buffer
				// receive = new byte[65535];
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (dsReceive != null && !dsReceive.isClosed())
			{
				dsReceive.close();
			}
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
