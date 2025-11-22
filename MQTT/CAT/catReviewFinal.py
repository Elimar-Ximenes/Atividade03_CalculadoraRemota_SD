import paho.mqtt.client as mqtt
from collections import defaultdict, deque
import json

BROKER = "10.250.115.42" 

# Escuta todos os sensores em factory/sensors/<id>
TOPIC_SENSOR = "factory/sensors/#"

# Eventos publicados pelo CAT: temperatura > 200 e diferença de 5 graus entre medias
TOPIC_SPIKE  = "factory/events/temp_spike"
TOPIC_HIGH   = "factory/events/temp_high"

# Janela de 60 valores por sensor (equivale a 120s)
windows = defaultdict(lambda: deque(maxlen=60))

# Última média conhecida por sensor
last_avgs = defaultdict(lambda: None)

print("\n=== CAT MULTI-SENSOR INICIADO ===")


# ===============================================================
#   MQTT: Conexão ao broker
# ===============================================================
def on_connect(client, userdata, flags, rc, properties=None):
    print("Conectado ao broker!", rc)
    client.subscribe(TOPIC_SENSOR)
    print(f"Inscrito em: {TOPIC_SENSOR}")


# ===============================================================
#   MQTT: Recebimento dos sensores
# ===============================================================
def on_message(client, userdata, msg):
    payload = msg.payload.decode()

    try:
        data = json.loads(payload)

        sensor_id = data.get("device_id")
        temp      = data.get("luminosity_pct")

        # Log técnico mínimo
        print(f"\n[{sensor_id}] Leitura recebida: {temp}")

        if sensor_id is None or temp is None:
            return

        windows[sensor_id].append(temp)

        # Só começa a calcular média após 5 valores
        if len(windows[sensor_id]) < 5:
            return

        avg = sum(windows[sensor_id]) / len(windows[sensor_id])
        last_avg = last_avgs[sensor_id]

        # Log técnico
        print(f"  Média atual [{sensor_id}]: {avg:.2f}")

        # --- Detecta SPIKE ---
        if last_avg is not None:
            diff = avg - last_avg
            if abs(diff) >= 5:
                publish_spike(sensor_id, avg, last_avg, diff)

        last_avgs[sensor_id] = avg

        # --- Detecta temperatura alta (>200) ---
        if avg > 200:
            publish_high(sensor_id, avg)

    except Exception as e:
        print("Erro ao decodificar JSON:", e)


# ===============================================================
#   Publica evento de SPIKE
# ===============================================================
def publish_spike(sensor_id, avg, last_avg, diff):
    event = {
        "event": "TEMP_SPIKE",
        "sensor_id": sensor_id,
        "difference": diff,
        "previous_avg": last_avg,
        "current_avg": avg
    }

    client.publish(TOPIC_SPIKE, json.dumps(event))


# ===============================================================
#   Publica evento de TEMPERATURA ALTA
# ===============================================================
def publish_high(sensor_id, avg):
    event = {
        "event": "TEMP_HIGH",
        "sensor_id": sensor_id,
        "average": avg
    }

    client.publish(TOPIC_HIGH, json.dumps(event))


# ===============================================================
#   Inicialização
# ===============================================================
client = mqtt.Client(client_id="CAT_MULTISENSOR")
client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, 1883, 60)
client.loop_forever()
