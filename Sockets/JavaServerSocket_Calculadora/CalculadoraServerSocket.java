import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class CalculadoraServerSocket {

    public static void main(String[] args) {

        ServerSocket servidorSocket; // servidor que vai ficar ouvindo as conexões
        Calculadora calculadora = new Calculadora(); // objeto que realiza as operações matemáticas

        try {
            // Cria o servidor na porta 9090
            servidorSocket = new ServerSocket(9090);

            System.out.println("Servidor no ar...");

            // Loop infinito: servidor sempre ativo esperando novas conexões
            while (true) {

                // Aguarda o cliente conectar
                Socket socketConexao = servidorSocket.accept();
                System.out.println("Nova conexão recebida!");

                // Leitor para receber mensagens do cliente
                BufferedReader entradaCliente =
                        new BufferedReader(new InputStreamReader(socketConexao.getInputStream()));

                // Escritor para enviar respostas ao cliente
                DataOutputStream saidaCliente =
                        new DataOutputStream(socketConexao.getOutputStream());

                // Loop interno: trata múltiplas operações enviadas pelo mesmo cliente
                while (true) {

                    // Lê o código da operação enviada pelo cliente
                    String codigoOperacao = entradaCliente.readLine();

                    // Se o cliente enviou "FIM" ou fechou a conexão, encerra o atendimento
                    if (codigoOperacao == null || codigoOperacao.equals("FIM")) {
                        System.out.println("Conexão encerrada pelo cliente.");
                        break;
                    }

                    String respostaOperacao = ""; // variável que armazenará o resultado

                    // Verifica qual operação foi solicitada
                    switch (codigoOperacao) {

                        case "1": // operação de soma
                            respostaOperacao = "" + calculadora.soma(
                                    Double.parseDouble(entradaCliente.readLine()), // lê operando 1
                                    Double.parseDouble(entradaCliente.readLine())  // lê operando 2
                            );
                            break;

                        case "2": // operação de subtração
                            respostaOperacao = "" + calculadora.subtrai(
                                    Double.parseDouble(entradaCliente.readLine()),
                                    Double.parseDouble(entradaCliente.readLine())
                            );
                            break;

                        case "3": // operação de multiplicação
                            respostaOperacao = "" + calculadora.multiplica(
                                    Double.parseDouble(entradaCliente.readLine()),
                                    Double.parseDouble(entradaCliente.readLine())
                            );
                            break;

                        case "4": // operação de divisão
                            respostaOperacao = "" + calculadora.divide(
                                    Double.parseDouble(entradaCliente.readLine()),
                                    Double.parseDouble(entradaCliente.readLine())
                            );
                            break;

                        case "5": // cálculo de expressão completa enviada em RPN
                            String expressaoRPN = entradaCliente.readLine(); // lê string RPN
                            respostaOperacao = "" + calculadora.calculaRPN(expressaoRPN);
                            break;

                        default: // operação inválida
                            respostaOperacao = "Operação inválida";
                            break;
                    }

                    // Envia o resultado devolta para o cliente
                    saidaCliente.writeBytes(respostaOperacao + "\n");
                    saidaCliente.flush();
                }

                // Fecha o socket do cliente após terminar o atendimento
                socketConexao.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}