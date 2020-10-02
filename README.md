记录需求分析过程。

1. 网关设备发现（设备发现不加密）  
组播的方式给网关发送:
```json
{"cmd": "whois"}
```
这样网关就会应答  
```json
{"cmd":"iam","port":"9898","sid":"7811dcf981c4","model":"gateway","proto_version":"1.1.2","ip":"192.168.1.145"}
```
这个就是网关发现的过程，这个响应报文包含了网关的 ip 地址，和网关设备 id  
网关的心跳  
```json
{"cmd":"heartbeat","model":"gateway","sid":"7811dcf981c4","short_id":"0","token":"mYa79UqLGsfd20R6","data":"{\"ip\":\"192.168.1.145\"}"}
```


读插座的状态:  
```json
{"cmd":"read", "sid":"158d000234727c"}
```
返回:
```json
{"cmd":"read_ack","model":"plug","sid":"158d000234727c","short_id":38455,"data":"{\"voltage\":3600,\"status\":\"on\",\"inuse\":\"1\",\"power_consumed\":\"429112\",\"load_power\":\"1478.04\"}"}
```
插座的心跳  
```json
{"cmd":"heartbeat","model":"plug","sid":"158d000234727c","short_id":38455,"data":"{\"voltage\":3600,\"status\":\"off\",\"inuse\":\"0\",\"power_consumed\":\"429112\",\"load_power\":\"0.00\"}"}
```

温湿度感器的设备 id 是: 158d0002028518  
```json
{"cmd":"read", "sid":"158d0002028518"}
```
人体传感器的设备 id 是: 158d000223f246
```json
{"cmd":"read", "sid":"158d000223f246"}
```

