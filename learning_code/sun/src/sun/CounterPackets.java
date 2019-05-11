package sun;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CounterPackets {
    ArrayList<String> ips;//存放读取文本信息
    HashMap<String,Integer> counter;//存放地址和对应的数据包的数量

    public CounterPackets() {
        counter = new HashMap<String, Integer>();
    }

    //从packet.txt.中读取捕获到的IP数据包信息
    public void readPackets() throws IOException,FileNotFoundException {
        ips = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("packets.txt"));
        while (br.read() != -1) {
            ips.add(br.readLine());
        }
        counter();
    }

    //统计流量
    public void counter() {
        for (int i = 0; i < ips.size(); i++) {
            String[] result = getIps(ips.get(i));
            String index = result[0] + " " + result[1];
            setNumber(index);
        }
    }
    //获取源地址和目的地址
    public String[] getIps (String ips) {
        StringBuffer sb = new StringBuffer(ips);
        for (int i = 0; i < ips.length(); i++) {
            if (ips.charAt(i) == '/' || ips.charAt(i) == '>' || ips.charAt(i) == '-') {
                sb.replace(i, i + 1, " ");
            }
        }
        ips = sb.toString().trim();
        return ips.split("\\s+");
    }

    //数目叠加
    public void setNumber(String index) {
        if (counter.containsKey(index)) {
            int value = counter.get(index);
            counter.put(index,  value+1);
        } else {
            counter.put(index, 1);
        }
    }
    //将结果打印到控制台
    public void print() {
        System.out.println("Source IP " + "       " + "Target IP " + "     " + "PacketNumber");
        Iterator it = counter.keySet().iterator();
        while (it.hasNext()) {
            String index = (String)it.next();
            String[] ips = index.split("\\s+");
            String srcIp = String.format("%-6s", ips[0]);
            String dstIp = ips[1];
            int number = counter.get(index);
            System.out.println(srcIp + "        " + dstIp + "          " + number);
        }
    }
}