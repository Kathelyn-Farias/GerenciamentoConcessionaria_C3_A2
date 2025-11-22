package controller;

import conexion.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import utils.MenuHelper;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class VeiculoMongoController {

    private static final Scanner in = new Scanner(System.in);

    private static MongoCollection<Document> col() {
        MongoDatabase db = MongoConnection.getDatabase();
        return db.getCollection("veiculo");
    }

    private static int proximoId() {
        Document doc = col().find()
                .sort(Sorts.descending("_id"))
                .first();
        return (doc == null) ? 1 : doc.getInteger("_id") + 1;
    }

    public static void listar() {
        System.out.println("\n-- Veículos (Mongo) --");
        for (Document d : col().find()) {
            System.out.printf(
                    "#%d | %s %s | Cor: %s | Ano: %d | Preço: R$ %.2f | Disponível: %s%n",
                    d.getInteger("_id"),
                    d.getString("marca"),
                    d.getString("modelo"),
                    d.getString("cor"),
                    d.getInteger("ano"),
                    d.getDouble("preco"),
                    d.getBoolean("disponivel") ? "SIM" : "NÃO");
        }
    }

    public static void listarDisponiveis() {
        System.out.println("\n-- Veículos disponíveis --");
        for (Document d : col().find(eq("disponivel", true))) {
            System.out.printf(
                    "#%d | %s %s | Cor: %s | Ano: %d | Preço: R$ %.2f%n",
                    d.getInteger("_id"),
                    d.getString("marca"),
                    d.getString("modelo"),
                    d.getString("cor"),
                    d.getInteger("ano"),
                    d.getDouble("preco"));
        }
    }

    public static void inserir() {
        do {
            System.out.println("\n== Inserir Veículo (Mongo) ==");
            int id = proximoId();

            System.out.print("Marca: ");
            String marca = in.nextLine();

            System.out.print("Modelo: ");
            String modelo = in.nextLine();

            System.out.print("Cor: ");
            String cor = in.nextLine();

            System.out.print("Ano: ");
            int ano = Integer.parseInt(in.nextLine());

            System.out.print("Preço: ");
            double preco = Double.parseDouble(in.nextLine());

            Document doc = new Document("_id", id)
                    .append("marca", marca)
                    .append("modelo", modelo)
                    .append("cor", cor)
                    .append("ano", ano)
                    .append("preco", preco)
                    .append("disponivel", true);

            col().insertOne(doc);
            System.out.println("Veículo inserido com _id = " + id);

        } while (MenuHelper.confirm("\nDeseja inserir outro veículo?"));
    }

    public static void remover() {
        do {
            listar();
            System.out.print("\nID do veículo para remover (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            // Verifica se há vendas associadas a este veículo
            MongoDatabase db = MongoConnection.getDatabase();
            long qtdVendas = db.getCollection("venda")
                    .countDocuments(eq("id_veiculo", id));

            if (qtdVendas > 0) {
                System.out.printf(
                        "Não é possível remover o veículo #%d: existem %d venda(s) associada(s).%n",
                        id, qtdVendas);
            } else {
                long apagados = col().deleteOne(eq("_id", id)).getDeletedCount();
                System.out.println(apagados > 0 ? "Veículo removido." : "Veículo não encontrado.");
            }

        } while (MenuHelper.confirm("\nDeseja remover outro veículo?"));
    }

    public static void atualizar() {
        do {
            listar();
            System.out.print("\nID do veículo para atualizar (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            Document atual = col().find(eq("_id", id)).first();
            if (atual == null) {
                System.out.println("Veículo não encontrado.");
            } else {
                System.out.printf("Marca atual: %s%nNova marca (ENTER = manter): ", atual.getString("marca"));
                String marca = in.nextLine();
                if (marca.isEmpty())
                    marca = atual.getString("marca");

                System.out.printf("Modelo atual: %s%nNovo modelo (ENTER = manter): ", atual.getString("modelo"));
                String modelo = in.nextLine();
                if (modelo.isEmpty())
                    modelo = atual.getString("modelo");

                System.out.printf("Cor atual: %s%nNova cor (ENTER = manter): ", atual.getString("cor"));
                String cor = in.nextLine();
                if (cor.isEmpty())
                    cor = atual.getString("cor");

                System.out.printf("Ano atual: %d%nNovo ano (ENTER = manter): ", atual.getInteger("ano"));
                String anoStr = in.nextLine();
                int ano = anoStr.isEmpty() ? atual.getInteger("ano") : Integer.parseInt(anoStr);

                System.out.printf("Preço atual: %.2f%nNovo preço (ENTER = manter): ", atual.getDouble("preco"));
                String precoStr = in.nextLine();
                double preco = precoStr.isEmpty() ? atual.getDouble("preco") : Double.parseDouble(precoStr);

                System.out.printf("Disponível atual: %s%nNovo disponível (S/N, ENTER = manter): ",
                        atual.getBoolean("disponivel") ? "SIM" : "NÃO");
                String dispStr = in.nextLine().trim().toUpperCase();
                boolean disponivel = dispStr.isEmpty()
                        ? atual.getBoolean("disponivel")
                        : dispStr.startsWith("S");

                Document set = new Document("marca", marca)
                        .append("modelo", modelo)
                        .append("cor", cor)
                        .append("ano", ano)
                        .append("preco", preco)
                        .append("disponivel", disponivel);

                col().updateOne(eq("_id", id), new Document("$set", set));
                System.out.println("Veículo atualizado.");
            }

        } while (MenuHelper.confirm("\nDeseja atualizar outro veículo?"));
    }
}