package sun;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.Packet;
import sun.CounterPackets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class test {
    static NetworkInterface[] devices = JpcapCaptor.getDeviceList();

    public static void getDevices() {

        //获得网卡设备列表
        if (devices.length == 0) {
            System.out.println( "No networkInterface!" );
            return;
        }

        //输出网卡信息
        for (int i = 0; i < devices.length; i++) {
            System.out.println("NetworkInterface" + i + ": " + devices[i].name + "("
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
            System.out.println( "\n" );
        }

    }

    public static int chooseNetworkInterface () {

        //选择网卡进行监听
        Scanner scan = new Scanner( System.in );
        System.out.println( "Please choose a NetworkInterface number: " + "[0-" + (devices.length - 1) + "]");
        int index = scan.nextInt();
        if ( index > devices.length - 1 ){
            System.out.println( "Please choose the right NetworkInterface !" );
            return -1;
        }

        return index;
    }

    public static void oneByOneReceiver(int index ) throws IOException {

        JpcapCaptor captor = JpcapCaptor.openDevice(devices[index], 65535,
                false, 20);
        PrintWriter pw = new PrintWriter(new FileWriter(new File("packets.txt")));
        // 设置过滤器
        captor.setFilter("ip", true);
        int counter = 0;
        Packet packet;
        long startTime = System.currentTimeMillis();
        while (startTime + 2 * 60 * 10 >= System.currentTimeMillis()) {
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

    public static void main( String[] args ) throws IOException{

        //获得网卡列表，以及相关数据
        getDevices();
        //选中网卡进行监听
        int index = chooseNetworkInterface();
        if ( index == -1 ) {
            return;
        }

        //抓包
        oneByOneReceiver( index );


    }
}
