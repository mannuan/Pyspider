import java.sql.*;

public class taskdb {

    public static void main(String[] args) throws ClassNotFoundException
    {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        Connection connection = null;
        try
        {
            // create a database connection
            connection = DriverManager.getConnection(
                    String.format("jdbc:sqlite:%s/data/task.db",
                            System.getProperty("user.home")));
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

//            statement.executeUpdate("drop table if exists person");
//            statement.executeUpdate("create table person (id integer, name string)");
//            statement.executeUpdate("insert into person values(1, 'leo')");
//            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("SELECT * FROM `taskdb`");
            while(rs.next())
            {
                // read the result set
                System.out.print("taskid = " + rs.getInt("taskid")+"\t");
                System.out.print("project = " + rs.getString("project")+"\t");
                System.out.print("url = " + rs.getString("url")+"\t");
                System.out.print("status = " + rs.getString("status")+"\t");
                System.out.print("schedule = " + rs.getString("schedule")+"\t");
                System.out.print("fetch = " + rs.getInt("fetch")+"\t");
                System.out.print("process = " + rs.getInt("process")+"\t");
                System.out.print("track = " + rs.getString("track")+"\t");
                System.out.print("lastcrawltime = " + rs.getString("lastcrawltime")+"\t");
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
