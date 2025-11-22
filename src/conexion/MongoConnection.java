package conexion;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    // Ajuste o nome do banco se quiser outro (hoje é "concessionaria")
    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "concessionaria";

    private static MongoClient client;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            client = MongoClients.create(URI);
            database = client.getDatabase(DB_NAME);
        }
        return database;
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}