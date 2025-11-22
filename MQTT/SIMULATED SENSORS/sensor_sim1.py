import paho.mqtt.client as mqtt
import json
import time

BROKER = "10.250.115.42"
TOPIC  = "factory/sensors/temp_sim1"

client = mqtt.Client()
client.connect(BROKER, 1883, 60)

print("\n=== SENSOR SIMULADO 1 INICIADO (EXPONENCIAL) ===")

value = 100
step = 3          # primeiros aumentos constantes
exp_factor = 1    # começa o modo exponencial depois


while True:
    # envia leitura atual
    payload = {
        "device_id": "temp_sim1",
        "luminosity_pct": value,
        "timestamp": time.time()
    }

    client.publish(TOPIC, json.dumps(payload))
    print(f"→ Enviado temp_sim1: {value}")

    time.sleep(2)

    # após 100 → sobe +3 até ~115, depois começa a exponencial
    if value < 115:
        value += step
    else:
        value += exp_factor     # incrementa com expoente crescente
        exp_factor *= 2         # dobra o passo (2, 4, 8, 16, 32...)

    # trava no máximo
    if value > 300:
        value = 300
