// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//								DATABASECONNECTION
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

package jeggybot.util;

import java.sql.*;

public class DatabaseConnection
{
	// ********************************************************************************
    //          DECLARATIONS
    // ********************************************************************************
	
	private Connection connection;			// database connection
	
	// ********************************************************************************
    //          CONSTRUCTOR
    // ********************************************************************************

	public DatabaseConnection()
	{
		// load jdbc mysql driver
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (java.lang.ClassNotFoundException e) {
		}
	}
	
    // ********************************************************************************
    //          PUBLIC METHODS
    // ********************************************************************************

    public void connect(String address, String username, String password, String name) throws SQLException
    {
		// set database driver connection string
		String driverPath = "jdbc:mysql://" + address + "/" + name;
		// open connection to database
		connection = DriverManager.getConnection(driverPath, username, password);
    }

    public void close() throws SQLException
    {
    	if (connection != null) {
			// close the database connection
			connection.close();
    	}
    }

    public ResultSet query(String query) throws SQLException
    {
    	// create query statement
    	Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    	// execute query and store result on resultset
    	ResultSet rs = stmt.executeQuery(query);
    	return rs;
    }

    public int update(String query) throws SQLException
    {
    	// create update statement
    	Statement stmt = connection.createStatement();
    	// execute update on database
    	return stmt.executeUpdate(query);
    }
    
    public int[] update(String queries[]) throws SQLException
    {
    	// create update statement
    	Statement stmt = connection.createStatement();
    	// loop through each insert/update query and add it to stmt
    	for (String query: queries) {
    		stmt.addBatch(query);
    	}
    	// execute queries in a single batch
    	return stmt.executeBatch();
    }
	
	public PreparedStatement prepareStatement(String query) throws SQLException
	{
		return connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	public int getRowCount(ResultSet rs) throws SQLException
	{
		// go to last row in resultset
		rs.last();
		int count = rs.getRow();
		// return to first entry
		rs.beforeFirst();
		return count;
	}
}