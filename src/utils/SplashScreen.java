package utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import conexion.MongoConnection;
import org.bson.Document;

public class SplashScreen {

    public static void show() {
        System.out.println("==================================================");
        System.out.println("     SISTEMA: Gerenciamento de Concessionária     ");
        System.out.println("     Trabalho C3 - Banco de Dados Não Relacional  ");
        System.out.println("==================================================");
        System.out.println("Disciplina: Banco de Dados");
        System.out.println("Professor:  Howard Roatti");
        System.out.println("Semestre:   3º");
        System.out.println("Grupo:  Enrico S. Breda e Kathelyn V. R. Farias");
        System.out.println("--------------------------------------------------");

        try {
            MongoDatabase db = MongoConnection.getDatabase();

            MongoCollection<Document> colClientes = db.getCollection("cliente");
            MongoCollection<Document> colVeiculos = db.getCollection("veiculo");
            MongoCollection<Document> colVendas = db.getCollection("venda");

            long qtdClientes = colClientes.countDocuments();
            long qtdVeiculos = colVeiculos.countDocuments();
            long qtdVendas = colVendas.countDocuments();
            long totalDocs = qtdClientes + qtdVeiculos + qtdVendas;

            String stClientes = (qtdClientes > 0) ? "com documentos" : "sem documentos";
            String stVeiculos = (qtdVeiculos > 0) ? "com documentos" : "sem documentos";
            String stVendas = (qtdVendas > 0) ? "com documentos" : "sem documentos";

            System.out.println("Resumo das coleções do MongoDB (banco: concessionaria)");
            System.out.printf("  - cliente : %4d documento(s) [%s]%n", qtdClientes, stClientes);
            System.out.printf("  - veiculo : %4d documento(s) [%s]%n", qtdVeiculos, stVeiculos);
            System.out.printf("  - venda   : %4d documento(s) [%s]%n", qtdVendas, stVendas);
            System.out.println("--------------------------------------------------");
            System.out.printf("  TOTAL de documentos no sistema: %d%n", totalDocs);
            System.out.println("==================================================");
        } catch (Exception e) {
            System.out.println("Falha ao acessar o MongoDB para exibir o resumo inicial.");
            System.out.println("Verifique se o servidor Mongo está em execução.");
            System.out.println("Detalhes: " + e.getMessage());
            System.out.println("==================================================");
        }
    }
}
