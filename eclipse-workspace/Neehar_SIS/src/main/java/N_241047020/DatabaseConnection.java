package N_241047020;0

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConnection {

    // Method to load database properties from db.properties file
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IOException("Unable to find db.properties file.");
            }
            properties.load(inputStream);
        }
        return properties;
    }

    // Method to get the database connection
    public static Connection getConnection() throws SQLException, IOException, ClassNotFoundException {
        Properties dbProperties = loadProperties();

        String dbURL = dbProperties.getProperty("db.url");
        String username = dbProperties.getProperty("db.username");
        String password = dbProperties.getProperty("db.password");
        String driver = dbProperties.getProperty("db.driver");

        // Load the database driver
        Class.forName(driver);

        // Return a connection to the database
        return DriverManager.getConnection(dbURL, username, password);
    }
}
