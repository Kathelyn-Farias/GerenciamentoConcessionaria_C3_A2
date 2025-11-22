import utils.MenuHelper;
import utils.SplashScreen;
import reports.Relatorios;
import controller.ClienteMongoController;
import controller.VeiculoMongoController;
import controller.VendaMongoController;
import conexion.MongoConnection;

public class Main {
    public static void main(String[] args) {
        SplashScreen.show();

        boolean loop = true;
        while (loop) {
            switch (MenuHelper.menuPrincipal()) {
                case 1 -> { // Relatórios
                    int op = MenuHelper.submenuRelatorios();
                    if (op == 1) {
                        Relatorios.vendasPorMarcaMes();
                    } else if (op == 2) {
                        Relatorios.vendasDetalhadas();
                    }
                    MenuHelper.pause();
                }

                case 2 -> { // Inserir documentos
                    switch (MenuHelper.submenuEntidades("Inserir")) {
                        case 1 -> ClienteMongoController.inserir();
                        case 2 -> VeiculoMongoController.inserir();
                        case 3 -> VendaMongoController.inserir();
                        case 0 -> {
                        }
                    }
                    MenuHelper.pause();
                }

                case 3 -> { // Remover documentos
                    switch (MenuHelper.submenuEntidades("Remover")) {
                        case 1 -> ClienteMongoController.remover();
                        case 2 -> VeiculoMongoController.remover();
                        case 3 -> VendaMongoController.remover();
                        case 0 -> {
                        }
                    }
                    MenuHelper.pause();
                }

                case 4 -> { // Atualizar documentos
                    switch (MenuHelper.submenuEntidades("Atualizar")) {
                        case 1 -> ClienteMongoController.atualizar();
                        case 2 -> VeiculoMongoController.atualizar();
                        case 3 -> VendaMongoController.atualizar();
                        case 0 -> {
                        }
                    }
                    MenuHelper.pause();
                }

                case 5 -> { // Listar registros
                    switch (MenuHelper.submenuEntidades("Listar")) {
                        case 1 -> ClienteMongoController.listar();
                        case 2 -> VeiculoMongoController.listar();
                        case 3 -> VendaMongoController.listar();
                        case 0 -> {
                        }
                    }
                    MenuHelper.pause();
                }

                case 6 -> loop = false;

                default -> System.out.println("Opção inválida.");
            }
        }

        // fecha o cliente Mongo antes de encerrar
        MongoConnection.close();
        System.out.println("Até mais!");
    }
}