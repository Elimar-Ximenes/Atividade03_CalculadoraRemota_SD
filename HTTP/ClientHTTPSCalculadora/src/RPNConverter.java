import java.util.Stack;

public class RPNConverter {

    public static String toRPN(String expressao) {

        expressao = expressao.replaceAll("\\s+", ""); // remove espaços

        StringBuilder saida = new StringBuilder();
        Stack<Character> pilha = new Stack<>();

        for (int i = 0; i < expressao.length(); i++) {
            char c = expressao.charAt(i);

            // Se for número, podemos permitir números com mais de um dígito
            if (Character.isDigit(c)) {
                while (i < expressao.length() && Character.isDigit(expressao.charAt(i))) {
                    saida.append(expressao.charAt(i));
                    i++;
                }
                saida.append(" ");
                i--;
            }

            // Se operador
            else if (isOperador(c)) {
                while (!pilha.isEmpty() &&
                        prioridade(pilha.peek()) >= prioridade(c)) {
                    saida.append(pilha.pop()).append(" ");
                }
                pilha.push(c);
            }

            // Abre parêntese
            else if (c == '(') {
                pilha.push(c);
            }

            // Fecha parêntese
            else if (c == ')') {
                while (!pilha.isEmpty() && pilha.peek() != '(') {
                    saida.append(pilha.pop()).append(" ");
                }
                pilha.pop(); // remove '('
            }
        }

        // esvazia a pilha
        while (!pilha.isEmpty()) {
            saida.append(pilha.pop()).append(" ");
        }

        return saida.toString().trim();
    }

    private static boolean isOperador(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int prioridade(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }
}