package reports;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import conexion.MongoConnection;

public class Relatorios {

    // ============================================================
    // Relatório 1: Vendas por marca e mês
    // Equivalente à view vw_vendas_por_marca_mes da C2:
    // SELECT marca, ano_mes, COUNT(*), SUM(valor_final) ...
    // ============================================================
    public static void vendasPorMarcaMes() {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> vendas = db.getCollection("venda");

            List<Document> pipeline = Arrays.asList(
                    // join com VEICULO (id_veiculo -> _id)
                    new Document("$lookup",
                            new Document("from", "veiculo")
                                    .append("localField", "id_veiculo")
                                    .append("foreignField", "_id")
                                    .append("as", "veiculo")),
                    // transforma array veiculo[] em objeto simples
                    new Document("$unwind", "$veiculo"),
                    // agrupa por marca + ano_mes (YYYY-MM)
                    new Document("$group",
                            new Document("_id",
                                    new Document("marca", "$veiculo.marca")
                                            .append("ano_mes",
                                                    new Document("$dateToString",
                                                            new Document("format", "%Y-%m")
                                                                    .append("date", "$data_venda"))))
                                    .append("qtd_vendas", new Document("$sum", 1))
                                    .append("total_vendido", new Document("$sum", "$valor_final"))),
                    // ordena por marca e mês
                    new Document("$sort",
                            new Document("_id.marca", 1)
                                    .append("_id.ano_mes", 1)));

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            System.out.println("\n== Relatório: Vendas por marca/mês (Mongo) ==");
            for (Document d : vendas.aggregate(pipeline)) {
                Document id = (Document) d.get("_id");
                String marca = id.getString("marca");
                String anoMes = id.getString("ano_mes");
                int qtd = d.getInteger("qtd_vendas", 0);
                double total = d.getDouble("total_vendido");

                System.out.printf("Marca: %-10s | Mês: %s | Qtde: %3d | Total: %s%n",
                        marca, anoMes, qtd, nf.format(total));
            }

        } catch (Exception e) {
            System.out.println("Erro ao executar relatório vendasPorMarcaMes (Mongo): " + e.getMessage());
        }
    }

    // ============================================================
    // Relatório 2: Vendas detalhadas
    // Equivalente à view vw_vendas_detalhadas da C2:
    // id_venda, data_venda, cliente, marca, modelo, cor, valor_final
    // ============================================================
    public static void vendasDetalhadas() {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> vendas = db.getCollection("venda");

            List<Document> pipeline = Arrays.asList(
                    // join com CLIENTE
                    new Document("$lookup",
                            new Document("from", "cliente")
                                    .append("localField", "id_cliente")
                                    .append("foreignField", "_id")
                                    .append("as", "cliente")),
                    // join com VEICULO
                    new Document("$lookup",
                            new Document("from", "veiculo")
                                    .append("localField", "id_veiculo")
                                    .append("foreignField", "_id")
                                    .append("as", "veiculo")),
                    // transforma arrays em objetos simples
                    new Document("$unwind", "$cliente"),
                    new Document("$unwind", "$veiculo"),
                    // seleciona somente os campos que queremos exibir
                    new Document("$project",
                            new Document("_id", 1)
                                    .append("data_venda", 1)
                                    .append("cliente", "$cliente.nome")
                                    .append("marca", "$veiculo.marca")
                                    .append("modelo", "$veiculo.modelo")
                                    .append("cor", "$veiculo.cor")
                                    .append("valor_final", 1)),
                    // ordena por data (decrescente)
                    new Document("$sort", new Document("data_venda", -1)));

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            System.out.println("\n== Relatório: Vendas detalhadas (Mongo) ==");
            for (Document d : vendas.aggregate(pipeline)) {
                int idVenda = d.getInteger("_id");
                Object data = d.get("data_venda"); // Date -> toString()
                String cliente = d.getString("cliente");
                String marca = d.getString("marca");
                String modelo = d.getString("modelo");
                String cor = d.getString("cor");
                double valor = d.getDouble("valor_final");

                System.out.printf("#%d | %s | %-15s | %-10s %-10s %-8s | %s%n",
                        idVenda,
                        data,
                        cliente,
                        marca,
                        modelo,
                        cor,
                        nf.format(valor));
            }

        } catch (Exception e) {
            System.out.println("Erro ao executar relatório vendasDetalhadas (Mongo): " + e.getMessage());
        }
    }
}