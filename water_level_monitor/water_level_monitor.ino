#define Trig 2 //引脚Trig 连接 IO D2
#define Echo 3 //引脚Echo 连接 IO D3

float cm; // 距离变量
float temp;
unsigned int i = 0;
unsigned int count = 0;

void setup() {
  // 初始化串口和 GPIO
  Serial.begin(115200);
  pinMode(Trig, OUTPUT);
  pinMode(Echo, INPUT);
}

float getDistance() {
  digitalWrite(Trig, LOW); // 给 Trig 发送一个低电平
  delayMicroseconds(2);  // 等待 2 微秒
  digitalWrite(Trig, HIGH); // 给 Trig 发送一个高电平
  delayMicroseconds(10);  // 等待 10 微秒
  digitalWrite(Trig, LOW);  // 给 Trig 发送一个低电平
  temp = float(pulseIn(Echo, HIGH));  // 存储回波等待时间
  return (temp * 17)/1000; // 把回波时间换算成 cm
}

void loop() {
  cm = getDistance();
  //Serial.print("level,site=water value=");
  Serial.println(cm);
  delay(125);
 }
