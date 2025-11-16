import java.util.Stack;

public class Calculadora {

    public String sayHello(String nome, String sobrenome) {
        return "Fala " + nome + " " + sobrenome;
    }

    public double soma(double oper1, double oper2) {
        return oper1 + oper2;
    }

    public double subtrai(double oper1, double oper2) {
        return oper1 - oper2;
    }

    public double multiplica(double oper1, double oper2) {
        return oper1 * oper2;
    }

    public double divide(double oper1, double oper2) {
        return oper1 / oper2;
    }

    // ============================================
    // Avaliação de expressão em Notação Polonesa Reversa (RPN)
    // Exemplo de entrada: "10 15 + 4 *"
    // ============================================
    public double calculaRPN(String expressao) {

        Stack<Double> pilha = new Stack<>();
        String[] tokens = expressao.split(" ");

        for (String t : tokens) {
            switch (t) {
                case "+":
                    pilha.push(pilha.pop() + pilha.pop());
                    break;

                case "-":
                    double b = pilha.pop();
                    double a = pilha.pop();
                    pilha.push(a - b);
                    break;

                case "*":
                    pilha.push(pilha.pop() * pilha.pop());
                    break;

                case "/":
                    double divisor = pilha.pop();
                    double dividendo = pilha.pop();
                    pilha.push(dividendo / divisor);
                    break;

                default:
                    // Se não for operador, é número
                    pilha.push(Double.parseDouble(t));
            }
        }

        return pilha.pop();
    }
}
