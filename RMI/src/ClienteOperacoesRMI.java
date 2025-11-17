import java.rmi.RemoteException;
import java.util.Stack;

public class ClienteOperacoesRMI {

    public static double avaliarRPN_Cliente(String rpn, ICalculadora calc) throws RemoteException {

        Stack<Double> pilha = new Stack<>();
        String[] tokens = rpn.split("\\s+");

        for (String t : tokens) {

            if (t.matches("\\d+")) {
                pilha.push(Double.parseDouble(t));
            } else {

                double b = pilha.pop();
                double a = pilha.pop();
                double resultado = 0.0;

                switch (t) {
                    case "+": resultado = calc.soma(a, b); break;
                    case "-": resultado = calc.subtrai(a, b); break;
                    case "*": resultado = calc.multiplica(a, b); break;
                    case "/": resultado = calc.divide(a, b); break;
                }

                pilha.push(resultado);
            }
        }

        return pilha.pop();
    }
}
