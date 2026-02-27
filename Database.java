import java.sql.DriverManager;
import java.sql.Connection;
//import java.sql.Driver;
//import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// below two line are  added to test forcing a connection over local vs the current TCP/IP connection using ipv4 or ipv6
import org.newsclub.net.unix.AFUNIXSocketFactory;
import java.util.Properties;

//developed by Bright Rupp, the one and only :3 UwU

public class Database {
    private Connection connection;

    //for dumb reasons, I couldn't handle the SQLException in the constructor, so you'll need to use try-catch when you call Database()
    Database() throws SQLException
    {
	try
       	{
        	// Explicitly load the driver class
        	Class.forName("org.postgresql.Driver");
    	}
       	catch (ClassNotFoundException e)
       	{
        	System.err.println("PostgreSQL JDBC Driver not found. Add the JAR to your classpath!");
        	e.printStackTrace();
    	}
	Properties props = new Properties();
	props.setProperty("user", "student");
	props.setProperty("password", "student");
	props.put("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
	props.put("socketFactoryArg", "/var/run/postgresql/.s.PGSQL.5432");
	this.connection = DriverManager.getConnection("jdbc:postgresql://localhost/photon", props);
        // this.connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/photon", "student", "student");
    }

    //verifies that a given id is listed on the database
    public boolean checkForId(int id)
    {
        String qs = "SELECT id, codename FROM players WHERE id = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        { //apparently try statements automatically close these resources when they're in parenthesis?
            query.setInt(1, id);
            try (ResultSet r = query.executeQuery())
            {
                return r.next();
            }
        }
        catch (SQLException UwU) {
            UwU.printStackTrace();
            return false;
        }
    }

    //takes a player id, returns the corresponding codename on the database
    //returns null if id does not exist
    //thus you might use queryId() first to make sure the id is there
    public String getCodename(int id) throws SQLException
    {
        
        String qs = "SELECT codename FROM players WHERE id = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setInt(1, id);
            try (ResultSet r = query.executeQuery())
            {
                if (r.next())
                {
                    return r.getString("codename");
                }
                else
                {
                    return null;
                }
            }
        }
        catch (SQLException UwU) {
            UwU.printStackTrace();
            return null;
        }
        
    }

    //pretty self-explanatory tbh, pass in two values and add them to the database lmfao
    public void insertEntry(int id, String codename)
    {
        
        String qs = "INSERT INTO players (id, codename) VALUES (?, ?)";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setInt(1, id);
            query.setString(2, codename);
            query.executeUpdate();
        }
        catch (SQLException UwU) {
            UwU.printStackTrace();
        }
    }

    //delete an entry using its id
    public void deleteEntryById(int id)
    {
        String qs = "DELETE FROM players WHERE id = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setInt(1, id);
            query.executeUpdate();
        }
        catch (SQLException UwU) {
            UwU.printStackTrace();
        }
    }

    //delete an entry using its codename (could technically delete multiple
    //  players at once if they share a codename but I added this anyway in
    //  case you found it necessary)
    public void deleteEntryByCodename(String codename)
    {
        String qs = "DELETE FROM players where codename = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setString(1, codename);
            query.executeUpdate();
        } 
        catch (SQLException UwU) {
            UwU.printStackTrace();
        }
    }

    public void updateEntry(int id, String codeName)
    {
        this.deleteEntryById(id);
        this.insertEntry(id, codeName);
    }
}


