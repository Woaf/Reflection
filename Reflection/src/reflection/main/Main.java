/*
 * Copyright (C) 2019 Balint Fazekas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This program uses jdbc:derby @ localhot:1527 with username woaf and psw 123
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
 * @author Woaf
 */
public class Main {
    
    private static final String ReflectionDBSource = "ReflectionPU";
    private static final String AgentDBSource = "AgentPU";

    /**
     * We fill up the object space using this method
     */
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

    /**
     * This is a simple function that turns any word into such a format,
     * where the first letter is capitalized and the rest is not
     * @param input The string we with to modify.
     * @return The modified name.
     */
    private static String toCamelCase(String input) {

        StringBuilder sb = new StringBuilder();
        sb.append(input.toLowerCase());
        sb.deleteCharAt(0);
        sb.insert(0, input.substring(0, 1).toUpperCase(), 0, 1);
        return sb.toString();
    }

    /**
     * This function is used to set up the database connection, 
     * and return all the relevant table names in the database.
     * @return List of tables in the database that are user created.
     */
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
            }
            con.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tableNameList;
    }

    /**
     * Takes in a name of a table in the database, and creates an SQL query that
     * obtains all entries in that table.
     * @param tableName
     * @return Selection SQL query of the given table name.
     */
    private static String createQueryString(String tableName) {
        return "SELECT a FROM " + tableName + " a";
    }

    /**
     * The command line arguments are not yet used for this program.
     * On another note, the exceptions should be handled where they could 
     * potentially occur, instead of being thrown altogether to the main function.
     * @param args the command line arguments
     */
    public static void main(String[] args) 
            throws InstantiationException, 
                    IllegalAccessException, 
                    IllegalArgumentException, 
                    InvocationTargetException,
                    InterruptedException {

        /**
         * We first fill up the database, that represents the object space
         * of our model.
         */
        fillDB();

        /**
         * Then, we extract all of the objects that are present in the database.
         * Even though it is not explicitly implemented in this program, but one 
         * instance of each object in the database is enough for the agent to 
         * extract every piece of information about the object class itself.
         * 
         * The following lists represent the objects that are present in the base
         * object, and that are present in the evolutionary objects. 
         * 
         * These two lists altogether make up the representational table
         */
        List<ClassWrapper> resultWrappers = extractTables("base.");
        List<ClassWrapper> resultWrappers2 = extractTables("evo.");
        
        /**
         * We create our agent, which will gain knowledge about the environmental objects 
         * in an incremental fashion.
        */
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
        
        /**
         * The exploration of the object properties by the agents are looped forever.
         * 
         * Here is where the combination of the known objects happen.
         */
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
    
    /**
     * This method is responsible for combining two ClassWrapper objects. The combination 
     * includes the names of both classes, and all the fields and all methods of
     * both objects.
     * @param first
     * @param second
     * @return A ClassWrapper composed of the combination of the two input ClassWrapper 
     * parameters.
     */
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

    /**
     * Returns the list of ClassWrappers that are created from exploring the
     * tables in the database. Each class wrapper stores a class that is defined
     * in the object space somewhere. 
     * @param path The path to the source file of an object in the project.
     * @return List of ClassWrappers that the agents can use as "knowledge" base.
     */
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
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Class not found.", ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Cannot instantiate object.", ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Cannot access object members.", ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Wrong number of arguments given to the constructor.", ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Cannot invocate object.", ex);
            }
        });
        return resultWrappers;
    }
    
    /**
     * 
     * @param className Specifies which object is being observed.
     * @param type Specifies the return type that we want to find a matching function for.
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public static Method returnMethod(String className, String type) 
            throws InstantiationException, 
                    IllegalAccessException, 
                    IllegalArgumentException, 
                    InvocationTargetException {
        try {
            
            System.out.println("I am searching for class "+ className +", that has a method of return type of " + type);
            
            /**
             * We create a constructor the the class that's name was given as a
             * parameter, and get its constructors.
             */
            Class<?> arbitraryClass = Class.forName(className);
            ClassWrapper classWrapper = new ClassWrapper();
            classWrapper.setClass_constructors(Arrays.asList(arbitraryClass.getDeclaredConstructors()));
            
            /**
             * Using the getEmptyConstructor, we make sure that we are creating the 
             * observed object with its default, empty constructor. 
             * 
             * The purpose of this is that when extracting the constructors into 
             * a list, the first element might not be the empty constructor, and 
             * hence we have to find it.
             * 
             * Also, the empty constructor is used in this case, because we are not 
             * particularly interested in the values of the data members of the 
             * object, but only its functions. And, using this method, we do not
             * have to worry about finding parameters for the non-empty constructors.
             */
            Object o = getEmptyConstructor(classWrapper.getClass_constructors()).newInstance();
            classWrapper.setClass_fields(Arrays.asList(o.getClass().getDeclaredFields()));
            classWrapper.setClass_methods(Arrays.asList(o.getClass().getDeclaredMethods()));
            
            List<Method> localMethodList = new ArrayList<>();
            
            /**
             * A list of methods is extracted from that object, where the return 
             * type of the method matches the type we entered as a parameter.
             */
            localMethodList = classWrapper
                    .getClass_methods()
                    .stream()
                    .filter(method -> method.getReturnType().getName().equals(type))
                    .collect(Collectors.toList());
            
            /**
             * This list could be empty however, therefore we must only return a 
             * valid value when the size of the list is at least 1.
             */
            if(localMethodList.size() > 0)
            {
                /**
                 * If the method list with the specified return type is not empty, 
                 * then we randomly choose a method, and return it.
                 */
                Random rnd = new Random();
                return localMethodList.get(rnd.nextInt(localMethodList.size()));
            } else {
                /**
                 * Otherwise, we return with a null.
                 */
                return null;
            }

        } catch (ClassNotFoundException ex) {
            System.err.println("Class could not be found among the evolutionary objects.");
        }
        return null;
    }
    
    /**
     * This method is responsible for finding an empty constructor of an existing 
     * object type.
     * @param constructors The list of constructors that are being observed.
     * @return An empty constructor of an object type.
     */
    private static Constructor<?> getEmptyConstructor(List<Constructor<?>> constructors){
        Optional<Constructor<?>> constr = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst();
        if(constr.isPresent()){
            return constr.get();
        } else {
            return null;
        }
    }
}
