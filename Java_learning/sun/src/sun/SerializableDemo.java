package sun;

import java.io.*;


public class SerializableDemo {

    public static void main(String[] args) throws Exception, IOException{

        //初始化对象
        user user_one = new user();
        user_one.setName("sunqing");
        System.out.println(user_one.getName());

        //序列化对象到文件中
        FileOutputStream fos = new FileOutputStream("sun");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(user_one);
        oos.close();
        fos.close();

        //反序列化
        File file = new File("sun");
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        user user_two = (user)ois.readObject();
        ois.close();
        fis.close();
        System.out.println(user_two.getName());


    }


}
