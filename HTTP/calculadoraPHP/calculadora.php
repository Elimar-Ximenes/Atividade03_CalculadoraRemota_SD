<?php
// calculadora.php
// API calculadora remota via POST
// Operações suportadas:
// 1 = soma
// 2 = subtração
// 3 = multiplicação
// 4 = divisão
// 5 = expressão em RPN ("10 15 + 4 *")

header('Content-Type: application/json; charset=utf-8');

// importa funções auxiliares
require_once 'funcoes.php';

// dados recebidos
$oper1 = getPostFloat('oper1');
$oper2 = getPostFloat('oper2');
$operacao = isset($_POST['operacao']) ? intval($_POST['operacao']) : null;
$expressao = getPostString('expressao');

// estrutura padrão de resposta
$response = [
    'ok' => false,
    'erro' => null,
    'resultado' => null,
];

try {
    if ($operacao === null) {
        throw new Exception("Parâmetro 'operacao' obrigatório (1..5).");
    }

    switch ($operacao) {

        case 1: // soma
            if ($oper1 === null || $oper2 === null) {
                throw new Exception("Envie 'oper1' e 'oper2' para soma.");
            }
            $response['resultado'] = $oper1 + $oper2;
            break;

        case 2: // subtração
            if ($oper1 === null || $oper2 === null) {
                throw new Exception("Envie 'oper1' e 'oper2' para subtração.");
            }
            $response['resultado'] = $oper1 - $oper2;
            break;

        case 3: // multiplicação
            if ($oper1 === null || $oper2 === null) {
                throw new Exception("Envie 'oper1' e 'oper2' para multiplicação.");
            }
            $response['resultado'] = $oper1 * $oper2;
            break;

        case 4: // divisão
            if ($oper1 === null || $oper2 === null) {
                throw new Exception("Envie 'oper1' e 'oper2' para divisão.");
            }
            if ($oper2 == 0.0) {
                throw new Exception("Divisão por zero.");
            }
            $response['resultado'] = $oper1 / $oper2;
            break;

        case 5: // expressão em RPN
            if ($expressao === null || $expressao === "") {
                throw new Exception("Envie 'expressao' (RPN) para operacao=5.");
            }
            $response['resultado'] = evalRPN($expressao);
            break;

        default:
            throw new Exception("Operação inválida. Use 1..5.");
    }

    $response['ok'] = true;

} catch (Exception $ex) {
    // Em caso de erro, enviar mensagem informativa
    $response['erro'] = $ex->getMessage();
    // opcional: definir código HTTP 400 para erros de cliente
    http_response_code(400);
}

// envia resposta JSON (sem pretty print para facilitar parsing)
echo json_encode($response, JSON_UNESCAPED_UNICODE);
