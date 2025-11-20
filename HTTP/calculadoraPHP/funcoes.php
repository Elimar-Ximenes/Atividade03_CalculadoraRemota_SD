<?php
// funcoes.php

// Helper: lê valor POST e converte para float quando possível
function getPostFloat($key) {
    if (!isset($_POST[$key])) return null;
    $raw = str_replace(',', '.', trim($_POST[$key]));
    if ($raw === '') return null;
    if (!is_numeric($raw)) return null;
    return floatval($raw);
}

// Helper: lê string POST ou null
function getPostString($key) {
    return isset($_POST[$key]) ? trim($_POST[$key]) : null;
}

// Função para avaliar expressão em RPN
// Ex: "10 15 + 4 *"
function evalRPN(string $rpn) {
    $tokens = preg_split('/\s+/', trim($rpn));
    if ($tokens === false) throw new Exception("RPN inválida.");

    $stack = [];

    foreach ($tokens as $t) {
        if ($t === '') continue;

        // trata números (ponto ou vírgula)
        $num = str_replace(',', '.', $t);
        if (is_numeric($num)) {
            array_push($stack, floatval($num));
            continue;
        }

        // operador
        if (in_array($t, ['+', '-', '*', '/'])) {
            if (count($stack) < 2) {
                throw new Exception("RPN mal formada: operandos insuficientes.");
            }

            $b = array_pop($stack);
            $a = array_pop($stack);

            switch ($t) {
                case '+': $res = $a + $b; break;
                case '-': $res = $a - $b; break;
                case '*': $res = $a * $b; break;
                case '/':
                    if ($b == 0.0) throw new Exception("Divisão por zero.");
                    $res = $a / $b;
                    break;
            }

            array_push($stack, $res);
            continue;
        }

        // inválido
        throw new Exception("Token inválido na RPN: '$t'");
    }

    if (count($stack) !== 1) {
        throw new Exception("RPN mal formada: sobrou coisa na pilha.");
    }

    return $stack[0];
}
