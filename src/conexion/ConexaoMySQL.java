package conexion;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexaoMySQL {
    private static final String PROPS_PATH = "src/conexion/db.properties";
    private Connection conn;

    public Connection getConnection() throws Exception {
        if (conn != null && !conn.isClosed()) return conn;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(PROPS_PATH)) {
            p.load(in);
        }
        conn = DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password"));
        return conn;
    }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (Exception ignored) {}
    }
}
