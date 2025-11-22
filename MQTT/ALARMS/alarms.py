import paho.mqtt.client as mqtt
import json

BROKER = "10.250.115.42"

# Tópicos que o serviço ALARMS deve monitorar
TOPIC_SPIKE = "factory/events/temp_spike"
TOPIC_HIGH  = "factory/events/temp_high"

print("\n=== SERVIÇO DE ALARMES INICIADO ===")
print("Monitorando eventos do CAT...\n")


# ===============================================================
#   MQTT: Conexão ao broker
# ===============================================================
def on_connect(client, userdata, flags, rc, properties=None):
    print("Conectado ao broker!", rc)

    # Assina os eventos que o CAT publica
    client.subscribe(TOPIC_SPIKE)
    client.subscribe(TOPIC_HIGH)

    print(f"Inscrito em: {TOPIC_SPIKE}")
    print(f"Inscrito em: {TOPIC_HIGH}")


# ===============================================================
#   MQTT: Recebimento de eventos
# ===============================================================
def on_message(client, userdata, msg):
    payload = msg.payload.decode()

    try:
        data = json.loads(payload)
        event_type = data.get("event")

        print("\n================ ALARME RECEBIDO ================")

        if event_type == "TEMP_SPIKE":
            sensor = data.get("sensor_id")
            diff = data.get("difference")
            prev = data.get("previous_avg")
            curr = data.get("current_avg")

            print(f" AUMENTO REPENTINO DETECTADO!")
            print(f"→ Sensor: {sensor}")
            print(f"→ Diferença: {diff:.2f}")
            print(f"→ Média anterior: {prev:.2f}")
            print(f"→ Média atual: {curr:.2f}")

        elif event_type == "TEMP_HIGH":
            sensor = data.get("sensor_id")
            avg = data.get("average")

            print(f"ALERTA DE TEMPERATURA ALTA!")
            print(f"→ Sensor: {sensor}")
            print(f"→ Média registrada: {avg:.2f}")

        else:
            print("Evento desconhecido:", data)

        print("=================================================\n")

    except Exception as e:
        print("Erro ao processar evento:", e)


# ===============================================================
#   Inicialização
# ===============================================================
client = mqtt.Client(client_id="ALARM_SERVICE")
client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, 1883, 60)
client.loop_forever()
