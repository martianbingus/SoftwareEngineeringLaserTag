import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

// client class to send the data to the server, implements runnable to run as a thread inside main program
public class udpSend implements Runnable
{
	Scanner sc;
	DatagramSocket ds;
	InetAddress ip;
	Gui gui; // reference to the GUI to update the action feed

	udpSend(Gui gui)
	{
        this.gui = gui;
        setupSocket();
    }

    udpSend() 
    {
        this.gui = null;
        setupSocket(); // initialize socket and scanner
        sc = new Scanner(System.in); // Init scanner only here
    }

	void setupSocket() 
	{
    	try
		{
			// create the socket object for carrying the data
			ds = new DatagramSocket();
			if (ip == null) 
			{
				ip = InetAddress.getByName("127.0.0.1");
			}
		}
		catch (SocketException | UnknownHostException e)
		{
			e.printStackTrace();
		}
    }

	public void send(String message) 
	{
		// game start code / game stop code
		if (message != null && (message.contains("221") || message.contains("202")))
		{
			if (message.contains("221"))
			{
				System.out.println("Game stop code sent to system.");
			}
			else if (message.contains("202"))
			{
				System.out.println("Game start code sent to system.");
			}
			try
			{
				byte[] buf = message.getBytes();
				DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 7500);
				ds.send(DpSend);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		// regular message
		else if (message != null)
		{
			try
			{
				byte[] buf = message.getBytes();
				DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 7500);
				ds.send(DpSend);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
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

				// Step 3 : invoke the send call to actually send the data.
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
		// cleanup :)
		if (ds != null && !ds.isClosed())
		{
			ds.close();
		}
		System.out.println("Sender thread closed");

	}

	public void setTargetIp(String ipAddress) 
	{
		try 
		{
			this.ip = InetAddress.getByName(ipAddress);
			System.out.println("Target IP set to: " + ipAddress);
		} 
		catch (UnknownHostException e) 
		{
			System.err.println("Invalid IP address: " + ipAddress);
			e.printStackTrace();
		}
	}
}

