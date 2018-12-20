/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * The program uses jdbc:derby @ localhot:1527 with username woaf and psw 123
 */
package reflection.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author woaf
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = "//localhost:1527/Reflection;user=woaf;password=123";
        String connectionURL = "jdbc:derby:"+dbName;
        
        Connection con;
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(connectionURL);
            ResultSet sets = con.getMetaData().getTables(null, "WOAF", "%", null);
            while(sets.next())
            {
                System.out.println(sets.getString(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        
    }
    
}
