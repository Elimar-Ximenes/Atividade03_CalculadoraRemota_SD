import paho.mqtt.client as mqtt
import json
from collections import deque

BROKER = "10.250.115.42"   # <<< IP DA SUA MÁQUINA LOCAL
#Topico publicado pelo esp, aqui representaremos como temperatura
TOPIC_SENSOR = "factory/sensors/temperature"
# Topico publicado pelo client paho representando mudança de temperatura  
TOPIC_SPIKE  = "factory/events/temp_spike" 

# armazena últimas 60 leituras (120 segundos)
window = deque(maxlen=60)

last_avg = None  # última média calculada


# ===============================================================
#   MQTT CALLBACKS
# ===============================================================
def on_connect(client, userdata, flags, reason_code, properties=None):
    # Callback executado quando o serviço CAT estabelece conexão com o broker MQTT.
    # Aqui o CAT se inscreve no tópico onde os sensores (reais ou simulados)
    # publicam leituras de temperatura/luminosidade.
    # A partir dessa inscrição, todas as mensagens recebidas serão tratadas em on_message().
    print("Conectado ao broker!", reason_code)
    client.subscribe(TOPIC_SENSOR)
    print("Inscrito em:", TOPIC_SENSOR)

# Disparado quando chega nova mensagem do sensor
def on_message(client, userdata, msg):
    global last_avg

    # Decodifica o JSON recebido
    payload = msg.payload.decode()
    print("\n RECEBIDO DO SENSOR:", payload)

    # Extrai valor de temperatura/luminosidade
    try:
        data = json.loads(payload)
        temp = data.get("luminosity_pct")     # usando luminosidade como temperatura
        ts   = data.get("timestamp")

        if temp is None:
            print("⚠ Payload não contém luminosity_pct")
            return

        # adiciona à janela
        window.append(temp)

        # só calcula média se já temos dados suficientes
        if len(window) >= 2:
            avg = sum(window) / len(window)
            print(f"→ Média atual: {avg:.2f} com {len(window)} valores")

            # compara com última média
            if last_avg is not None:
                diff = abs(avg - last_avg)
                if diff > 5:
                    print("EVENTO: aumento repentino detectado!")
                    publish_spike(avg, last_avg, diff)
            
            last_avg = avg

    except Exception as e:
        print("Erro ao processar JSON:", e)


# ===============================================================
#   PUBLICAR EVENTO DE AUMENTO REPENTINO
# ===============================================================
# Dispara quando há aumento repentino
def publish_spike(avg, last_avg, diff):
    # Monta JSON do evento de spike
    event = {
        "event": "TEMP_SPIKE",
        "difference": diff,
        "previous_avg": last_avg,
        "current_avg": avg
    }

    # Publica no tópico de alertas
    client.publish(TOPIC_SPIKE, json.dumps(event))
    print(" Evento publicado em factory/events/temp_spike:", event)


# ===============================================================
#   MAIN
# ===============================================================
client = mqtt.Client(client_id="CAT_REAL_SENSOR")

client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, 1883, 60)
client.loop_forever()