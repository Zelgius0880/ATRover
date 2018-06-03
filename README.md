# ATRover
This is a small project to make a rover with Android Things and a Raspberry Pi 3.
It is controlled with Bluetooth and an Android app included in the **mobile** module.


# Hardware (Deprecated)
The hardware connection documentation need to be modified.


- Raspberry Py 3
- L293D Channel Motor Driver  ([Link](https://www.banggood.com/1pc-L293D-L293-L293B-DIP-SOP-Push-Pull-Four-Channel-Motor-Driver-IC-p-909310.html?rmmds=myorder&cur_warehouse=USA)). [Datasheet](http://www.ti.com/lit/ds/symlink/l293.pdf)
- 3 Color LED  ([Link](http://www.dx.com/p/diy-arduino-3-color-rgb-smd-led-module-black-135046#.WqT0VOjOWUk)) 
- 2x [N20 DC 6V 150RPM](http://www.dx.com/p/425975) Motor
- Lithium Battery ([Link](https://www.banggood.com/V1_0-Lithium-Battery-Expansion-Board-For-Cellphone-Raspberry-Pi-3-Model-B-Pi-2B-B-p-1059297.html?rmmds=myorder&cur_warehouse=CN))
- 9v Battery
- Keyes IR obstacle avoidance sensor


## Connecting
### LD293 - Raspberry
|**Raspberry**|**L293D**|**Other**|**Other**|**L293D**|**Raspberry**|
|--|--|--|--|--|--|
|BMC19|1,2EN|||Vcc|5v
|BCM6|1A|||4A|BCM16
| |1Y|Motor1|Motor2|4Y|
| |GND|||GND|
| |GND|||GND|
| |2Y|Motor1|Motor2|3Y|
|BMC5|2A|||3A|BCM20
||Vcc|Battery||3,4EN|BCM12
### LEDs - Raspberry
Red->PW10
Blue ->PWM1
Green ->BCM17

# 3D Files

I modified files from [SMARS](https://www.thingiverse.com/thing:2662828) to fit with a Raspberry Pi3. You can find all the files in the 3D folder