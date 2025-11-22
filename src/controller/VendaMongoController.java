package controller;

import conexion.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import utils.MenuHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class VendaMongoController {

    private static final Scanner in = new Scanner(System.in);

    private static MongoDatabase db() {
        return MongoConnection.getDatabase();
    }

    private static MongoCollection<Document> colVenda() {
        return db().getCollection("venda");
    }

    private static MongoCollection<Document> colCliente() {
        return db().getCollection("cliente");
    }

    private static MongoCollection<Document> colVeiculo() {
        return db().getCollection("veiculo");
    }

    private static int proximoId() {
        Document doc = colVenda().find()
                .sort(Sorts.descending("_id"))
                .first();
        return (doc == null) ? 1 : doc.getInteger("_id") + 1;
    }

    // ---- Listar com "joinzinho" manual (nome do cliente + veiculo) ----
    public static void listar() {
        System.out.println("\n-- Vendas (Mongo) --");

        for (Document v : colVenda().find().sort(Sorts.ascending("_id"))) {
            int idVenda = v.getInteger("_id");

            // pega como java.util.Date
            Date dataVenda = v.getDate("data_venda");
            String dataStr = (dataVenda != null) ? dataVenda.toString() : "sem data";

            double valor = v.getDouble("valor_final");
            int idCliente = v.getInteger("id_cliente");
            int idVeiculo = v.getInteger("id_veiculo");

            Document cli = colCliente().find(eq("_id", idCliente)).first();
            Document vei = colVeiculo().find(eq("_id", idVeiculo)).first();

            String nomeCliente = (cli != null) ? cli.getString("nome") : "N/D";
            String descVeiculo = (vei != null)
                    ? vei.getString("marca") + " " + vei.getString("modelo")
                    : "N/D";

            System.out.printf(
                    "#%d | %s | R$ %.2f | Cliente #%d %s | Veículo #%d %s%n",
                    idVenda,
                    dataStr,
                    valor,
                    idCliente, nomeCliente,
                    idVeiculo, descVeiculo);
        }
    }

    public static void inserir() {
        do {
            System.out.println("\n== Registrar Venda (Mongo) ==");

            System.out.println("\nClientes (id | nome):");
            for (Document c : colCliente().find().sort(Sorts.ascending("_id"))) {
                System.out.printf("#%d | %s%n", c.getInteger("_id"), c.getString("nome"));
            }

            System.out.println("\nVeículos disponíveis (id | marca modelo):");
            for (Document v : colVeiculo().find(eq("disponivel", true))) {
                System.out.printf(
                        "#%d | %s %s%n",
                        v.getInteger("_id"),
                        v.getString("marca"),
                        v.getString("modelo"));
            }

            System.out.print("ID do cliente: ");
            int idCliente = Integer.parseInt(in.nextLine());

            if (colCliente().find(eq("_id", idCliente)).first() == null) {
                System.out.println("Cliente não encontrado.");
                continue;
            }

            System.out.print("ID do veículo: ");
            int idVeiculo = Integer.parseInt(in.nextLine());

            Document veiculo = colVeiculo().find(eq("_id", idVeiculo)).first();
            if (veiculo == null) {
                System.out.println("Veículo não encontrado.");
                continue;
            }
            if (!veiculo.getBoolean("disponivel", true)) {
                System.out.println("Veículo não está disponível para venda.");
                continue;
            }

            System.out.print("Data da venda (AAAA-MM-DD): ");
            String dataStr = in.nextLine();
            LocalDate ld = LocalDate.parse(dataStr);
            Date dataVenda = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());

            System.out.print("Valor final: ");
            double valor = Double.parseDouble(in.nextLine());

            int idVenda = proximoId();

            Document venda = new Document("_id", idVenda)
                    .append("data_venda", dataVenda)
                    .append("valor_final", valor)
                    .append("id_cliente", idCliente)
                    .append("id_veiculo", idVeiculo);

            colVenda().insertOne(venda);

            colVeiculo().updateOne(eq("_id", idVeiculo),
                    new Document("$set", new Document("disponivel", false)));

            System.out.println("Venda registrada com _id = " + idVenda);

        } while (MenuHelper.confirm("\nDeseja registrar outra venda?"));
    }

    public static void remover() {
        do {
            listar();
            System.out.print("\nID da venda para remover (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            Document venda = colVenda().find(eq("_id", id)).first();
            if (venda == null) {
                System.out.println("Venda não encontrada.");
            } else {
                int idVeiculo = venda.getInteger("id_veiculo");

                long apagados = colVenda().deleteOne(eq("_id", id)).getDeletedCount();
                if (apagados > 0) {
                    colVeiculo().updateOne(eq("_id", idVeiculo),
                            new Document("$set", new Document("disponivel", true)));
                    System.out.println("Venda removida.");
                } else {
                    System.out.println("Nada foi removido.");
                }
            }

        } while (MenuHelper.confirm("\nDeseja remover outra venda?"));
    }

    public static void atualizar() {
        do {
            listar();
            System.out.print("\nID da venda para atualizar (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            Document atual = colVenda().find(eq("_id", id)).first();
            if (atual == null) {
                System.out.println("Venda não encontrada.");
            } else {
                Date dataAtual = atual.getDate("data_venda");
                String dataAtualStr = (dataAtual != null) ? dataAtual.toString() : "sem data";

                System.out.printf("Data atual: %s%nNova data (AAAA-MM-DD, ENTER = manter): ",
                        dataAtualStr);
                String dataStr = in.nextLine();

                Date novaData = dataAtual;
                if (!dataStr.isEmpty()) {
                    LocalDate novoLD = LocalDate.parse(dataStr);
                    novaData = Date.from(novoLD.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }

                System.out.printf("Valor atual: %.2f%nNovo valor (ENTER = manter): ",
                        atual.getDouble("valor_final"));
                String valorStr = in.nextLine();
                double novoValor = valorStr.isEmpty()
                        ? atual.getDouble("valor_final")
                        : Double.parseDouble(valorStr);

                Document set = new Document("data_venda", novaData)
                        .append("valor_final", novoValor);

                colVenda().updateOne(eq("_id", id), new Document("$set", set));
                System.out.println("Venda atualizada.");
            }

        } while (MenuHelper.confirm("\nDeseja atualizar outra venda?"));
    }
}