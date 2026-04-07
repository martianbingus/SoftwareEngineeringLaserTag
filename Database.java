import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// socket factory allows for the gui to connect over a local connection to avoid issues that occurred with some virtual machines receiving a password authentication error
import org.newsclub.net.unix.AFUNIXSocketFactory;
import java.util.Properties;

public class Database {
    private Connection connection;

    // you'll need to use try-catch when you call Database(), can't write the constructor without a thrown exception
    Database() throws SQLException
    {
		try
       	{
        	// explicitly load the driver class
        	Class.forName("org.postgresql.Driver");
    	}
       	catch (ClassNotFoundException e)
       	{
        	System.err.println("PostgreSQL JDBC Driver not found. Add the JAR to your classpath!");
        	e.printStackTrace();
    	}
        // getConnection(connection string, properties object) constructor used to be able to use the unix socket
		Properties props = new Properties();
		props.setProperty("user", "student");
		props.setProperty("password", "student");
		props.put("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
		props.put("socketFactoryArg", "/var/run/postgresql/.s.PGSQL.5432");
		this.connection = DriverManager.getConnection("jdbc:postgresql://localhost/photon", props);
    }

    //verifies that a given id is listed on the database
    public boolean checkForId(int id)
    {
        String qs = "SELECT id, codename FROM players WHERE id = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        { 
            query.setInt(1, id);
            try (ResultSet r = query.executeQuery())
            {
                return r.next();
            }
        }
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
            return false;
        }
    }

    // takes a player id and returns the corresponding codename on the database, returns null if id does not exist
    // might use queryId() first to make sure the id is there
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
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
            return null;
        }
        
    }

    // pass in two values and add them to the database
    public void insertEntry(int id, String codename)
    {
        
        String qs = "INSERT INTO players (id, codename) VALUES (?, ?)";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setInt(1, id);
            query.setString(2, codename);
            query.executeUpdate();
        }
        catch (SQLException UwU) 
        {
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
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
        }
    }

    public void deleteEntryByCodename(String codename)
    {
        String qs = "DELETE FROM players where codename = ?";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            query.setString(1, codename);
            query.executeUpdate();
        } 
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
        }
    }

    public void updateEntry(int id, String codeName)
    {
        this.deleteEntryById(id);
        this.insertEntry(id, codeName);
    }

    public void clear()
    {
        String qs = "TRUNCATE FROM players";
        try (PreparedStatement query = connection.prepareStatement(qs))
        {
            //query.setString(1, codename);
            query.executeUpdate();
        }
        catch (SQLException UwU) 
        {
            UwU.printStackTrace();
        }
    }
}


