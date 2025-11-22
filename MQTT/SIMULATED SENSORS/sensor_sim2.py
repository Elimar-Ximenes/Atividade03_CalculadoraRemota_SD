import paho.mqtt.client as mqtt
import time
import json

BROKER = "10.250.115.42"
TOPIC  = "factory/sensors/temp_sim2"

# FIX: forçar protocolo antigo para evitar erro do callback API
client = mqtt.Client(client_id="SIMULATED_SENSOR_2", protocol=mqtt.MQTTv311)

client.connect(BROKER, 1883, 60)

print("\n=== SENSOR SIMULADO 2 (EXPONENCIAL BASE 3) INICIADO ===")

value = 100       # valor inicial
exponent = 1      # expoente inicial

while True:
    # aumento exponencial: soma 3^exponent
    increment = 3 ** exponent
    value += increment

    if value > 300:
        value = 300  # trava no máximo

    payload = {
        "device_id": "temp_sim2",
        "luminosity_pct": value,   # interpretado como temperatura
        "timestamp": time.time()
    }

    client.publish(TOPIC, json.dumps(payload))
    print(f"→ Enviado temp_sim2: {value} (incremento usado: 3^{exponent} = {increment})")

    exponent += 1
    time.sleep(2)