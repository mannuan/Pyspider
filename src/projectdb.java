import java.sql.*;

public class projectdb {

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

//            statement.executeUpdate("drop table if exists person");
//            statement.executeUpdate("create table person (id integer, name string)");
//            statement.executeUpdate("insert into person values(1, 'leo')");
//            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("SELECT * FROM `projectdb`");
            while(rs.next())
            {
                // read the result set
                System.out.print("name = " + rs.getString("name")+"\t");
                System.out.print("group = " + rs.getInt("group")+"\t");
                System.out.print("status = " + rs.getString("status")+"\t");
                System.out.print("script = " + rs.getString("script")+"\t");
                System.out.print("comments = " + rs.getString("comments")+"\t");
                System.out.print("rate = " + rs.getInt("rate")+"\t");
                System.out.print("burst = " + rs.getInt("burst")+"\t");
                System.out.print("updatetime = " + rs.getString("updatetime")+"\t");
                System.out.println();
            }
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
