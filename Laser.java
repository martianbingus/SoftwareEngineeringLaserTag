import java.sql.SQLException;

public class Laser
{
    Database database;
    Gui gui;

    public Laser()
    {
        try 
        {
            database = new Database();
        }
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
        }
    }

    public void addPlayer(int id, String codename, String hwId)
    {
        // check if player is already in the database to avoid duplicates
        if (!database.checkForId(id))
        {
            database.insertEntry(id, codename);
            System.out.println("Saved new player: " + codename + " with player id: " + id + " and with hardware id: " + hwId);
        }
        else
        {
            System.out.println("Player with player id " + id + " aleady exists in the database.");
        }
    }
    
    public String getCodename(int id)
    {
		String ret = null;
		try
		{
			ret = database.getCodename(id);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ret;
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
        // initialize laser, sending and receiving threads, database connection, and the gui
	    Laser laser = new Laser();
    	udpSend sender = new udpSend();
	    udpReceive receiver = new udpReceive(null, sender);

        laser.gui = new Gui(laser, sender, receiver);
	    laser.gui.setVisible(true);

        laser.run();
    }
}
