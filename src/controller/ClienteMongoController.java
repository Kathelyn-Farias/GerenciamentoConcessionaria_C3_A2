package controller;

import conexion.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import utils.MenuHelper;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class ClienteMongoController {

    private static final Scanner in = new Scanner(System.in);

    private static MongoCollection<Document> col() {
        MongoDatabase db = MongoConnection.getDatabase();
        return db.getCollection("cliente"); // nome da coleção criada pela migração
    }

    private static int proximoId() {
        Document doc = col().find()
                .sort(Sorts.descending("_id"))
                .first();
        return (doc == null) ? 1 : doc.getInteger("_id") + 1;
    }

    public static void listar() {
        System.out.println("\n-- Clientes (Mongo) --");
        for (Document d : col().find()) {
            System.out.printf(
                    "#%d | %s | CPF: %s | Tel: %s | %s%n",
                    d.getInteger("_id"),
                    d.getString("nome"),
                    d.getString("cpf"),
                    d.getString("telefone"),
                    d.getString("email"));
        }
    }

    public static void inserir() {
        do {
            System.out.println("\n== Inserir Cliente (Mongo) ==");
            int id = proximoId();

            System.out.print("Nome: ");
            String nome = in.nextLine();

            System.out.print("CPF: ");
            String cpf = in.nextLine();

            System.out.print("Telefone: ");
            String telefone = in.nextLine();

            System.out.print("Email: ");
            String email = in.nextLine();

            Document doc = new Document("_id", id)
                    .append("nome", nome)
                    .append("cpf", cpf)
                    .append("telefone", telefone)
                    .append("email", email);

            col().insertOne(doc);
            System.out.println("Cliente inserido com _id = " + id);

        } while (MenuHelper.confirm("\nDeseja inserir outro cliente?"));
    }

    public static void remover() {
        do {
            listar();
            System.out.print("\nID do cliente para remover (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            // Verifica se há vendas associadas a este cliente
            MongoDatabase db = MongoConnection.getDatabase();
            long qtdVendas = db.getCollection("venda")
                    .countDocuments(eq("id_cliente", id));

            if (qtdVendas > 0) {
                System.out.printf(
                        "Não é possível remover o cliente #%d: existem %d venda(s) associada(s).%n",
                        id, qtdVendas);
            } else {
                long apagados = col().deleteOne(eq("_id", id)).getDeletedCount();
                System.out.println(apagados > 0 ? "Cliente removido." : "Cliente não encontrado.");
            }

        } while (MenuHelper.confirm("\nDeseja remover outro cliente?"));
    }

    public static void atualizar() {
        do {
            listar();
            System.out.print("\nID do cliente para atualizar (0 = voltar): ");
            int id = Integer.parseInt(in.nextLine());
            if (id == 0)
                break;

            Document atual = col().find(eq("_id", id)).first();
            if (atual == null) {
                System.out.println("Cliente não encontrado.");
            } else {
                System.out.printf("Nome atual: %s%nNovo nome (ENTER = manter): ", atual.getString("nome"));
                String nome = in.nextLine();
                if (nome.isEmpty())
                    nome = atual.getString("nome");

                System.out.printf("CPF atual: %s%nNovo CPF (ENTER = manter): ", atual.getString("cpf"));
                String cpf = in.nextLine();
                if (cpf.isEmpty())
                    cpf = atual.getString("cpf");

                System.out.printf("Telefone atual: %s%nNovo telefone (ENTER = manter): ", atual.getString("telefone"));
                String telefone = in.nextLine();
                if (telefone.isEmpty())
                    telefone = atual.getString("telefone");

                System.out.printf("Email atual: %s%nNovo email (ENTER = manter): ", atual.getString("email"));
                String email = in.nextLine();
                if (email.isEmpty())
                    email = atual.getString("email");

                Document set = new Document("nome", nome)
                        .append("cpf", cpf)
                        .append("telefone", telefone)
                        .append("email", email);

                col().updateOne(eq("_id", id), new Document("$set", set));
                System.out.println("Cliente atualizado.");
            }

        } while (MenuHelper.confirm("\nDeseja atualizar outro cliente?"));
    }
}