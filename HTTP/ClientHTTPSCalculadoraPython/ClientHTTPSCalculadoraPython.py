import requests

class CalculadoraRest:
    def __init__(self, base_url):
        self.base_url = base_url

    def operacao(self, param1, param2):
        url = f"{self.base_url}/soma/{param1}/{param2}"
        response = requests.post(url)
        if response.status_code == 200:
            return response.json().get('result')
        else:
            return f"Erro: {response.status_code}, {response.text}"

    def soma(self, param1, param2):
        return self.operacao(param1, param2)

    def subtracao(self, param1, param2):
        return 0

    def multiplicacao(self, param1, param2):
        return 0

    def divisao(self, param1, param2):
        return 0


if __name__ == "__main__":
    calculadora = CalculadoraRest("https://calculadora-fxpc.onrender.com/operation")

    # Exemplo de uso:
    print("Soma: 10 + 5 =", calculadora.soma(10, 5))
   