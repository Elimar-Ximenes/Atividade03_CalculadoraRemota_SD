#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// ======== WIFI ========
const char* ssid = "Galaxy M15 5G 2E72";
const char* password = "123456789";

// ======== MQTT BROKER ========
const char* mqtt_server = "10.250.115.42";
const int mqtt_port = 1883;

WiFiClient espClient;
PubSubClient client(espClient);

// ======== MQTT TOPICS ========
String dataTopic    = "factory/sensors/temperature";
String commandTopic = "factory/commands/light_sensor_esp32";
String responseTopic = "factory/commands/light_sensor_esp32/response";

// ======== SENSOR ========
#define PIN_LDR 32

bool publishingEnabled = true; // controla se publica ou não

// =================================================
//   CONECTAR WIFI
// =================================================
void setup_wifi() {
  Serial.println();
  Serial.print("Conectando ao WiFi: ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi conectado!");
  Serial.print("IP local ESP32: ");
  Serial.println(WiFi.localIP());
}

// =================================================
//   PROCESSAR COMANDO MQTT
// =================================================
void processCommand(char* payload, unsigned int length) {
  StaticJsonDocument<256> doc;

  char jsonBuffer[256];
  memcpy(jsonBuffer, payload, length);
  jsonBuffer[length] = '\0';

  Serial.print("JSON recebido bruto: ");
  Serial.println(jsonBuffer);

  DeserializationError error = deserializeJson(doc, jsonBuffer);
  if (error) {
    Serial.println("Erro ao ler JSON!");
    return;
  }

  String commandType = doc["command_type"].as<String>();
  String requestId   = doc["request_id"].as<String>();

  Serial.println("Comando OK decodificado!");
  Serial.println("Tipo: " + commandType);
  Serial.println("Req ID: " + requestId);

  if (commandType == "TURN_IDLE") {
    publishingEnabled = false;
  }
  if (commandType == "TURN_ACTIVE") {
    publishingEnabled = true;
  }

  // monta resposta
  StaticJsonDocument<256> resp;
  resp["device_id"] = "ldr_esp32_001";
  resp["request_id"] = requestId;
  resp["success"] = true;
  resp["message"] = "OK";
  resp["status"] = publishingEnabled ? "ACTIVE" : "IDLE";

  String out;
  serializeJson(resp, out);

  client.publish(responseTopic.c_str(), out.c_str());
}

// =================================================
//   CALLBACK MQTT
// =================================================
void callback(char* topic, byte* payload, unsigned int length) {
  if (String(topic) == commandTopic) {
    processCommand((char*)payload, length);
  }
}

// =================================================
//   RECONNECT MQTT
// =================================================
void reconnect() {
  while (!client.connected()) {
    Serial.print("Conectando ao broker MQTT... ");

    if (client.connect("ESP32_LDR")) {
      Serial.println("Conectado!");
      client.subscribe(commandTopic.c_str());
    } else {
      Serial.print("Falhou, rc=");
      Serial.print(client.state());
      Serial.println(" tentando novamente...");
      delay(2000);
    }
  }
}

// =================================================
//   PUBLICAR SENSOR
// =================================================
int getLuminosityPercent(int raw) {
  return map(raw, 4095, 0, 0, 300); 
}

String classifyLight(int pct) {
  if (pct < 100) return "CONTROLADO";
  if (pct <= 200) return "TOLERÁVEL";
  return "PERIGO";
}

void publishLightEvent() {
  int raw = analogRead(PIN_LDR);
  int pct = getLuminosityPercent(raw);
  String status = classifyLight(pct);

  StaticJsonDocument<256> doc;
  doc["device_id"] = "ldr_esp32_001";
  doc["raw_value"] = raw;
  doc["luminosity_pct"] = pct;
  doc["status"] = status;
  doc["timestamp"] = millis();

  String payload;
  serializeJson(doc, payload);

  client.publish(dataTopic.c_str(), payload.c_str());
  Serial.println(payload);
}

// =================================================
//   SETUP
// =================================================
void setup() {
  Serial.begin(115200);
  setup_wifi();

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  pinMode(PIN_LDR, INPUT);
}

// =================================================
//   LOOP PRINCIPAL
// =================================================
void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  if (publishingEnabled) {
    publishLightEvent();
  }
  //Publica as "temperatura" a cada 2 segundos
  delay(2000);
}
