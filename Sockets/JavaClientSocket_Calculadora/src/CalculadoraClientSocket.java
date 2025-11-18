import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class CalculadoraClientSocket {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in); // leitor para entrada do usuário

        // Interface simples no console
        System.out.println("============== CALCULADORA REMOTA ==============");
        System.out.println("Escolha o modo de operação:");
        System.out.println("1 - Modo 1 (Servidor resolve a expressão completa)");
        System.out.println("2 - Modo 2 (Cliente resolve usando múltiplas chamadas)");
        System.out.print("Opção: ");

        int modoOperacao = scanner.nextInt(); // lê opção do usuário
        scanner.nextLine(); // consome o \n sobrando

        // Entrada da expressão original
        System.out.println("\nDigite a expressão (ex: (10+5)*3 ): ");
        String expressaoInfixa = scanner.nextLine();

        // Conversão da expressão infixa para RPN
        String expressaoRPN = RPNConverter.toRPN(expressaoInfixa);
        System.out.println("\nExpressão convertida para RPN: " + expressaoRPN);

        try {

            // ===================================================
            // MODO 1: Servidor calcula a expressão inteira (RPN)
            // ===================================================
            if (modoOperacao == 1) {

                System.out.println("\n[MODO 1] Servidor calculará a expressão completa.");

                // Abre conexão com o servidor na porta 9090
                Socket socketCliente = new Socket("127.0.0.1", 9090);

                // Canal de envio de dados para o servidor
                DataOutputStream saidaServidor =
                        new DataOutputStream(socketCliente.getOutputStream());

                // Envia código da operação: "5" = cálculo completo via RPN
                saidaServidor.writeBytes("5\n");

                // Envia a expressão já convertida
                saidaServidor.writeBytes(expressaoRPN + "\n");

                // Garante que tudo foi enviado
                saidaServidor.flush();

                // Canal de leitura da resposta do servidor
                BufferedReader entradaServidor =
                        new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

                // Lê o resultado retornado pelo servidor
                String resultadoServidor = entradaServidor.readLine();

                // Exibe o resultado
                System.out.println("Resultado recebido do servidor: " + resultadoServidor);

                // Fecha a conexão com o servidor
                socketCliente.close();
            }

            // ===============================================================
            // MODO 2: Cliente calcula a expressão chamando o servidor várias vezes
            // ===============================================================
            else if (modoOperacao == 2) {

                System.out.println("\n[MODO 2] Cliente vai decompor e chamar o servidor várias vezes.");

                // Avaliação feita passo a passo: cliente envia diversas operações simples ao servidor
                double resultadoFinal = ClienteOperacoes.avaliarRPN_Cliente(expressaoRPN);

                System.out.println("Resultado final calculado pelo cliente (modo 2): " + resultadoFinal);
            }

            // Caso o usuário tenha digitado opção inválida
            else {
                System.out.println("Opção inválida!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
