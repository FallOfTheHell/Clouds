package SQL;

import java.sql.*;

public class DataBase {
    private static Connection connection;
    private static Statement statement;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "090909eG";
    private static final String URL = "jdbc:mysql://localhost:3306/_user_cloud?useSSL=false";
    public static void main(String[] args) throws SQLException {
        connect();
        ResultSet resultSet = statement.executeQuery("select * from users");
        //statement.execute("insert into users(username,pass) value (\"Glu\", 123)");
        while (resultSet.next()){
            System.out.println(resultSet.getInt(1) + resultSet.getString(2)+ resultSet.getInt(3));
        }
        disconnect();
    }
    public static Connection connect()  {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println(e);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void singUpUser(String userName, String pass, String name, String lastname)  {
        String insert = "insert into users(username,password,name,lastname) value (?, ?, ?, ?)";
        try {
            PreparedStatement prSt = connect().prepareStatement(insert);
            prSt.setString(1, userName);
            prSt.setString(2, pass);
            prSt.setString(3, name);
            prSt.setString(4, lastname);
            prSt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ResultSet getUser(String userName, String pass){
        ResultSet resultSet = null;
        String select = "Select * From users where username =? and password =?";
        try {
            PreparedStatement prSt = connect().prepareStatement(select);
            prSt.setString(1, userName);
            prSt.setString(2, pass);
            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}
