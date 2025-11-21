import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente HTTP para a calculadora PHP.
 *
 * Comentários detalhados (linha a linha) especialmente nas partes de comunicação,
 * conforme exigido pelo professor.
 */
public class CalculadoraRestClient {

    // Configurações de retry
    private static final int MAX_ATTEMPTS = 4; // número máximo de tentativas
    private static final long BASE_DELAY_MS = 300; // tempo base para backoff exponencial
    private static final Random RAND = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Cabeçalho de interface textual mínimo
        System.out.println("============== CALCULADORA REMOTA (REST) ==============");
        System.out.println("Escolha o modo de operação:");
        System.out.println("1 - Modo 1 (Servidor REST resolve a expressão completa)");
        System.out.println("2 - Modo 2 (Cliente resolve usando múltiplas chamadas REST)");
        System.out.print("Opção: ");

        int modo = scanner.nextInt();
        scanner.nextLine();   // limpa buffer do Scanner

        System.out.println("\nDigite a expressão (ex: (10+5)*3 ): ");
        String expressaoInfixa = scanner.nextLine();

        // Converte a expressão infixa para RPN usando RPNConverter
        String expressaoRPN;
        try {
            expressaoRPN = RPNConverter.toRPN(expressaoInfixa);
        } catch (Exception e) {
            System.out.println("Erro ao converter expressão: " + e.getMessage());
            return;
        }

        System.out.println("\nExpressão convertida para RPN: " + expressaoRPN);

        try {
            if (modo == 1) {
                // modo 1: envia a RPN inteira para o servidor calcular
                double resultado = calcularExpressaoCompletaREST(expressaoRPN);
                System.out.println("\nResultado recebido do servidor REST: " + resultado);
            } else if (modo == 2) {
                // modo 2: cliente avalia RPN chamando o servidor para cada operação
                double resultado = avaliarRPN_Cliente(expressaoRPN);
                System.out.println("\nResultado final calculado pelo cliente (modo 2): " + resultado);
            } else {
                System.out.println("Opção inválida!");
            }
        } catch (Exception e) {
            System.out.println("Erro ao comunicar com servidor REST: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MODO 1 – envia a expressão completa em RPN ao servidor REST (com retry)
    private static double calcularExpressaoCompletaREST(String rpn) throws Exception {
        // monta parâmetros codificados para POST
        String params = "operacao=5&expressao=" + URLEncoder.encode(rpn, StandardCharsets.UTF_8);
        String urlStr = "http://localhost:8080/calculadora.php";

        // faz POST com retry e recebe JSON como String
        String json = doPostWithRetry(urlStr, params);

        // Verifica se "ok" é true; se false, lança com a mensagem do campo "erro"
        boolean ok = parseJsonBoolean(json, "ok");
        if (!ok) {
            String erro = parseJsonString(json, "erro");
            throw new Exception("Servidor respondeu erro: " + erro);
        }

        // extrai campo resultado (número)
        String valor = parseJsonValue(json, "resultado");
        return parseResultadoParaDouble(valor);
    }

    // MODO 2 – avalia a expressão em RPN localmente, chamando servidor para cada operação básica (com retry)
    public static double avaliarRPN_Cliente(String rpn) throws Exception {
        Stack<Double> pilha = new Stack<>();
        String[] tokens = rpn.split("\\s+");

        for (String token : tokens) {

            // número simples: inteiro, negativo ou decimal com ponto/vírgula
            if (token.matches("[+-]?\\d+(\\.\\d+)?") || token.matches("[+-]?\\d+(,\\d+)?")) {
                pilha.push(Double.parseDouble(token.replace(",", ".")));
            }

            // operador básico
            else if (token.matches("[+\\-*/]")) {
                if (pilha.size() < 2)
                    throw new IllegalArgumentException("RPN mal formada: operandos insuficientes.");

                double b = pilha.pop();
                double a = pilha.pop();

                double resultado = chamarServidorREST(token, a, b);
                pilha.push(resultado);
            }

            else {
                throw new IllegalArgumentException("Token inválido: " + token);
            }
        }

        if (pilha.size() != 1)
            throw new IllegalStateException("Erro ao avaliar a expressão.");

        return pilha.pop();
    }


    // Chama operação simples no servidor (modo 2) — usa retry dentro de doPostWithRetry
    private static double chamarServidorREST(String operador, double a, double b) throws Exception {
        int codigo;
        switch (operador) {
            case "+": codigo = 1; break;
            case "-": codigo = 2; break;
            case "*": codigo = 3; break;
            case "/": codigo = 4; break;
            default: throw new IllegalArgumentException("Operador inválido: " + operador);
        }

        // cria params codificados
        String params = "operacao=" + codigo
                + "&oper1=" + URLEncoder.encode(String.valueOf(a), StandardCharsets.UTF_8)
                + "&oper2=" + URLEncoder.encode(String.valueOf(b), StandardCharsets.UTF_8);

        String urlStr = "http://localhost:8080/calculadora.php";

        // faz POST com retry
        String json = doPostWithRetry(urlStr, params);

        // verifica se ok
        boolean ok = parseJsonBoolean(json, "ok");
        if (!ok) {
            String erro = parseJsonString(json, "erro");
            throw new Exception("Servidor respondeu erro: " + erro);
        }

        // extrai resultado
        String valor = parseJsonValue(json, "resultado");
        return parseResultadoParaDouble(valor);
    }

    // Realiza POST com política de retry exponencial e tratamento de códigos HTTP
    private static String doPostWithRetry(String urlStr, String params) throws Exception {
        int attempt = 0;
        Exception lastEx = null;

        while (attempt < MAX_ATTEMPTS) {
            attempt++;
            try {
                return doPost(urlStr, params);
            } catch (Exception ex) {
                lastEx = ex;

                // se a mensagem da exceção contém HTTP_ERROR_CODE:XXX, decidimos retry ou não
                String msg = ex.getMessage() == null ? "" : ex.getMessage();

                // tenta extrair código HTTP se presente no texto da exception
                Integer code = extractHttpCode(msg);

                if (code != null) {
                    if (code >= 400 && code < 500) {
                        // erro do cliente; não faz retry
                        throw ex;
                    }
                    // 5xx -> podemos tentar novamente
                }

                // backoff exponencial com jitter simples
                long delay = BASE_DELAY_MS * (1L << (attempt - 1));
                long jitter = RAND.nextInt(100);
                long totalDelay = delay + jitter;

                System.out.println("Tentativa " + attempt + " falhou: " + ex.getMessage() + ". Retentando em " + totalDelay + "ms");
                try { Thread.sleep(totalDelay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw ie; }
            }
        }

        throw new Exception("Todas tentativas falharam.", lastEx);
    }

    // Faz o POST e retorna o JSON como String; lança Exception em caso de erro (inclui código HTTP)
    private static String doPost(String urlStr, String params) throws Exception {
        URI uri = new URI(urlStr);
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // timeout para evitar bloqueio indefinido
        conn.setConnectTimeout(5000); // 5s connect
        conn.setReadTimeout(5000);    // 5s read

        // define método HTTP POST
        conn.setRequestMethod("POST");

        // indica que haverá envio de corpo na requisição
        conn.setDoOutput(true);

        // informa o tipo de conteúdo (form-urlencoded com charset)
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        // codifica payload em bytes UTF-8
        byte[] payload = params.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", Integer.toString(payload.length));

        // Envia os dados (usa try-with-resources para garantir fechamento do OutputStream)
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
            os.flush();
        }

        // obtém código de status HTTP da resposta
        int status = conn.getResponseCode();

        // seleciona InputStream apropriado (entrada normal para 2xx, error stream para outros)
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        // lê todo o corpo da resposta como String (UTF-8)
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n'); // preserva quebras para facilitar debug (parser lida com whitespace)
            }
        }

        String response = sb.toString().trim();

        if (status < 200 || status >= 300) {
            // lança com código para doPostWithRetry decidir reter ou não
            throw new Exception("HTTP_ERROR_CODE:" + status + " BODY:" + response);
        }

        return response;
    }

    // utilitário: extrai código HTTP de mensagem de exceção previamente formatada
    private static Integer extractHttpCode(String msg) {
        if (msg == null) return null;
        Pattern p = Pattern.compile("HTTP_ERROR_CODE:(\\d+)");
        Matcher m = p.matcher(msg);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // Extrai campo simples do JSON - método genérico e robusto o suficiente para este uso.
    // Lê string, número, boolean ou null.
    private static String parseJsonValue(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;

        // localiza ':' após a chave
        int colon = json.indexOf(':', idx);
        if (colon == -1) return null;

        // pega substring após ':'
        String after = json.substring(colon + 1).trim();

        if (after.startsWith("\"")) {
            // valor é string: pega até próxima aspas não escapada
            int i = 1;
            StringBuilder sb = new StringBuilder();
            while (i < after.length()) {
                char c = after.charAt(i);
                if (c == '"' && after.charAt(i-1) != '\\') break;
                sb.append(c);
                i++;
            }
            return sb.toString();
        } else {
            // número, boolean ou null — pega até ',' ou '}' ou ']'
            String token = after.split("[,}\\]]", 2)[0].trim();
            return token;
        }
    }

    // Extrai string (ou null) do JSON para um campo
    private static String parseJsonString(String json, String field) {
        String v = parseJsonValue(json, field);
        if (v == null || v.equals("null")) return null;
        return v;
    }

    // Extrai boolean do JSON (se presente)
    private static boolean parseJsonBoolean(String json, String field) {
        String v = parseJsonValue(json, field);
        if (v == null) return false;
        v = v.trim().toLowerCase();
        return v.equals("true") || v.equals("1");
    }

    // Converte string extraída do JSON para double (trata null e valores vazios)
    private static double parseResultadoParaDouble(String s) {
        if (s == null) throw new IllegalArgumentException("Resultado nulo no JSON");
        s = s.trim();
        if (s.equals("null") || s.equals("")) throw new IllegalArgumentException("Resultado indefinido no JSON");
        return Double.parseDouble(s);
    }
}
