# ATRover
This is a small project to make a rover with Android Things and a Raspberry Pi 3.
It is controlled with Bluetooth and an Android app included in the **mobile** module. 
The rove is controlled with Bluetooh. When you use the mobile app, it will ask you to make the mobile device discovable. 
The LED of the Rover will come blue when it's ready to connect. It can take 1 or 2 min to etablish the connection.

# Hardware

- Raspberry Py 3
- L293D Channel Motor Driver  ([Link](https://www.banggood.com/1pc-L293D-L293-L293B-DIP-SOP-Push-Pull-Four-Channel-Motor-Driver-IC-p-909310.html?rmmds=myorder&cur_warehouse=USA)). [Datasheet](http://www.ti.com/lit/ds/symlink/l293.pdf)
- 3 Color LED  ([Link](http://www.dx.com/p/diy-arduino-3-color-rgb-smd-led-module-black-135046#.WqT0VOjOWUk)) 
- 2x [N20 DC 6V 150RPM](http://www.dx.com/p/425975) Motor
- Lithium Battery ([Link](https://www.banggood.com/V1_0-Lithium-Battery-Expansion-Board-For-Cellphone-Raspberry-Pi-3-Model-B-Pi-2B-B-p-1059297.html?rmmds=myorder&cur_warehouse=CN))
- 9v Battery

## Connecting
### LD293 - Raspberry
![Connection between LD293D and Raspberry](https://github.com/Zelgius0880/ATRover/blob/master/images/Untitled%20Sketch%202_bb.png?raw=true)
### LD293 - Motor
![LD293 - Motor](https://github.com/Zelgius0880/ATRover/blob/master/images/Untitled%20Sketch_bb.png?raw=true)
### All Connections
![enter image description here](https://github.com/Zelgius0880/ATRover/blob/master/images/RoverWiring_bb.png?raw=true)
# 3D Files

I modified files from [SMARS](https://www.thingiverse.com/thing:2662828) to fit with a Raspberry Pi3. You can find all the files in the 3D folder
