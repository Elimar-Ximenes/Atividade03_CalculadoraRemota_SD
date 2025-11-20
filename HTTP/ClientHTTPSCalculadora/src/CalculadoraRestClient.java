// Importa classes necessárias para leitura de dados, conexão HTTP e manipulação de strings
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Stack;

// Classe principal do cliente REST
public class CalculadoraRestClient {

    public static void main(String[] args) {

        // Leitor de entrada do usuário
        Scanner scanner = new Scanner(System.in);

        // Menu inicial exibido ao usuário
        System.out.println("============== CALCULADORA REMOTA (REST) ==============");
        System.out.println("Escolha o modo de operação:");
        System.out.println("1 - Modo 1 (Servidor REST resolve a expressão completa)");
        System.out.println("2 - Modo 2 (Cliente resolve usando múltiplas chamadas REST)");
        System.out.print("Opção: ");

        // Lê o modo escolhido
        int modo = scanner.nextInt();
        scanner.nextLine();   // limpa buffer

        // Solicita a expressão ao usuário
        System.out.println("\nDigite a expressão (ex: (10+5)*3 ): ");
        String expressaoInfixa = scanner.nextLine();

        // Converte a expressão de infixa para RPN
        String expressaoRPN = RPNConverter.toRPN(expressaoInfixa);
        System.out.println("\nExpressão convertida para RPN: " + expressaoRPN);

        try {

            // --- MODO 1 ---
            if (modo == 1) {

                // Envia TODA a expressão para o servidor em um único POST
                double resultado = calcularExpressaoCompletaREST(expressaoRPN);

                System.out.println("\nResultado recebido do servidor REST: " + resultado);
            }

            // --- MODO 2 ---
            else if (modo == 2) {

                // Avalia a expressão passo a passo chamando o servidor para cada operação
                double resultado = avaliarRPN_Cliente(expressaoRPN);

                System.out.println("\nResultado final calculado pelo cliente (modo 2): " + resultado);
            }

            else {
                System.out.println("Opção inválida!");
            }

        } catch (Exception e) {
            System.out.println("Erro ao comunicar com servidor REST!");
            e.printStackTrace();
        }
    }

    // ============================================================
    // MODO 1 – Envia a expressão completa em RPN ao servidor REST
    // ============================================================
    private static double calcularExpressaoCompletaREST(String rpn) throws Exception {

        // Cria o objeto URI com o endereço do servidor
        URI uri = new URI("http://localhost:8080/calculadora.php");

        // Converte URI para URL
        URL url = uri.toURL();

        // Abre a conexão HTTP
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Define que será utilizado o método POST
        conn.setRequestMethod("POST");

        // Permite envio de dados no corpo da requisição
        conn.setDoOutput(true);

        // Informa o tipo de conteúdo enviado (formulário HTML)
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Monta os parâmetros de envio: operacao=5 indica cálculo completo
        String params = "operacao=5&expressao=" + URLEncoder.encode(rpn, StandardCharsets.UTF_8);

        // Envia os parâmetros para o servidor via POST
        conn.getOutputStream().write(params.getBytes());

        // Lê a resposta JSON do servidor
        String json = lerJSON(conn);

        // Extrai apenas o valor do campo "resultado"
        String resultado = extrairCampoJSON(json, "resultado");

        // Converte o resultado de String para double
        return Double.parseDouble(resultado);
    }

    // ============================================================
    // MODO 2 – Cliente resolve usando múltiplas chamadas ao servidor
    // ============================================================
    public static double avaliarRPN_Cliente(String rpn) throws Exception {

        // Pilha utilizada para avaliar a expressão RPN
        Stack<Double> pilha = new Stack<>();

        // Divide a expressão RPN em tokens
        String[] tokens = rpn.split("\\s+");

        // Percorre cada token
        for (String token : tokens) {

            // Se for um número, empilha
            if (token.matches("\\d+(\\.\\d+)?")) {
                pilha.push(Double.parseDouble(token));
            }

            // Se for um operador (+ - * /), faz a chamada REST
            else if (token.matches("[+\\-*/]")) {

                // Desempilha operandos
                double b = pilha.pop();
                double a = pilha.pop();

                // Chama o servidor REST para realizar a operação
                double resultado = chamarServidorREST(token, a, b);

                // Empilha o resultado
                pilha.push(resultado);
            }
        }

        // Ao final sobra apenas o resultado final
        return pilha.pop();
    }

    // ============================================================
    // Operação simples enviada ao servidor via POST (modo 2)
    // ============================================================
    private static double chamarServidorREST(String operador, double a, double b) throws Exception {

        // Cria o objeto URI
        URI uri = new URI("http://localhost:8080/calculadora.php");

        // Converte URI para URL
        URL url = uri.toURL();

        // Abre a conexão HTTP
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Configura o método POST
        conn.setRequestMethod("POST");

        // Habilita envio de dados
        conn.setDoOutput(true);

        // Informa o tipo de conteúdo
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Traduz o operador para o código esperado pelo servidor
        int codigo = switch (operador) {
            case "+" -> 1;  // soma
            case "-" -> 2;  // subtração
            case "*" -> 3;  // multiplicação
            case "/" -> 4;  // divisão
            default -> throw new IllegalArgumentException("Operador inválido");
        };

        // Monta os parâmetros enviados na requisição POST
        String params = "operacao=" + codigo + "&oper1=" + a + "&oper2=" + b;

        // Envia para o servidor
        conn.getOutputStream().write(params.getBytes());

        // Lê a resposta JSON
        String json = lerJSON(conn);

        // Extrai o valor do campo resultado
        String resultado = extrairCampoJSON(json, "resultado");

        // Retorna como double
        return Double.parseDouble(resultado);
    }

    // ============================================================
    // Função para ler a resposta JSON retornada pelo servidor
    // ============================================================
    private static String lerJSON(HttpURLConnection conn) throws Exception {

        // Abre leitor para ler a resposta do servidor
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        // Buffer para montar a string completa do JSON
        StringBuilder sb = new StringBuilder();

        // Lê linha por linha
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha);
        }

        // Fecha o leitor
        reader.close();

        // Retorna o JSON completo como string
        return sb.toString();
    }

    // ============================================================
    // Função simples que extrai um campo específico do JSON
    // ============================================================
    private static String extrairCampoJSON(String json, String campo) {

        // Monta o padrão buscado, ex.: "resultado":
        String chave = "\"" + campo + "\":";

        // Caso o campo não exista
        if (!json.contains(chave))
            return "0";

        // Divide o JSON a partir da chave
        String[] partes = json.split(chave);

        // Pega o valor após a chave
        String valor = partes[1].trim();

        // Remove possíveis caracteres excedentes
        valor = valor.split(",")[0].replace("}", "").trim();

        // Remove aspas caso existam
        return valor.replace("\"", "");
    }
}