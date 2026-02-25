import java.sql.SQLException;

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

    public void addPlayer(int id, String codename, int hwId)
    {
        // check if player is already in the database to avoid duplicates
        if (!database.checkForId(id))
        {
            database.insertEntry(id, codename);
            System.out.println("Saved new player: " + codename + " with player id: " + id + " and with hardware id: " + hwId);
        }
        else
        {
            database.updateEntry(id, codename);
            System.out.println("Player with player id " + id + " already exists in the database. Changed codename to " + codename);
        }
    }

    public void run()
    {
        // this method is just to keep the main thread alive, since the connection threads, gui, and database are running in the background
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
	    Laser laser = new Laser();
        // initialize laser and gui
        // also initializes the sending and receiving threads, and the database connection via the Laser constructor
        // Testing initializing network objects here
    	udpSend sender = new udpSend();
	    udpReceive receiver = new udpReceive(null, sender); // pass sender to receiver so it can update the action feed when it receives a message

        laser.gui = new Gui(laser, sender, receiver);
	    laser.gui.setVisible(true);

        laser.run();
    }
}
