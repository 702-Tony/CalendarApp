/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import static calendarapp.CalendarApp.activeUser;
import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Anthony Adams
 */
public class DBConnection {
    /*
    To Delete from Table
    "DELETE FROM appointment WHERE appointmentId =";
    */
    // DB Connection variables
    private ResourceBundle rbdb = null;
    try {
      rbdb = ResourceBundle.getBundle("resources/dbconfig.properties");
    }
    private static final String databaseName = rbdb.getString("db.databaseName");
    private static final String username = rbdb.getString("db.username");
    private static final String password = rbdb.getString("db.password");
    private static final String dbURL = rbdb.getString("db.dbURL");
    private static final String driver = rbdb.getString("db.driver");
    public static Connection conn;
    public static void makeConnection() {
        // TODO Update with 10.7 Hint for try with resources
        try {
            Class.forName(driver);
            conn = (Connection) DriverManager.getConnection(dbURL, username, password);
            System.out.println("Connection Successful!");
        }
        catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
            System.out.println("Connection Failed.");
        }
    }
    public static void closeConnection() {
        try {
            conn.close();
            System.out.println("Connection Closed.");
        } catch (SQLException e) {
            System.out.println(e);
            System.out.println("FAILURE! Connection not closed.");
        }
    }
    public static boolean validateLogin(String user, String pass) {
        // using prepared statement to prevent SQL Injection attacks
        String stmt = "SELECT userId, passWord, userName FROM user where userName = '?';";
        String query = "SELECT userId, passWord, userName FROM user where userName = '" + user + "';";
        makeConnection();
        try {
            PreparedStatement selectStmt;
            selectStmt = conn.prepareStatement(stmt);
            ResultSet result = selectStmt.executeQuery(query);
            // move to next line if no data returned SQL Error is thrown
            result.next();
            String pwd = result.getString("passWord");
            if (pwd.equals(pass)) {
                System.out.println("Success!");
                // store active user data for reference later
                activeUser.setUser(result.getString("userName"), result.getInt("userId"));
                return true;
            } else {

                System.out.println("Login Failed!");
                return false;
            }
        } catch (SQLException e) {
            // Print error to console
            System.out.println("SQL ERROR!!! " + e);
        } finally {
            System.out.println("Checking for Close of connection");
            closeConnection();
        }
        return false;
    }

    public static ResultSet processQuery(String query) {
        // this should return a result set object so data can be processed for tables and such
        makeConnection();
        try {
            Statement queryStatement;
            queryStatement = conn.createStatement();
            ResultSet result = queryStatement.executeQuery(query);
            return result;
        } catch (SQLException e) {
            System.out.println("DBConnection.processQuery | " + e);
        }
        return null;
    }
    public static boolean processInput(String query, String statement) {
        // takes prepared statement and query as arguments to execute update to sql server
        makeConnection();
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(statement);
            preparedStatement.executeUpdate(query);
            System.out.println("Success!");
            return true;
        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION!!! \n" + e);
            return false;
        } finally {
            closeConnection();

        }
    }
}
