package geneticEvolutionaryAlgorithm.database;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import com.mysql.cj.jdbc.MysqlDataSource;

import geneticEvolutionaryAlgorithm.entities.Artifact;
import geneticEvolutionaryAlgorithm.entities.Component;
import geneticEvolutionaryAlgorithm.utils.GEA_Result;

public class DbController {

	String path = "";
	Connection conn = null;
	MysqlDataSource dataSource = null;
	String username = null;
	String password = null;
	String serverName = null;
	String databaseName = null;

//	public DbController(String path) {
//		this.path = path;
//		conn = getConnection(path);
//		if (!this.isReady()) {
//			System.out.println("Null Connection");
//		} else {
//			boolean ok = beginRUTransaction();
//			if (!ok) {
//				this.closeConn();
//			}
//		}
//	}

	public DbController(String serverName, String databaseName, String username, String password) {
		conn = getConnection(serverName, databaseName, username, password);
		
		if (!this.isReady()) {
			System.out.println("Null Connection");
		} else {
			boolean ok = beginRUTransaction();
			if (!ok) {
				this.closeConn();
			}
		}
	}
	
	
	public boolean insertIndividualsToDatabase(String projectName, GEA_Result oldResult, GEA_Result newResult) throws Exception {

		boolean commit = true;
		commit = deletePreviousInsertsOfProject(projectName);
		if (!commit) {
			throw new Exception("Could not delete old values from database");
		}
		if (commit) {
			commit &= insertProject(projectName, oldResult, newResult);
		}
		if (commit) {
			commit &= insertPackages(projectName, oldResult, false);
		}
		if (commit) {
			commit &= insertPackages(projectName, newResult, true);
		}
		if (commit) {
			commit &= insertClasses(projectName, oldResult, false);
		}
		if (commit) {
			commit &= insertClasses(projectName, newResult, true);
		}

		if (!commit) {
			System.out.println("Error, could not do necessary Database actions");
			connRollBackAndClose();
			return false;
		}

		return this.connCommitAndClose();
	}

	private boolean insertClasses(String projectName, GEA_Result indv, boolean isNew) {
		try {
			Iterator<Entry<String, Artifact>> it = indv.getArtifactsIterator();
			while(it.hasNext()) {
				Entry<String, Artifact> e = it.next();
				Artifact art = e.getValue();

				//Checking if the class Component exists as an entry in the Database
				String comp = art.getComponent().getName();
				String query = "SELECT ID FROM gea_packages WHERE name=? AND isNew=? AND projectName=?";
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, comp);
				preparedStmt.setBoolean(2, isNew);
				preparedStmt.setString(3, projectName);
				ResultSet rs = preparedStmt.executeQuery();
				int count = 0;
				int id = -1;
				while (rs.next()) {
					count++;
					id = rs.getInt("ID");
				}
				if (count != 1 || id == -1) {
					return false;
				}

				//creating query to insert the class to the Database
				query = " insert into gea_classes (name, packageID, projectName, cohesion, coupling, isNew)"
						+ " values (?, ?, ?, ?, ?, ?)";
				preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, art.getName());
				preparedStmt.setInt(2, id);
				preparedStmt.setString(3, projectName);
				preparedStmt.setDouble(4, art.getCohesion());
				preparedStmt.setDouble(5, art.getCoupling());
				preparedStmt.setBoolean(6, isNew);
				preparedStmt.executeUpdate();
			}
			return true;
		} catch (Exception e) {
			System.out.println("Class insertion failed");
			return false;
		}
	}

	private boolean insertPackages(String projectName, GEA_Result indv, boolean isNew) {
		try {
			for (int i = 0; i < indv.getComponents().size(); i++) {
				String query = " insert into gea_packages (name, cohesion, coupling, projectName, isNew)"
						+ " values (?, ?, ?, ?, ?)";
				Component comp = indv.getComponents().get(i);
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1, comp.getName());
				preparedStmt.setDouble(2, comp.getCohesion());
				preparedStmt.setDouble(3, comp.getCoupling());
				preparedStmt.setString(4, projectName);
				preparedStmt.setBoolean(5, isNew);
				preparedStmt.executeUpdate();
			}
			return true;
		} catch (Exception e) {
			System.out.println("Package insertion failed");
			return false;
		}
	}

	private boolean insertProject(String projectName, GEA_Result indvOld, GEA_Result indvNew) {
		try {
			String query = " insert into gea_projects (name, coupling_old, cohesion_old, coupling_new, cohesion_new)"
					+ " values (?, ?, ?, ?, ?)";

			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, projectName);
			preparedStmt.setDouble(2, indvOld.getCoupling());
			preparedStmt.setDouble(3, indvOld.getCohesion());
			preparedStmt.setDouble(4, indvNew.getCoupling());
			preparedStmt.setDouble(5, indvNew.getCohesion());
			preparedStmt.executeUpdate();
			return true;
		} catch (Exception e) {
			System.out.println("Project insertion failed");
			return false;
		}
	}

	public boolean deletePreviousInsertsOfProject(String projectName) {
		if (projectName == null || projectName.isEmpty()) {
			throw new IllegalArgumentException("Invalid values. They must not be null, empty or blank");
		}
		if (conn == null) {
			System.out.println("Connection is null, something went wrong");
			return false;
		}
		try {
			String sql = "DELETE FROM gea_classes WHERE projectName=?";
			PreparedStatement prpStmt = conn.prepareStatement(sql);
			prpStmt.setString(1, projectName);
			prpStmt.executeUpdate();
			sql = "DELETE FROM gea_packages WHERE projectName=?";
			prpStmt = conn.prepareStatement(sql);
			prpStmt.setString(1, projectName);
			prpStmt.executeUpdate();
			sql = "DELETE FROM gea_projects WHERE Name=?";
			prpStmt = conn.prepareStatement(sql);
			prpStmt.setString(1, projectName);
			prpStmt.executeUpdate();
			return true;

		} catch (SQLException e) {
			System.out.println("Delete of previous data failed");
			return false;
		}
	}

	public void closeConn() {
		try {
			this.conn.close();
			this.conn = null;
		} catch (Exception e) {
			this.connRollBackAndClose();
		}

	}

	public boolean isReady() {
		if (conn != null) {
			return true;
		}
		return false;
	}

	private boolean beginRUTransaction() { // READ_UNCOMMITTED_SQL_TRANSACTION
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			return true;
		} catch (Exception e) {
			System.out.println("Could not start a Read_Unncommitted transaction");
			return false;
		}
	}

	private boolean connRollBackAndClose() {
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.rollback();
			conn.close();

			System.out.println("Rolling back transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("Could not roll back transaction");
			return false;
		}
	}

	private boolean connCommitAndClose() {
		if (!this.isReady()) {
			return false;
		}
		try {
			conn.commit();
			conn.close();

			System.out.println("\n\nCommiting transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("\n\nCould not commit transaction");
			connRollBackAndClose();
			return false;
		}
	}

	private Connection getConnection(String serverName, String databaseName, String username, String password) {

		boolean ok = false;

		ok = true;

		this.username = username;
		this.password = password;
		this.serverName = serverName;
		this.databaseName = databaseName;

		if (serverName == null || serverName.isEmpty() || 
				databaseName == null || databaseName.isEmpty()) {
			System.out.println("One or more of the Credentials given is null");
			return null;
		}


		// <

		String url = "jdbc:mysql://" + serverName + "/" + databaseName + "";

		System.out.println("\n\n\nConnecting database...");

		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");
			return connection;
		} catch (SQLException e) {
			System.out.println("Cannot connect the database!\n");
			return null;
		}
		// >
	}

	private Connection getConnection(String path) {

		if (path == null || path.isEmpty()) {
			return null;
		}

		boolean ok = false;

		try {
			ok = true;
			// BufferedReader reader = new BufferedReader(new FileReader(filename));
			// String line;

			File file = new File(path);

			if (!file.exists() || !file.isFile()) {
				return null;
			}

			Scanner input = new Scanner(new FileInputStream(file));

			boolean flag = input.hasNextLine();
			if (!input.hasNextLine()) {
				input.close();
				return null;
			}

			while (flag) {
				String line = input.nextLine();
				if (line.startsWith("username=")) {
					username = line.replaceFirst("username=", "");
					// username = line;
				} else if (line.startsWith("password=")) {
					password = line.replaceFirst("password=", "");
					// password = line;
				} else if (line.startsWith("serverName=")) {
					serverName = line.replaceFirst("serverName=", "");
					// serverName = line;
				} else if (line.startsWith("databaseName=")) {
					databaseName = line.replaceFirst("databaseName=", "");
					// databaseName = line;
					// "jdbc:mysql://"+serverName+"/"+line+ "?user=" +username + "&password=" +
					// password + "&useUnicode=true&characterEncoding=UTF-8";
				}
				flag = input.hasNextLine();
			}
			input.close();
			// if (username == null || password == null || serverName == null ||
			// databaseName == null) {
			if (serverName == null || databaseName == null) {

				ok = false;
			}
			if (!ok) {
				System.out.println("One or more of the Credentials given is null");
				return null;
			}

		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", path);
			return null;
		}

		// <

		String url = "jdbc:mysql://" + serverName + "/" + databaseName + "";

		System.out.println("\n\n\nConnecting database...");

		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");
			return connection;
		} catch (SQLException e) {
			System.out.println("Cannot connect the database!\n");
			return null;
		}
		// >

	}
}
