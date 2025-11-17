import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CalculadoraClienteRMI {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("============== CALCULADORA REMOTA RMI ==============");
        System.out.println("Escolha o modo de operacao:");
        System.out.println("1 - Modo 1 (Servidor resolve a expressão completa)");
        System.out.println("2 - Modo 2 (Cliente resolve usando múltiplas chamadas RMI)");
        System.out.print("Opção: ");
        int modo = scanner.nextInt();
        scanner.nextLine(); // consumir newline

        System.out.println("\nDigite a expressao (ex: (10+5)*3 ): ");
        String expressao = scanner.nextLine();

        // Converte para RPN
        String rpn = RPNConverter.toRPN(expressao);
        System.out.println("\nExpressao convertida para RPN: " + rpn);

        try {
            // Conecta ao Registry RMI no localhost, porta 1099
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);

            // Nome deve ser o mesmo usado no ServidorRMI:
            // Naming.rebind("rmi://localhost/CalculadoraRMI", calculadora);
            ICalculadora calc = (ICalculadora) reg.lookup("CalculadoraRMI");

            if (modo == 1) {
                // =======================
                //  MODO 1: SERVIDOR CALCULA TUDO
                // =======================
                System.out.println("\n[MODO 1] Servidor calculara a expressao completa (RPN).");

                double resultado = calc.calculaRPN(rpn);
                System.out.println("Resultado recebido do servidor: " + resultado);

            } else if (modo == 2) {
                // =======================
                //  MODO 2: CLIENTE CHAMA O SERVIDOR VARIAS VEZES
                // =======================
                System.out.println("\n[MODO 2] Cliente vai decompor e chamar o servidor varias vezes via RMI.");

                // Agora em vez de abrir Socket, usamos o objeto remoto calc
                double resultadoFinal = ClienteOperacoesRMI.avaliarRPN_Cliente(rpn, calc);

                System.out.println("Resultado final calculado pelo cliente (modo 2): " + resultadoFinal);

            } else {
                System.out.println("Opcao invalida!");
            }

        } catch (Exception e) {
            System.out.println("Erro no cliente RMI:");
            e.printStackTrace();
        }
    }
}
