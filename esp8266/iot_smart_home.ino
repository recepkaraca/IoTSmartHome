/*
   ----------------------------------
               MFRC522      Node
               Reader/PCD   MCU
   Signal      Pin          Pin
   ----------------------------------
   RST/Reset   RST          D1 (GPIO5)
   SPI SS      SDA(SS)      D2 (GPIO4)
   SPI MOSI    MOSI         D7 (GPIO13)
   SPI MISO    MISO         D6 (GPIO12)
   SPI SCK     SCK          D5 (GPIO14)
   3.3V        3.3V         3.3V
   GND         GND          GND
*/


// SCL D1
// SDA D2

#include "FirebaseESP8266.h"
#include <ESP8266WiFi.h>
#include "DHT.h"
#include <SPI.h>
#include <MFRC522.h>
#include <LiquidCrystal_I2C.h>
#include <stdlib.h>
#include <math.h>

#define DHTTYPE DHT11

#define WLAN_SSID "KARACA"
#define WLAN_PASS "20232527"
#define FIREBASE_HOST "iot-with-asistant.firebaseio.com"
#define FIREBASE_AUTH "hbWOBvmle0ODqHJYgcOOkDzcsX4jkUuzJO9vv4QU"

#define RELAY1 1 // TX
#define RELAY2 3 // RX

#define DHT_PIN D3 //RXC-3

#define SS_PIN D4  //D2
#define RST_PIN D3 //D1

#define BUZZER D0

#define MOTION_SENSOR D8


DHT dht(DHT_PIN, DHTTYPE);
MFRC522 mfrc522(SS_PIN, RST_PIN);   // Create MFRC522 instance.
LiquidCrystal_I2C lcd(0x27, 16, 2);
FirebaseData firebaseData;

String alarm;
String beep;
String relay1;
String relay2;
float minTemp;
float maxTemp;
float currentTemp;
bool isLogged;

bool isRelay2Locked;
bool isRelay2Locked2 = false;
bool isRelay1Locked;

void setup()
{
  //Serial.begin(9600);
  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  pinMode(MOTION_SENSOR, INPUT);

  wifiConnect();
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  //6. Optional, set number of error retry
  Firebase.setMaxRetry(firebaseData, 3);

  //7. Optional, set number of error resumable queues
  Firebase.setMaxErrorQueue(firebaseData, 30);

  SPI.begin();
  mfrc522.PCD_Init();
  lcd.init();
  lcd.backlight();
}

void loop()
{
  if (!isLoggedIn()) {
    // NFC READ
    //////////////////////////
    isAlarmed();
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Waiting for NFC");
    lcd.setCursor(0, 1);
    lcd.print("or RFID card.");
    // Look for new cards
    // Look for new cards
    if ( ! mfrc522.PICC_IsNewCardPresent())
    {
      return;
    }
    // Select one of the cards
    if ( ! mfrc522.PICC_ReadCardSerial())
    {
      return;
    }
    //Show UID on serial monitor
    //Serial.println();
    //Serial.print(" UID tag :");
    String content = "";
    byte letter;
    for (byte i = 0; i < mfrc522.uid.size; i++)
    {
      //Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
      //Serial.print(mfrc522.uid.uidByte[i], HEX);
      content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " "));
      content.concat(String(mfrc522.uid.uidByte[i], HEX));
    }
    content.toUpperCase();
    String nfcID = content.substring(1);
    nfcID.replace(" ", ":");

    //Serial.println();
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(nfcID);

    Firebase.setString(firebaseData, "/nfc/nfc_id", nfcID);
    delay(2000);
    if (!isLoggedIn()) {
      Firebase.setString(firebaseData, "/nfc/nfc_id", "");
    } else {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Welcome");
      lcd.setCursor(0, 1);
      String nfcUsername;
      if (Firebase.getString(firebaseData, "/nfc/nfc_username")) {
        if (firebaseData.dataType() == "string") {
          nfcUsername = firebaseData.stringData();
        }
      }
      lcd.print(nfcUsername);
      delay(3000);
    }
    delay(500);
  } else {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Logged In");
    lcd.setCursor(0, 1);
    String nfcUsername;
    if (Firebase.getString(firebaseData, "/nfc/nfc_username")) {
      if (firebaseData.dataType() == "string") {
        nfcUsername = firebaseData.stringData();
      }
    }
    lcd.print(nfcUsername);

    String lcdBacklight;
    if (Firebase.getString(firebaseData, "/lcd/lcd_backlight")) {
      if (firebaseData.dataType() == "string") {
        lcdBacklight = firebaseData.stringData();
      }
    }
    if(lcdBacklight == "1") {
      lcd.backlight();
    }else {
      lcd.noBacklight();
    }

    saveTempAndHumidity();
    readTemps();
    if (currentTemp <= maxTemp && currentTemp >= minTemp && !isRelay2Locked2) {
      isRelay2Locked = true;
      digitalWrite(RELAY2, LOW);
    } else if(!(currentTemp <= maxTemp && currentTemp >= minTemp) && !isRelay2Locked2){
      isRelay2Locked = false;
      digitalWrite(RELAY2, HIGH);
    }
    motionSensor();
    controlRelay();
    receiveLCDMessage();
  }
}


void wifiConnect()
{
  // Connect to WiFi access point.
  //Serial.println(); Serial.println();
  //Serial.print("Connecting to ");
  //Serial.println(WLAN_SSID);

  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    //Serial.print(".");
  }
  //Serial.println();

  //Serial.println("WiFi connected");
  //Serial.println("IP address: ");
  //Serial.println(WiFi.localIP());
}

void controlRelay() {
  //Serial.print(Firebase.getString("relay1") + "\n");
  if (Firebase.getString(firebaseData, "/relay/relay1") && !isRelay1Locked) {
    if (firebaseData.dataType() == "string") {
      relay1 = firebaseData.stringData();
    }
    digitalWrite(RELAY1, abs(relay1.toInt() - 1));
  }

  if (Firebase.getString(firebaseData, "/relay/relay2") && !isRelay2Locked) {
    if (firebaseData.dataType() == "string") {
      relay2 = firebaseData.stringData();
    }
    if(relay2 == "1"){
      isRelay2Locked2 = true;
    }else {
      isRelay2Locked2 = false;
    }
    digitalWrite(RELAY2, abs(relay2.toInt() - 1));
  }
}


void saveTempAndHumidity() {
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  delay(200);
  char tString[10];
  gcvt(t, 10, tString);
  char hString[10];
  gcvt(h, 10, hString);
  Firebase.setString(firebaseData, "/temperature/current_temperature", tString);
  Firebase.setString(firebaseData, "/temperature/humidity", hString);
  currentTemp = t;
}

void readTemps() {
  if (Firebase.getString(firebaseData, "/temperature/max_temperature")) {
    if (firebaseData.dataType() == "string") {
      maxTemp = firebaseData.stringData().toFloat();
    }
  }
  if (Firebase.getString(firebaseData, "/temperature/min_temperature")) {
    if (firebaseData.dataType() == "string") {
      minTemp = firebaseData.stringData().toFloat();
    }
  }
}

void motionSensor() {
  String enabled;
  String pirRelay1;
  if (Firebase.getString(firebaseData, "/pir/pir_enabled")) {
    if (firebaseData.dataType() == "string") {
      enabled = firebaseData.stringData();
    }
  }
  if (enabled == "1") {
    if (Firebase.getString(firebaseData, "/pir/pir_relay1")) {
      if (firebaseData.dataType() == "string") {
        pirRelay1 = firebaseData.stringData();
      }
      if (pirRelay1 == "1" && (digitalRead(MOTION_SENSOR) == HIGH)) {
        digitalWrite(RELAY1, LOW);
      } else {
        digitalWrite(RELAY1, HIGH);
      }
      isRelay1Locked = true;
    }
  } else {
    isRelay1Locked = false;
  }

}

void receiveLCDMessage() {
  String message;
  if (Firebase.getString(firebaseData, "/lcd/lcd_value")) {
    if (firebaseData.dataType() == "string") {
      message = firebaseData.stringData();
      lcd.clear();
    }
  }
  for (int i = 0; i < ceil(message.length() / 32.0f); i++) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(message.substring(0 + (i * 32), 16 + (i * 32)));
    lcd.setCursor(0, 1);
    lcd.print(message.substring(16 + (i * 32), 32 + (i * 32)));
    delay(2000);
  }

}

void readRFID() {

}

bool isLoggedIn () {
  if (Firebase.getString(firebaseData, "/beep_sound")) {
    if (firebaseData.dataType() == "string") {
      beep = firebaseData.stringData();
    }
  }
  if (beep == "1") {
    beepSound();
    Firebase.setString(firebaseData, "beep_sound", "0");
  }
  String nfcUsername;
  if (Firebase.getString(firebaseData, "/nfc/nfc_username")) {
    if (firebaseData.dataType() == "string") {
      nfcUsername = firebaseData.stringData();
    }
  }
  if (nfcUsername == "") {
    return false;
  } else {
    return true;
  }

}

void isAlarmed() {
  if (Firebase.getString(firebaseData, "/alarm")) {
    if (firebaseData.dataType() == "string") {
      alarm = firebaseData.stringData();
    }
  }
  if (alarm == "1") {
    digitalWrite(BUZZER, HIGH);
    for (int i = 0; i < 2; i++) {
      lcd.clear();
      if (i == 0) {
        lcd.noBacklight();
        lcd.setCursor(0, 0);
        lcd.print("Alarm");
        lcd.setCursor(0, 1);
        lcd.print("Emergency");
        lcd.backlight();
        delay(100);
        lcd.noBacklight();
        delay(100);
        lcd.backlight();
        delay(100);
        lcd.noBacklight();
        delay(100);
      } else {
        lcd.backlight();
        lcd.setCursor(0, 0);
        lcd.print("Emergency");
        lcd.setCursor(0, 1);
        lcd.print("Alarm");
        delay(100);
        lcd.noBacklight();
        delay(100);
        lcd.backlight();
        delay(100);
        lcd.noBacklight();
        delay(100);
        lcd.backlight();
      }
    }
  } else {
    digitalWrite(BUZZER, LOW);
    lcd.backlight();
  }
}

void beepSound() {
  digitalWrite(BUZZER, HIGH);
  delay(100);
  digitalWrite(BUZZER, LOW);
  delay(100);
  digitalWrite(BUZZER, HIGH);
  delay(100);
  digitalWrite(BUZZER, LOW);
  delay(100);
  digitalWrite(BUZZER, HIGH);
  delay(100);
  digitalWrite(BUZZER, LOW);
}
