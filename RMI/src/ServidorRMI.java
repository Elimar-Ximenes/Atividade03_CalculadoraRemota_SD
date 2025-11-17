import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServidorRMI {

    public static void main(String[] args) {
        try {
            // inicia o registro RMI na porta padrão 1099
            LocateRegistry.createRegistry(1099);
            System.out.println("Registry RMI iniciado na porta 1099.");

            // cria o objeto remoto
            ICalculadora calculadora = new CalculadoraRMI();

            // registra o objeto remoto com o nome "CalculadoraRMI"
            Naming.rebind("rmi://localhost/CalculadoraRMI", calculadora);

            System.out.println("Servidor RMI da Calculadora no ar!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
