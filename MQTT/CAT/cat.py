import paho.mqtt.client as mqtt
import json

BROKER = "10.250.115.42"
TOPIC  = "factory/sensors/light_sensor_esp32"

print("=== CAT MONITOR INICIADO === ===")
print(f"Escutando tópico: {TOPIC}\n")

# ---------- CALLBACKS NO NOVO FORMATO DO PAHO 2.x -------------

def on_connect(client, userdata, flags, reason_code, properties=None):
    print("Conectado ao broker. Código:", reason_code)
    client.subscribe(TOPIC)
    print("Inscrito no tópico:", TOPIC)

def on_message(client, userdata, msg):
    print("\n📩 RECEBIDO DO SENSOR:")
    payload = msg.payload.decode()
    print(payload)

    try:
        data = json.loads(payload)
        pct    = data.get("luminosity_pct")
        status = data.get("status")
        ts     = data.get("timestamp")

        print(f" → Luminosidade: {pct}% | Status: {status} | ts: {ts}")

    except Exception as e:
        print("⚠️ Erro ao decodificar JSON:", e)

# -------------------------------------------------------------

client = mqtt.Client(client_id="CAT_TESTE")  # sem callback_api_version !

client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, 1883, 60)
client.loop_forever()
