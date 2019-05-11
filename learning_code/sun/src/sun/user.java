package sun;

import java.io.IOException;
import java.io.Serializable;


public class user implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {

        // 使用原来的ReadObject方法
        in.defaultReadObject();

        //弹窗测试
        //Runtime.getRuntime().exec("calc");
    }

}



