import java.io.*;
import java.net.Socket;
import java.util.Stack;

public class ClienteOperacoes {

    // Avalia uma expressão RPN, chamando o servidor para cada operação
    public static double avaliarRPN_Cliente(String rpn) throws IOException {

        Stack<Double> pilha = new Stack<>();
        String[] tokens = rpn.split("\\s+");

        for (String token : tokens) {

            // Se número → empilha
            if (token.matches("\\d+")) {
                pilha.push(Double.parseDouble(token));
            }

            // Se operador → chama o servidor
            else if (token.matches("[+\\-*/]")) {

                double b = pilha.pop();
                double a = pilha.pop();

                double resultado = chamarServidorOperacao(token, a, b);

                pilha.push(resultado); // devolve para a pilha
            }
        }

        return pilha.pop();
    }

    // faz uma chamada remota ao servidor usando sockets
    private static double chamarServidorOperacao(String op, double a, double b) throws IOException {

        Socket socket = new Socket("127.0.0.1", 9090);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        int codigoOperacao = 0;
        switch (op) {
            case "+": codigoOperacao = 1; break;
            case "-": codigoOperacao = 2; break;
            case "*": codigoOperacao = 3; break;
            case "/": codigoOperacao = 4; break;
        }

        // envia operação e operandos
        out.writeBytes(codigoOperacao + "\n");
        out.writeBytes(a + "\n");
        out.writeBytes(b + "\n");
        out.flush();

        // recebe resposta
        double resultado = Double.parseDouble(in.readLine());

        socket.close();

        return resultado;
    }
}
