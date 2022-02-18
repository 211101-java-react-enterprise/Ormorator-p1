package com.revature.orm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final ConnectionFactory connectionFactory =
            new ConnectionFactory();
    private static final Properties props = new Properties();

    static {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            props.load(loader.getResourceAsStream("db.properties"));
            Class.forName(props.getProperty("dbDriver"));
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private ConnectionFactory() {}

    public static ConnectionFactory getInstance() { return connectionFactory; }

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(props.getProperty("url"),
                    props.getProperty("username"), props.getProperty(
                            "password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

}
