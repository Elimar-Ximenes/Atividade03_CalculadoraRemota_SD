import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class CalculadoraServerSocket {

    public static void main(String[] args) {

        ServerSocket welcomeSocket;
        Calculadora calc = new Calculadora();

        try {
            welcomeSocket = new ServerSocket(9090);

            System.out.println("Servidor no ar...");

            while (true) {

                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Nova conexão recebida!");

                BufferedReader socketEntrada =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream socketOutput =
                        new DataOutputStream(connectionSocket.getOutputStream());

                while (true) {

                    // Lê a operação enviada pelo cliente
                    String operacao = socketEntrada.readLine();

                    // Se o cliente encerrar, sair do loop
                    if (operacao == null || operacao.equals("FIM")) {
                        System.out.println("Conexão encerrada pelo cliente.");
                        break;
                    }

                    String resposta = "";

                    switch (operacao) {

                        case "1": // soma
                            resposta = "" + calc.soma(
                                    Double.parseDouble(socketEntrada.readLine()),
                                    Double.parseDouble(socketEntrada.readLine())
                            );
                            break;

                        case "2": // subtração
                            resposta = "" + calc.subtrai(
                                    Double.parseDouble(socketEntrada.readLine()),
                                    Double.parseDouble(socketEntrada.readLine())
                            );
                            break;

						case "3": // multiplicação
							resposta = "" + calc.multiplica(
									Double.parseDouble(socketEntrada.readLine()),
									Double.parseDouble(socketEntrada.readLine())
							);
							break;

						case "4": // divisão
							resposta = "" + calc.divide(
									Double.parseDouble(socketEntrada.readLine()),
									Double.parseDouble(socketEntrada.readLine())
							);
							break;

                        case "5": // cálculo de expressão completa via RPN
                            String expressaoRPN = socketEntrada.readLine();
                            resposta = "" + calc.calculaRPN(expressaoRPN);
                            break;

                        default:
                            resposta = "Operação inválida";
                            break;
                    }

                    // Envia o resultado de volta ao cliente
                    socketOutput.writeBytes(resposta + "\n");
                    socketOutput.flush();
                }

                connectionSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
