/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.gis.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bruce Schubert
 */
public class SqlServerConnection {

    public Connection connectWithDriverManager(String connectionUrl) {
        try {
            // This technique will create a database connection using the 
            // first available driver in the list of drivers that can 
            // successfully connect with the given URL.
            
            // A call to forName("X") causes the class named X to be initialized.
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection con = DriverManager.getConnection(connectionUrl);
            return con;
        }
        catch (SQLException ex) {
            Logger.getLogger(SqlServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(SqlServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
