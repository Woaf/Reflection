/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * The program uses jdbc:derby @ localhot:1527 with username woaf and psw 123
 */
package reflection.main;

import base.Block;
import evo.Tree;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author woaf
 */
public class Main {
    
    private static final String ReflectionDBSource = "ReflectionPU";
    private static final String AgentDBSource = "AgentPU";

    private static void fillDB() {

        List<Object> objects = new ArrayList<>();

        Block b = new Block(10, 20, 15, true, true);
        Block b2 = new Block(40, 20, 30, false, false);
        Tree t = new Tree();

        objects.add(b);
        objects.add(b2);
        objects.add(t);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(ReflectionDBSource);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (Object object : objects) {
            em.persist(object);
        }

        em.getTransaction().commit();
        em.close();
    }

    private static String toCamelCase(String input) {

        StringBuilder sb = new StringBuilder();
        sb.append(input.toLowerCase());
        sb.deleteCharAt(0);
        sb.insert(0, input.substring(0, 1).toUpperCase(), 0, 1);
        return sb.toString();
    }

    private static List<String> setupDatabaseConnection() {

        Connection con;
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = "//localhost:1527/Reflection;user=woaf;password=123";
        String connectionURL = "jdbc:derby:" + dbName;

        List<String> tableNameList = new ArrayList<>();

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(connectionURL);
            ResultSet sets = con.getMetaData().getTables(null, "WOAF", "%", null);
            while (sets.next()) {
                if (!sets.getString(3).equals("SEQUENCE")) {
                    tableNameList.add(sets.getString(3));
                }
                //System.out.println(sets.getString(3));
            }
            con.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tableNameList;
    }

    private static String createQueryString(String tableName) {
        return "SELECT a FROM " + tableName + " a";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        
        fillDB();

        List<ClassWrapper> resultWrappers = extractTables("base.");
        List<ClassWrapper> resultWrappers2 = extractTables("evo.");
        
        
        Agent a = new Agent();
        a.addWrappers(resultWrappers);
        a.addWrappers(resultWrappers2);
        for(int i = 0; i < a.getKnownClasses_().size(); i++){
            System.out.println("Constructors:");
            a.getKnownClasses_().get(i).getClass_constructors().forEach(System.out::println);
            System.out.println("Fileds:");
            a.getKnownClasses_().get(i).getClass_fields().forEach(System.out::println);
            System.out.println("Methods:");
            a.getKnownClasses_().get(i).getClass_methods().forEach(System.out::println);
            System.out.println("Evolutionary object?: " + a.getKnownClasses_().get(i).isEvolutionary());
        }
        
        System.out.println("EXPOSE:");
        a.exposeObject();
        
        /*
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        /**
         * note: We will deal with persisting the agents into a database later on
         *
        emf = Persistence.createEntityManagerFactory(AgentDBSource);
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(a);
        em.getTransaction().commit();
        em.close();
        */

    }

    private static List<ClassWrapper> extractTables(String path) {
        List<String> tables = setupDatabaseConnection()
                .stream()
                .map(s -> toCamelCase(s))
                .collect(Collectors.toList());
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(ReflectionDBSource);
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery(createQueryString(tables.get(0)));
        List<ClassWrapper> resultWrappers = new ArrayList<>();
        tables.forEach((name) -> {
            try {
                Class<?> arbitraryClass = Class.forName(path + name);
                ClassWrapper classWrapper = new ClassWrapper();
                classWrapper.setClass_constructors(Arrays.asList(arbitraryClass.getDeclaredConstructors()));
                
                if(path.equals("base.")){
                    classWrapper.setEvolutionary(false);
                    Object o = classWrapper.getClass_constructors().get(0).newInstance();
                    classWrapper.setClass_fields(Arrays.asList(o.getClass().getDeclaredFields()));
                    classWrapper.setClass_methods(Arrays.asList(o.getClass().getDeclaredMethods()));
                }
                else {
                    classWrapper.setEvolutionary(true);
                }

                resultWrappers.add(classWrapper);

            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.WARNING, null, ex);
            } catch (InstantiationException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return resultWrappers;
    }
}
