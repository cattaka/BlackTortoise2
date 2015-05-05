#include <Arduino.h>
#include <SoftwareSerial.h>
#include <Servo.h>
#include "Geppa.h"


// =============================
// Debug setting
//#define DEBUG_CONSOLE_DUMP_RAW
//#define DEBUG_CONSOLE_DUMP_PACKET
//#define USE_DEBUG_CONSOLE

// =============================
// Constants for debug console
#define DEBUG_CONSOLE_RX A0
#define DEBUG_CONSOLE_TX A1
#ifdef DEBUG_CONSOLE_DUMP_PACKET
SoftwareSerial g_debug(DEBUG_CONSOLE_RX, DEBUG_CONSOLE_TX);
#endif
// =============================

// =============================
// Constants for servos
#define SERVO_NUM 2
#define SERVO_YAW   7
#define SERVO_PITCH  8
// =============================

// =============================
// Constants for twin motors
#define MOTOR_PIN_NUM     4
#define MOTOR_LEFT        3
#define MOTOR_LEFT_REV    5
#define MOTOR_RIGHT       11
#define MOTOR_RIGHT_REV   6
// =============================

// =============================
// Constants for LEDS
#define LED_0    2
#define LED_1    4
// =============================

// =============================
// Valiables
struct MotorPin {
  int pin;
  unsigned char value;
};
struct MotorPin motorPins[MOTOR_PIN_NUM];

struct MyServo {
  Servo servo;
  long pulseMin;
  long pulseMax;
  long currentValue;
  long value;
  long stepWidth;
};
struct MyServo myServos[SERVO_NUM];

void initMyServo(int idx, int pin, long pulseMin, long pulseMax, long initValue, long stepWidth) {
  myServos[idx].pulseMin = pulseMin;
  myServos[idx].pulseMax = pulseMax;
  myServos[idx].currentValue = initValue;
  myServos[idx].value = initValue;
  myServos[idx].stepWidth = stepWidth;
  myServos[idx].servo.attach(pin, pulseMin, pulseMax);
}
void initMotorPin(int idx, int pin) {
  pinMode(pin, OUTPUT);
  motorPins[idx].pin = pin;
  motorPins[idx].value = 0;
}
// =============================

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data);
Geppa g_geppa(handleRecvPacket);

void setup()
{
  {  // Initializing serial
  Serial.begin(115200);
  }
  {  // Initializing debug console
#ifdef DEBUG_CONSOLE_DUMP_PACKET
    g_debug.begin(9600);
    g_debug.println("Started");
#endif
  }
  {  // Initalizing LEDs
    pinMode(LED_0, OUTPUT);
    pinMode(LED_1, OUTPUT);
  }
  {  // Initializing servos
    initMyServo(0, SERVO_YAW,   550, 2350, 0x7F, 0xFF);
    initMyServo(1, SERVO_PITCH,  550, 2350, 0x7F, 0xFF);
  }
  {  // Initializing motors
    initMotorPin(0, MOTOR_LEFT);
    initMotorPin(1, MOTOR_LEFT_REV);
    initMotorPin(2, MOTOR_RIGHT);
    initMotorPin(3, MOTOR_RIGHT_REV);
  }
}

void loop()
{
  {  // Receive data from serial
    while(Serial.available() > 0) {
      unsigned char c = Serial.read();
      g_geppa.feedData(c);
#ifdef DEBUG_CONSOLE_DUMP_RAW
      g_debug.print(' ');
      g_debug.print(c, HEX);
      g_debug.print('-');
      g_debug.print(g_geppa.state, HEX);
#endif
    }
  }
  {  // Receive data from debug console.
#ifdef USE_DEBUG_CONSOLE
    if (g_debug.available() > 0) {
      while (g_debug.available() > 0) {
        unsigned char c = g_debug.read();
        g_geppa.feedData(c);
      }
    }
#endif
  }
  {  // Controlling servo angles
    for (int i=0;i<SERVO_NUM;i++) {
      long targetValue = myServos[i].value;
      if (myServos[i].currentValue + myServos[i].stepWidth < targetValue) {
        myServos[i].currentValue += myServos[i].stepWidth;
      }
      else if (myServos[i].currentValue - myServos[i].stepWidth > targetValue) {
        myServos[i].currentValue -= myServos[i].stepWidth;
      }
      else {
        myServos[i].currentValue = targetValue;
      }
      //int val = map(myServos[i].currentValue, 0, 0xFF, 0, 180);
      //myServos[i].servo.write(val);
      int val = map(myServos[i].currentValue, 0, 0xFF, myServos[i].pulseMin, myServos[i].pulseMax);
      myServos[i].servo.writeMicroseconds(val);
    }
  }
  {  // Controlling motors
    for (int i=0;i<MOTOR_PIN_NUM;i++) {
      analogWrite(motorPins[i].pin, motorPins[i].value);
    }
  }
  delay(10);
}

void handleRecvPacket(unsigned char packetType, unsigned char opCode, int dataLen, unsigned char* data) {
#ifdef DEBUG_CONSOLE_DUMP_PACKET
  g_debug.print('(');
  g_debug.print(packetType, HEX);
  g_debug.print(',');
  g_debug.print(opCode, HEX);
  g_debug.print(',');
  g_debug.print(dataLen, HEX);
  g_debug.print(',');
  for (int i=0;i<dataLen;i++) {
    if (data[i] < 0x10) {
      g_debug.print('0');
    }
    g_debug.print(data[i], HEX);
  }
  g_debug.print(')');
  g_debug.print('\n');
#endif
  if (packetType == 0x01) {
    if (opCode == 0) {
      // ECHO
      int len = dataLen;
      unsigned char t = packetType + opCode + (0xFF & len) + (0xFF & (len<<8));
      Serial.write(0x02);
      Serial.write(packetType);
      Serial.write(opCode);
      Serial.write((uint8_t)(0xFF & len));
      Serial.write((uint8_t)(0xFF & (len<<8)));
      Serial.write((uint8_t)t);
      for (int i=0;i<dataLen;i++) {
        Serial.write(data[i]);
      }
      Serial.write(0x03);
    }
    else if (opCode == 1) {
      // MOTER
      if (dataLen != MOTOR_PIN_NUM) {
#ifdef DEBUG_CONSOLE_DUMP_PACKET
        g_debug.print("dataLen is not MOTOR_PIN_NUM.\n");
#endif
      } else {
        motorPins[0].value = data[0];
        motorPins[1].value = data[1];
        motorPins[2].value = data[2];
        motorPins[3].value = data[3];
      }
    } else if (opCode == 2) {
      // SERVO_HEAD
      if (dataLen != SERVO_NUM) {
#ifdef DEBUG_CONSOLE_DUMP_PACKET
        g_debug.print("dataLen is not SERVO_NUM.\n");
#endif
      }
      else {
        for (int i=0;i<SERVO_NUM;i++) {
          myServos[i].value = data[i];
        }
      }
    }
    else if (opCode == 2) {
      // EYE_LEDS
      unsigned char val = data[0];
      digitalWrite(LED_0, (val & 1) ? HIGH:LOW);
      digitalWrite(LED_1, (val & 2) ? HIGH:LOW);
    }
    /*
    else if (opCode == 3) {
      // POSE
      if (dataLen != SERVO_NUM + 3) {
        g_debug.print("dataLen is not (SERVO_NUM + 2 + 1).\n");
      }
      else {
        int flags = (int)data[0] | (((int)data[1]) << 8);
        int led = data[SERVO_NUM+2];
        for (int i=0;i<SERVO_NUM;i++) {
          if (flags & (1<<i)) {
            myServos[i].value = data[i+2];
            g_debug.print("Servo idx=");
            g_debug.print(i, DEC);
            g_debug.print(", value=");
            g_debug.print(myServos[i].value, HEX);
            g_debug.print("\n");
          }
        }
        if (flags & (1<<SERVO_NUM)) {
          digitalWrite(LED_EYE_LEFT, (led & 1) ? HIGH:LOW);
          digitalWrite(LED_EYE_RIGHT, (led & 2) ? HIGH:LOW);
        }
      }
    }
    else if (opCode == 4) {
      g_debug.print("OK\n");
      // REQ_ACCEL
      int x = (analogRead(PIN_ACCEL_X) - OFFSET_ACCEL_X);
      int y = (analogRead(PIN_ACCEL_Y) - OFFSET_ACCEL_Y);
      int z = (analogRead(PIN_ACCEL_Z) - OFFSET_ACCEL_Z);
      byte data[] = {
        0x02,
        0x01,
        0x05,
        0x06,
        0x00,
        0x0C,
        (0xFF & (x >> 0)),
        (0xFF & (x >> 8)),
        (0xFF & (y >> 0)),
        (0xFF & (y >> 8)),
        (0xFF & (z >> 0)),
        (0xFF & (z >> 8)),
        0x03
      };
      int n = sizeof(data)/sizeof(byte);
      for (int i=0;i<n;i++) {
        Serial.write(data[i]);
      }
      //g_debug.print("(");
      //g_debug.print(x, DEC);
      //g_debug.print(",");
      //g_debug.print(y, DEC);
      //g_debug.print(",");
      //g_debug.print(z, DEC);
      //g_debug.print(")\n");
    }
  }
  else if (packetType == 0x69) {
    // The message from RBT-001
    if (opCode == 0x11) {
      g_debug.print("Transparent mode is started:");
      g_debug.print(data[0], HEX);
      g_debug.print(":");
      g_debug.print(data[1], HEX);
      g_debug.print("\n");
    }
    else if (opCode == 0x0C) {
      digitalWrite(LED_EYE_LEFT, HIGH);
      digitalWrite(LED_EYE_RIGHT, HIGH);
      g_debug.print("Bluetooth connection is established.");
      g_debug.print("\n");
    }
    else if (opCode == 0x0E) {
      digitalWrite(LED_EYE_LEFT, LOW);
      digitalWrite(LED_EYE_RIGHT, LOW);
      g_debug.print("SPP Link released.");
      g_debug.print("\n");
    }
    else if (opCode == 0x10) {
      // End UART break mode
      g_debug.print("End UART break mode.\n");
      byte data[] = {
        0x02,
        0x52,
        0x11,
        0x01,
        0x00,
        0x64,
        0x01,
        0x03
      };
      int n = sizeof(data)/sizeof(byte);
      for (int i=0;i<n;i++) {
        Serial.write(data[i]);
      }
    }
  */
  }
}



