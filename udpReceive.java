import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class udpReceive implements Runnable
{
    // constructor so the connection can run in a thread in the actual program
    DatagramSocket dsReceive;
	byte[] receive;
	DatagramPacket DpReceive;
	udpSend sender; // reference to the sender to update the action feed when we receive a message
	Gui gui; // reference to the GUI to update the action feed
	
	udpReceive(Gui gui, udpSend sender)
    {
		this.gui = gui;
		this.sender = sender;
		setupSocket(); // Initialize socket and buffer
    }

	udpReceive()
    {
		this.gui = null; // No GUI
		setupSocket(); // Initialize socket and buffer
    }

    public void setGui(Gui g)
	{
		this.gui = g;
	}

	public void setupSocket() 
	{
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
	}

	@Override
    public void run()
    {
	    if (dsReceive == null)
	    {
		    System.err.println("Receive socket is null. Thread Exiting.");
		    return;
	    }
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

				// Update the GUI action feed with the received message
				if (gui != null) 
				{
					gui.consoleLog("Received: " + receivedString);
				}

				// parse received message, broadcast player that was hit
				if (receivedString.contains(":"))
				{
					String[] parts = receivedString.split(":");

                    // python script sends "AttackerID:VictimID"
                    String victimId = parts[1]; 

                    // BROADCAST: Send the victim's equipment ID back to port 7500
                    if (sender != null) 
					{
                        sender.send(victimId);
                    }

                    // Log to GUI for visibility
                    if (gui != null) 
					{
                        gui.consoleLog("Hit Detected! Broadcasted Victim ID: " + victimId);
                    }
				}

				// Exit the server if the client sends "bye" not case sensitive
				if (receivedString.equalsIgnoreCase("bye"))
				{
					System.out.println("Client sent bye.....EXITING");
					break;
				}
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
