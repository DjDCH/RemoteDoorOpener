#define REDPIN   11
#define GREENPIN 10

#define FADESPEED 10
#define SLOWSPEED 3000

#define VOID_FLAG  B00000001
#define CLOSE_FLAG B00000010
#define OPEN_FLAG  B00000100

void setup() {
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);

  Serial.begin(9600);
}

void loop() {
  if (Serial.available() > 0)  {
      react(Serial.read());
  }
  delay(FADESPEED);
}

void react(byte data) {
  if ((data & VOID_FLAG) == VOID_FLAG) {
    do_void();
  }
  if ((data & CLOSE_FLAG) == CLOSE_FLAG) {
    do_close();
  }
  if ((data & OPEN_FLAG) == OPEN_FLAG) {
    do_open();
    delay(SLOWSPEED);
    do_close();
  }
}

void do_open() {
  digitalWrite(REDPIN, LOW);
  digitalWrite(GREENPIN, HIGH);
}

void do_close() {
  digitalWrite(GREENPIN, LOW);
  digitalWrite(REDPIN, HIGH);
}

void do_void() {
  digitalWrite(GREENPIN, LOW);
  digitalWrite(REDPIN, LOW);
}
