import java.io.*;
import java.net.Socket;
import java.util.Stack;

public class ClienteOperacoes {

    // Método que avalia uma expressão em Notação Polonesa Reversa (RPN)
    // Chamando o servidor para realizar cada operação aritmética encontrada.
    public static double avaliarRPN_Cliente(String rpn) throws IOException {

        // Pilha usada para processar a expressão RPN
        Stack<Double> pilha = new Stack<>();

        // Divide a expressão em tokens separados por espaço
        String[] tokens = rpn.split("\\s+");

        // Percorre todos os tokens da expressão
        for (String token : tokens) {

            // Caso o token seja um número → empilha
            if (token.matches("\\d+")) {
                pilha.push(Double.parseDouble(token));
            }

            // Caso seja um operador → precisa chamar o servidor
            else if (token.matches("[+\\-*/]")) {

                // Desempilha os dois últimos valores para usar como operandos
                double b = pilha.pop();  // segundo operando
                double a = pilha.pop();  // primeiro operando

                // Faz uma chamada ao servidor para executar a operação
                double resultado = chamarServidorOperacao(token, a, b);

                // Empilha o resultado de volta para continuar o processamento
                pilha.push(resultado);
            }
        }

        // Ao final, restará apenas o resultado final da expressão
        return pilha.pop();
    }

    // Método responsável por enviar uma operação ao servidor via socket
    // e receber o resultado calculado remotamente.
    private static double chamarServidorOperacao(String op, double a, double b) throws IOException {

        // Abre conexão TCP com o servidor de operações na porta 9090
        Socket socket = new Socket("127.0.0.1", 9090);

        // Canal de saída para enviar dados ao servidor
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Canal de entrada para ler a resposta do servidor
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Define um código inteiro para cada operação
        int codigoOperacao = 0;
        switch (op) {
            case "+": codigoOperacao = 1; break;
            case "-": codigoOperacao = 2; break;
            case "*": codigoOperacao = 3; break;
            case "/": codigoOperacao = 4; break;
        }

        // Envia linha a linha:
        // 1 → código da operação
        // 2 → valor 'a'
        // 3 → valor 'b'
        out.writeBytes(codigoOperacao + "\n");
        out.writeBytes(a + "\n");
        out.writeBytes(b + "\n");
        out.flush();

        // Lê do servidor o resultado já calculado
        double resultado = Double.parseDouble(in.readLine());

        // Fecha o socket depois da operação
        socket.close();

        return resultado;
    }
}
