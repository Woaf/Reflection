/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * The program uses jdbc:derby @ localhot:1527 with username woaf and psw 123
 */
package reflection.main;

import base.Block;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author woaf
 */
public class Main {

    private static void fillDB() {
        
        List<Object> objects = new ArrayList<>();
        
        Block b = new Block(10, 20, 15, true, true);
        Block b2 = new Block(40, 20, 30, false, false);
       
        objects.add(b);
        objects.add(b2);
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReflectionPU");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        
        for (Object object : objects) {
            em.persist(object);
        }
        
        em.getTransaction().commit();
        em.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = "//localhost:1527/Reflection;user=woaf;password=123";
        String connectionURL = "jdbc:derby:" + dbName;
        
        Connection con;
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(connectionURL);
            ResultSet sets = con.getMetaData().getTables(null, "WOAF", "%", null);
            while(sets.next())
            {
                if(!sets.getString(3).equals("SEQUENCE"))
                    System.out.println(sets.getString(3));
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fillDB();
        
    }
    
}
