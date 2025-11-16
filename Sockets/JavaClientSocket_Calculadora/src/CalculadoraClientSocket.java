import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class CalculadoraClientSocket {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("============== CALCULADORA REMOTA ==============");
        System.out.println("Escolha o modo de operação:");
        System.out.println("1 - Modo 1 (Servidor resolve a expressão completa)");
        System.out.println("2 - Modo 2 (Cliente resolve usando múltiplas chamadas)");
        System.out.print("Opção: ");
        int modo = scanner.nextInt();
        scanner.nextLine(); // consumir newline

        System.out.println("\nDigite a expressão (ex: (10+5)*3 ): ");
        String expressao = scanner.nextLine();

        // Converte para RPN
        String rpn = RPNConverter.toRPN(expressao);
        System.out.println("\nExpressão convertida para RPN: " + rpn);

        try {
            if (modo == 1) {
                // =======================
                //  MODO 1: SERVIDOR CALCULA
                // =======================
                System.out.println("\n[MODO 1] Servidor calculará a expressão completa.");

                Socket clientSocket = new Socket("127.0.0.1", 9090);
                DataOutputStream socketSaidaServer =
                        new DataOutputStream(clientSocket.getOutputStream());

                socketSaidaServer.writeBytes("5\n"); // operação 5 = cálculo RPN completa
                socketSaidaServer.writeBytes(rpn + "\n");
                socketSaidaServer.flush();

                BufferedReader messageFromServer =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String resultado = messageFromServer.readLine();
                System.out.println("Resultado recebido do servidor: " + resultado);

                clientSocket.close();

            } else if (modo == 2) {
                // =======================
                //  MODO 2: CLIENTE CHAMA O SERVIDOR VÁRIAS VEZES
                // =======================
                System.out.println("\n[MODO 2] Cliente vai decompor e chamar o servidor várias vezes.");

                double resultadoFinal = ClienteOperacoes.avaliarRPN_Cliente(rpn);

                System.out.println("Resultado final calculado pelo cliente (modo 2): " + resultadoFinal);

            } else {
                System.out.println("Opção inválida!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}