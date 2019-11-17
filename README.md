# vanhack-mobodexter-sergioozaki


## Environment Setup

```
https://github.com/wimvanderbauwhede/limited-systems/wiki/Raspbian-%22stretch%22-for-Raspberry-Pi-3-on-QEMU

sudo apt-get install qemu-system-x86
sudo apt install qemu-system-arm

git clone https://github.com/dhruvvyas90/qemu-rpi-kernel.git

https://downloads.raspberrypi.org/raspbian_lite_latest

unzip 2019-09-26-raspbian-buster-lite.zip

qemu-system-arm \
   -kernel kernel-qemu-4.19.50-buster \
   -dtb qemu-rpi-kernel/versatile-pb.dtb \
   -m 256 -M versatilepb -cpu arm1176 \
   -serial stdio \
   -append "rw console=ttyAMA0 root=/dev/sda2 rootfstype=ext4  loglevel=8 rootwait fsck.repair=yes memtest=1" \
   -drive file=2019-09-26-raspbian-buster-lite.img,format=raw \
   -redir tcp:5022::22  \
   -no-reboot


qemu-system-arm \
   -kernel kernel-qemu-4.19.50-buster \
   -dtb qemu-rpi-kernel/versatile-pb.dtb \
   -m 256 -M versatilepb -cpu arm1176 \
   -serial stdio \
   -append "rw console=ttyAMA0 root=/dev/sda2 rootfstype=ext4  loglevel=8 rootwait fsck.repair=yes memtest=1" \
   -drive file=2019-09-26-raspbian-buster-lite.img,format=raw \
   -net nic -net user,hostfwd=tcp::2222-:22
   -no-reboot


sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils

```
