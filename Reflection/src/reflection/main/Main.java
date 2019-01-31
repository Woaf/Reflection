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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
    public static void main(String[] args) 
            throws InstantiationException, 
                    IllegalAccessException, 
                    IllegalArgumentException, 
                    InvocationTargetException,
                    InterruptedException {

        
        fillDB();

        List<ClassWrapper> resultWrappers = extractTables("base.");
        List<ClassWrapper> resultWrappers2 = extractTables("evo.");
        
        
        Agent a = new Agent();
        a.addWrappers(resultWrappers);
        a.addWrappers(resultWrappers2);
        for(int i = 0; i < a.getKnownClasses_().size(); i++){
            a.getKnownClasses_().get(i).filterMethodName();
            /**
             * Keep these comments for debugging purposes
            System.out.println("Constructors:");
            a.getKnownClasses_().get(i).getClass_constructors().forEach(System.out::println);
            System.out.println("Fileds:");
            a.getKnownClasses_().get(i).getClass_fields().forEach(System.out::println);
            System.out.println("Methods:");
            a.getKnownClasses_().get(i).getClass_methods().forEach(System.out::println);
            System.out.println("Evolutionary object?: " + a.getKnownClasses_().get(i).isEvolutionary());
            */
        }
        
        while (true)
        {
            System.out.println("EXPOSE:");
            a.exposeObject();
            if(a.getLocalTypeOfInterest() != null)
            {
                Method m = returnMethod(a.getLocalClassOfInterest(), a.getLocalTypeOfInterest());
                if(m != null)
                {
                    System.out.println("I found the following method for you: " + m);
                    a.updateClassWrapperWithName("evo.Tree", m);
                    System.out.println(a.getKnownClasses_());
                }
            }
            
            int x = 0;
            int y = 0;
            Random rnd = new Random();
            while(x == y && a.getKnownClasses_().size() != 1)
            {
                x = rnd.nextInt(a.getKnownClasses_().size());
                y = rnd.nextInt(a.getKnownClasses_().size());
                
            }
            
            ClassWrapper firstObj = a.getKnownClasses_().get(x);
            ClassWrapper secondObj = a.getKnownClasses_().get(y);

            System.out.println("First: " + firstObj.getClass_method_names());
            System.out.println("Second:" + secondObj.getClass_method_names());
            
            String arbitraryMethodName = "";
            int itemIndex = rnd.nextInt(firstObj.getClass_constructors().size());
            int iterator = 0;
            for(String s : firstObj.getClass_method_names())
            {
                if(itemIndex == iterator)
                {
                    arbitraryMethodName = s;
                    break;
                }
            }
            
            System.out.println("Looking to match with: " + arbitraryMethodName);
            
            if(secondObj.getClass_method_names().contains(arbitraryMethodName))
            {
                System.out.println("SUCCESS!!");
                ClassWrapper c = combineObjects(firstObj, secondObj);
                System.out.println("NEW CLASS: " + c);
            }
            
            
            
            
            Thread.sleep(1000);
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
    
    private static ClassWrapper combineObjects(ClassWrapper first, ClassWrapper second)
    {
        ClassWrapper cw = new ClassWrapper();
        cw.setName(first.getName() + second.getName());
        cw.setClass_fields(first.getClass_fields());
        cw.getClass_fields().addAll(second.getClass_fields());
        cw.setClass_methods(first.getClass_methods());
        cw.getClass_methods().addAll(second.getClass_methods());
        
        return cw;
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
                classWrapper.setName(path+name);
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
    
    public static Method returnMethod(String className, String type) 
            throws InstantiationException, 
                    IllegalAccessException, 
                    IllegalArgumentException, 
                    InvocationTargetException {
        try {
            
            System.out.println("I am searching for class "+ className +", that has a method of return type of " + type);
            
            Class<?> arbitraryClass = Class.forName(className);
            ClassWrapper classWrapper = new ClassWrapper();
            classWrapper.setClass_constructors(Arrays.asList(arbitraryClass.getDeclaredConstructors()));
            
            Object o = getEmptyConstructor(classWrapper.getClass_constructors()).newInstance();
            classWrapper.setClass_fields(Arrays.asList(o.getClass().getDeclaredFields()));
            classWrapper.setClass_methods(Arrays.asList(o.getClass().getDeclaredMethods()));
            
            List<Method> localMethodList = new ArrayList<>();
            
            localMethodList = classWrapper
                    .getClass_methods()
                    .stream()
                    .filter(method -> method.getReturnType().getName().equals(type))
                    .collect(Collectors.toList());
            
            if(localMethodList.size() > 0)
            {
                Random rnd = new Random();
                return localMethodList.get(rnd.nextInt(localMethodList.size()));
            } else {
                return null;
            }

        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Class could not be found among the evolutionary objects.");
        }
        return null;
    }
    
    private static Constructor<?> getEmptyConstructor(List<Constructor<?>> constructors){
        Optional<Constructor<?>> constr = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst();
        if(constr.isPresent()){
            return constr.get();
        } else {
            return null;
        }
    }
}
