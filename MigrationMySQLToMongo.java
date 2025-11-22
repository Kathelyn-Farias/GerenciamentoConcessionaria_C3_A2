import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MigrationMySQLToMongo {

    // ------------------------------------------------------------------
    // CONFIGURAÇÃO DO MONGODB
    // ------------------------------------------------------------------
    // Se o Mongo estiver em outra porta/host, ajuste aqui.
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String MONGO_DB_NAME = "concessionaria";

    // ------------------------------------------------------------------
    // LÊ CONFIGURAÇÃO DO MYSQL A PARTIR DO db.properties
    // ------------------------------------------------------------------
    private static Connection getMySQLConnection() throws Exception {
        Properties props = new Properties();
        // mesmo caminho que a ConexaoMySQL usa no C2:
        String propsPath = "src/conexion/db.properties";
        try (InputStream in = new FileInputStream(propsPath)) {
            props.load(in);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        // carrega driver (opcional em versões novas, mas garante)
        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager.getConnection(url, user, pass);
    }

    public static void main(String[] args) {
        System.out.println("=== MIGRAÇÃO MySQL -> MongoDB (C2 -> C3) ===");

        try (Connection mysqlConn = getMySQLConnection();
                MongoClient mongoClient = MongoClients.create(MONGO_URI)) {

            MongoDatabase mongoDb = mongoClient.getDatabase(MONGO_DB_NAME);

            migrateCliente(mysqlConn, mongoDb);
            migrateVeiculo(mysqlConn, mongoDb);
            migrateVenda(mysqlConn, mongoDb);

            System.out.println("=== Migração concluída com sucesso! ===");

        } catch (Exception e) {
            System.err.println("Erro durante a migração:");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // MIGRA TABELA CLIENTE -> COLEÇÃO "cliente"
    // ------------------------------------------------------------------
    private static void migrateCliente(Connection mysqlConn, MongoDatabase mongoDb) throws Exception {
        System.out.println("\n[1/3] Migrando tabela CLIENTE...");

        MongoCollection<Document> collection = mongoDb.getCollection("cliente");
        // Limpa a coleção para não duplicar registros
        collection.deleteMany(new Document());

        String sql = "SELECT id_cliente, nome, cpf, telefone, email FROM cliente ORDER BY id_cliente";

        try (PreparedStatement stmt = mysqlConn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Document> docs = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id_cliente");

                Document doc = new Document("_id", id)
                        .append("nome", rs.getString("nome"))
                        .append("cpf", rs.getString("cpf"))
                        .append("telefone", rs.getString("telefone"))
                        .append("email", rs.getString("email"));

                docs.add(doc);
            }

            if (!docs.isEmpty()) {
                collection.insertMany(docs);
            }

            System.out.println("CLIENTE -> documentos inseridos: " + docs.size());
        }
    }

    // ------------------------------------------------------------------
    // MIGRA TABELA VEICULO -> COLEÇÃO "veiculo"
    // ------------------------------------------------------------------
    private static void migrateVeiculo(Connection mysqlConn, MongoDatabase mongoDb) throws Exception {
        System.out.println("\n[2/3] Migrando tabela VEICULO...");

        MongoCollection<Document> collection = mongoDb.getCollection("veiculo");
        collection.deleteMany(new Document());

        String sql = """
                SELECT id_veiculo, marca, modelo, cor, ano, preco, disponivel
                FROM veiculo
                ORDER BY id_veiculo
                """;

        try (PreparedStatement stmt = mysqlConn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Document> docs = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id_veiculo");

                Document doc = new Document("_id", id)
                        .append("marca", rs.getString("marca"))
                        .append("modelo", rs.getString("modelo"))
                        .append("cor", rs.getString("cor"))
                        .append("ano", rs.getInt("ano"))
                        .append("preco", rs.getDouble("preco"))
                        .append("disponivel", rs.getBoolean("disponivel"));

                docs.add(doc);
            }

            if (!docs.isEmpty()) {
                collection.insertMany(docs);
            }

            System.out.println("VEICULO -> documentos inseridos: " + docs.size());
        }
    }

    // ------------------------------------------------------------------
    // MIGRA TABELA VENDA -> COLEÇÃO "venda"
    // ------------------------------------------------------------------
    private static void migrateVenda(Connection mysqlConn, MongoDatabase mongoDb) throws Exception {
        System.out.println("\n[3/3] Migrando tabela VENDA...");

        MongoCollection<Document> collection = mongoDb.getCollection("venda");
        collection.deleteMany(new Document());

        String sql = """
                SELECT id_venda, data_venda, valor_final, id_cliente, id_veiculo
                FROM venda
                ORDER BY id_venda
                """;

        try (PreparedStatement stmt = mysqlConn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            List<Document> docs = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id_venda");
                Date dataVenda = rs.getDate("data_venda");
                double valorFinal = rs.getDouble("valor_final");
                int idCliente = rs.getInt("id_cliente");
                int idVeiculo = rs.getInt("id_veiculo");

                Document doc = new Document("_id", id)
                        .append("data_venda", dataVenda)
                        .append("valor_final", valorFinal)
                        .append("id_cliente", idCliente) // referencia cliente._id
                        .append("id_veiculo", idVeiculo); // referencia veiculo._id

                docs.add(doc);
            }

            if (!docs.isEmpty()) {
                collection.insertMany(docs);
            }

            System.out.println("VENDA -> documentos inseridos: " + docs.size());
        }
    }
}
