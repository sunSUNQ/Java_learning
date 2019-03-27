package sun;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.Packet;

public class SnifferDemo {
    public static void  main( String[] args ) throws IOException{
        Scanner console = new Scanner(System.in);
        getDevices();//显示PC机上可用的网卡的信息
        System.out.println("请输入网卡号<0-2>");
        int num = console.nextInt();//获取用户要打开的网卡号
        System.out.println("请输入抓包时间（分钟）");
        int minuets = console.nextInt();//获取用户抓包时间
        oneByOneReceiver(num, minuets);//调用抓包方法
    }

    // 获取网络接口列表
    public static void getDevices() {
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        for (int i = 0; i < devices.length; i++) {
            System.out.println(i + ": " + devices[i].name + "("
                    + devices[i].description + ")");
            System.out.println(" datalink: " + devices[i].datalink_name + "("
                    + devices[i].datalink_description + ")");
            System.out.print(" MAC address:");
            for (byte b : devices[i].mac_address)
                System.out.print(Integer.toHexString(b & 0xff) + ":");
            System.out.println();
            for (NetworkInterfaceAddress a : devices[i].addresses)
                System.out.println(" address:" + a.address + " " + a.subnet
                        + " " + a.broadcast);
        }
    }

    // 使用逐个捕获方法, 从网络接口捕获数据包
    public static void oneByOneReceiver(int index, int time) throws IOException {
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        JpcapCaptor captor = JpcapCaptor.openDevice(devices[index], 65535,
                false, 20);
        PrintWriter pw = new PrintWriter(new FileWriter(new File("packets.txt")));
        // 设置过滤器
        captor.setFilter("ip", true);
        int counter = 0;
        Packet packet;
        long startTime = System.currentTimeMillis();
        while (startTime + time * 60 * 10 >= System.currentTimeMillis()) {
            packet = captor.getPacket();
            System.out.println(packet);
            if (packet != null) {
                String ips = packet.toString().split("\\s+")[1];
                pw.write(ips);
                pw.println();
                counter++;
            }
        }
        pw.close();
        CounterPackets cp = new CounterPackets();
        cp.readPackets();
        cp.print();
        System.out.println("PacketNumbers:" + counter);
    }

}
