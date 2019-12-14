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

#define DHTTYPE DHT11

#define WLAN_SSID "KARACA"
#define WLAN_PASS "20232527"
#define FIREBASE_HOST "iot-with-asistant.firebaseio.com"
#define FIREBASE_AUTH "hbWOBvmle0ODqHJYgcOOkDzcsX4jkUuzJO9vv4QU"

#define RELAY1 1 // TX
#define RELAY2 3 // RX

#define DHT_PIN D8 //RXC-3

#define SS_PIN D4  //D2
#define RST_PIN D3 //D1

#define BUZZER D0


DHT dht(DHT_PIN, DHTTYPE);
MFRC522 mfrc522(SS_PIN, RST_PIN);   // Create MFRC522 instance.
LiquidCrystal_I2C lcd(0x27, 16, 2);
FirebaseData firebaseData;

String alarm;
String beep;
bool isLogged;

void setup()
{
  Serial.begin(9600);
  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);
  pinMode(BUZZER, OUTPUT);

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
  /*
    char str[] = "";
    lcd.print(str);
    if(sizeof(str) > 16){
    for(int i = 0; i < sizeof(str) - 16; i++) {
      lcd.scrollDisplayLeft();
      delay(500);
    }
    }*/
}

void loop()
{
  isAlarmed();
  if (!isLoggedIn()) {
    // NFC READ
    //////////////////////////
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
    controlRelay();
  }

  //controlRelay();
  //saveTempAndHumidity();
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
  String relay1;
  if (Firebase.getString(firebaseData, "/relay/relay1")) {
    if (firebaseData.dataType() == "string") {
      relay1 = firebaseData.stringData();
    }
  }
  String relay2;
  if (Firebase.getString(firebaseData, "/relay/relay2")) {
    if (firebaseData.dataType() == "string") {
      relay2 = firebaseData.stringData();
    }
  }
  digitalWrite(RELAY1, abs(relay1.toInt() - 1));
  //Serial.print(Firebase.getString("relay2") + "\n");
  digitalWrite(RELAY2, abs(relay2.toInt() - 1));
}


void saveTempAndHumidity() {
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  char tString[10];
  gcvt(t, 10, tString);

  /*
    Serial.print("Current humidity = ");
    Serial.print(h);
    Serial.print("%  ");
    Serial.print("temperature = ");
    Serial.print(t);
    Serial.println("C  ");*/
  Firebase.setString(firebaseData, "/temperature/current_temperature", tString);
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

void delayFirebase() {
  delay(100);
}
