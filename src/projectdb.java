import java.sql.*;

public class projectdb {

    public static String project = "test127";

    public static void main(String[] args) throws ClassNotFoundException
    {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s/data/project.db",
                    System.getProperty("user.home")));
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate(String.format("delete from projectdb where name='%s';",project));

            // create a database connection
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s/data/task.db",
                            System.getProperty("user.home")));
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate(String.format("drop table taskdb_%s;",project));

            // create a database connection
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s/data/result.db",
                            System.getProperty("user.home")));
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate(String.format("drop table resultdb_%s;",project));

            System.out.println("ok");

        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException e)
            {
                // connection close failed.
                System.err.println(e);
            }
        }
    }

}
