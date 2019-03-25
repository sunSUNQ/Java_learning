# Java的反序列化

- 序列化与反序列化：

  ​	序列化（Serialization）是将对象的状态信息转化为可以存储或者传输的形式的过程，一般将一个对象存储到一个储存媒介，例如档案或记忆体缓冲等，在网络传输过程中，可以是字节或者XML等格式；而字节或者XML格式的可以还原成完全相等的对象，这个相反的过程又称为反序列化。

- 对象序列化机制：

  ​	对象序列化机制（object serialization）是java语言内建的一种对象持久化方式，通过对象序列化，可以将对象的状态信息保存为字节数组，并且可以在需要时，将这个字节数组通过反序列化的方式转换成对象，对象的序列化可以很容易的在JVM中的活动对象和字节数组（流）之间进行转换。在JAVA中，对象的序列化和反序列化被广泛的应用到RMI（远程方法调用）及网络传输中。

- Serialization接口详解：

  ​	Java类通过实现java.io.Serialization接口来启用序列化功能。下边是应用序列化与反序列化的代码样例。

  ```
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
  
  }
  ```

  ​	通过下边的操作来实现序列化与反序列化。

  ```
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
  ```

- ReadObject方法

  ​	着重讲一下ReadObject方法，如果ReadObject方法被序列化的类重写，虚拟机在反序列化的过程中，会调用被反序列化类的ReadObject方法。

  ​	也就是说如果在user类中重写了readObject方法，虚拟机在反序列化过程中会运用user类中的ReadObject方法。下边代码进行在user类中对ReadObject方法进行重写。运行时，会执行calc的弹窗。

```
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
        Runtime.getRuntime().exec("calc");
    }
}
```

​	这里就知道反序列化漏洞存在，可以通过重写被序列化类的ReadObject方法来实现。但是一般的程序员都不会直接将执行命令写在ReadObject中，此处就通过反射链来进行任意代码执行的。