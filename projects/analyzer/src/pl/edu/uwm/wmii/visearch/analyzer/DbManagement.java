package pl.edu.uwm.wmii.visearch.analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.*;



public class DbManagement {
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	
	public DbManagement(String dbUrl, String dbUser, String dbPass) {
		this.dbUrl = dbUrl;
		this.dbUser = dbUser;
		this.dbPass = dbPass;
	}
	
	private Connection conn = null;

	public void connect() throws SQLException, ClassNotFoundException {
		if (conn == null) {
			//Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:"+dbUrl, dbUser, dbPass);
		}
	}
	
	public  List<String> getImagePaths(ExtractionMethod method)
	{
		try{
			Statement statement= conn.createStatement();
			String descr = method.toString(); 
			ResultSet resultSet = statement.executeQuery("SELECT FileName,FileDirectory FROM Images " +
					"WHERE FileName NOT IN (SELECT FileName FROM ImageDescriptors WHERE descriptor ='"
					+descr+"') LIMIT 10");

			List<String> paths = new ArrayList<String>(11);

			while (resultSet.next()) {  

				paths.add(resultSet.getString("FileDirectory")+resultSet.getString("FileName"));

			}
			return paths;
		}
		catch (SQLException e) {
			System.out.println(e);
		} 
		return null;
	}
	
	
	public void InsertDescriptor(String fileName, ExtractionMethod method, String descriptorPath ){

		try {
			Statement statement = conn.createStatement();

			String descr = method.toString(); 

			statement.executeUpdate("INSERT INTO ImageDescriptors VALUES('"+fileName+"','" +
					descr+"',NOW(),'"+descriptorPath+"','')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disconnect() throws SQLException {
		conn.close();
	}

}
