package org.tandembrowsing.io.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.tandembrowsing.model.VirtualScreen;
import org.tandembrowsing.state.StateMachineSession;

public class DBUtil {
	private static Logger logger = Logger.getLogger("org.tandembrowsing.io.db");
	private static final String INSERT_VIRTUALSCREEN = "insert into virtualscreens (session, id, resource, browser, width, height, xPosition, yPosition, zIndex, border, resizable) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_VIRTUALSCREEN = "update virtualscreens set resource = ?, browser = ?, width = ?, height = ?, xPosition = ?, yPosition = ?, zIndex = ?, border = ?, resizable = ? where session = ? and id = ?";
	private static final String DELETE_VIRTUALSCREENS = "delete from virtualscreens where session = ?";
	private static final String DELETE_STATEMACHINES = "delete from statemachines where session = ?";
	private static final String LOAD_VIRTUALSCREENS = "select id, resource, browser, width, height, xPosition, yPosition, zIndex, border, resizable from virtualscreens where session = ? order by insertion_order";
	private static final String READ_STATE = "select state from statemachines where session = ?";	
	private static final String GET_STATEMACHINES = "select session, url, state from statemachines";
	private static final String SET_STATEMACHINE = "INSERT INTO statemachines (session, url) VALUES (?,?) ON DUPLICATE KEY UPDATE url=?;";
	private static final String SET_STATE = "update statemachines set state = ? where session = ?";
	private static final String INSERT_LOG = "insert into log (message, timecol) values (?, ?)";
	private static final String GET_SESSIONS = "select session from statemachines";
	
	public static Connection getConnection() throws NamingException, SQLException {
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		DataSource ds = (DataSource) envCtx.lookup("jdbc/tandembrowsing");
		return ds.getConnection();
	}
	
	private static void closeConnection(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Couldn't close db connection.", e);
		}		
	}

	private static void closePreparedStatement(PreparedStatement pstmt) {
		try {
			if (pstmt != null)
				pstmt.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Couldn't close db resources.", e);
		}
	}
	
	private static void closeStatement(Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Couldn't close db resources.", e);
		}
	}
	
	public static void addVirtualScreen(String smSession, VirtualScreen cell) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(INSERT_VIRTUALSCREEN);
			pstmt.setString(1, smSession);
			pstmt.setString(2, cell.getId());
        	pstmt.setString(3, cell.getResource()); 
        	pstmt.setString(4, cell.getBrowser()); 
        	pstmt.setFloat(5, cell.getWidth());
        	pstmt.setFloat(6, cell.getHeight());
        	pstmt.setFloat(7, cell.getXPosition());
        	pstmt.setFloat(8, cell.getYPosition());
        	pstmt.setInt(9, cell.getZIndex());
        	pstmt.setFloat(10, cell.getBorder());
        	pstmt.setBoolean(11, cell.isResizable());
        	pstmt.executeUpdate();      
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}
	
	public static void load(String smSession, Map <String, VirtualScreen>cells) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(LOAD_VIRTUALSCREENS);
			pstmt.setString(1, smSession);
			ResultSet rset = pstmt.executeQuery();
			while(rset.next()) {
				String id = rset.getString(1);
				cells.put(id, new VirtualScreen(id, rset.getString(2), rset.getString(3), rset.getFloat(4), rset.getFloat(5), rset.getFloat(6), rset.getFloat(7), rset.getInt(8), rset.getFloat(9), rset.getBoolean(10)));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}
	
	public static void removeVirtualScreens(String smSession) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(DELETE_VIRTUALSCREENS);
			pstmt.setString(1, smSession);		
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}

	public static void modifyVirtualScreen(String smSession, VirtualScreen cell) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(UPDATE_VIRTUALSCREEN);
        	pstmt.setString(1, cell.getResource()); 
        	pstmt.setString(2, cell.getBrowser());
        	pstmt.setFloat(3, cell.getWidth());
        	pstmt.setFloat(4, cell.getHeight());
        	pstmt.setFloat(5, cell.getXPosition());
        	pstmt.setFloat(6, cell.getYPosition());
        	pstmt.setFloat(7, cell.getZIndex());
        	pstmt.setFloat(8, cell.getBorder());
			pstmt.setBoolean(9, cell.isResizable());
			pstmt.setString(10, smSession);
			pstmt.setString(11, cell.getId());
        	pstmt.executeUpdate();      
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Cell insert to db failed. Inmemory operation only.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}

	public static String readState(String smSession) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String state = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(READ_STATE);
        	pstmt.setString(1, smSession);    
			ResultSet rset = pstmt.executeQuery();
			if(rset.next()) 
				state = rset.getString(1);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
		return state;
	}
	

	public static void setState(String smSession, String state) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SET_STATE);
        	pstmt.setString(1, state); 
        	pstmt.setString(2, smSession); 
        	pstmt.executeUpdate();   
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}
	

	public static void store(String smSession, Map<String, VirtualScreen> cells) {
		removeVirtualScreens(smSession);
		Collection <VirtualScreen> values = cells.values();
		for(VirtualScreen i : values) {
			addVirtualScreen(smSession, i);
		}
	}

	public static void log(String message, long millis) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(INSERT_LOG);
        	pstmt.setString(1, message); 
        	pstmt.setLong(2, millis);
        	pstmt.executeUpdate();   
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Log failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Log failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}		
	}



	public static void setStateMachine(String smSession, String url) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SET_STATEMACHINE);
			pstmt.setString(1, smSession);
        	pstmt.setString(2, url);
        	pstmt.setString(3, url);
        	pstmt.executeUpdate();   
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Storing statemachine into db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Storing statemachine into db failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
	}
	
	public static void getStateMachines(Map <String, StateMachineSession> sessions) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(GET_STATEMACHINES);
			while(rset.next()) {
				StateMachineSession session = new StateMachineSession(rset.getString(1), rset.getString(2), rset.getString(3));
				sessions.put(rset.getString(1), session);
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching statemachine from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching statemachine from db failed.", e);
		} finally {
			closeStatement(stmt);
			closeConnection(conn);
		}
	}

	public static void removeSession(String smSession) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(DELETE_STATEMACHINES);
			pstmt.setString(1, smSession);
			pstmt.executeUpdate();			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching state from db failed.", e);
		} finally {
			closePreparedStatement(pstmt);
			closeConnection(conn);
		}
		removeVirtualScreens(smSession);
	}

	public static void getRecoverySessions(Set<String> recoverySessions) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(GET_SESSIONS);
			while(rset.next()) {
				recoverySessions.add(rset.getString(1));
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Fetching statemachine from db failed.", e);
		} catch (NamingException e) {
			logger.log(Level.SEVERE, "Fetching statemachine from db failed.", e);
		} finally {
			closeStatement(stmt);
			closeConnection(conn);
		}
	}


	

}

