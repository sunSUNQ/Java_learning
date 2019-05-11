# Java的反序列化



## 序列化与反序列化：（Serialization）

- Java 序列化是指把 Java 对象转换为字节序列的过程便于保存在内存、文件、数据库中，ObjectOutputStream类的 writeObject() 方法可以实现序列化。

- Java 反序列化是指把字节序列恢复为 Java 对象的过程，ObjectInputStream 类的 readObject() 方法用于反序列化。

- 序列化与反序列化是让 Java 对象脱离 Java 运行环境的一种手段，可以有效的实现多平台之间的通信、对象持久化存储。

## 主要应用在以下场景：

- HTTP：多平台之间的通信，管理等

- RMI：是 Java 的一组拥护开发分布式应用程序的 API，实现了不同操作系统之间程序的方法调用。值得注意的是，RMI 的传输 100% 基于反序列化，Java RMI 的默认端口是 1099 端口。

- JMX：JMX 是一套标准的代理和服务，用户可以在任何 Java 应用程序中使用这些代理和服务实现管理,中间件软件 WebLogic 的管理页面就是基于 JMX 开发的，而 JBoss 则整个系统都基于 JMX 构架。 

## 漏洞成因

- 暴露或间接暴露反序列化 API ，导致用户可以操作传入数据，攻击者可以精心构造反序列化对象并执行恶意代码。

## 实现序列化与反序列化

​	Java类通过实现java.io.Serialization接口来启用序列化功能。下边是应用序列化与反序列化的代码样例。

```
package sun;

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

## ReadObject方法

​	着重讲一下ReadObject方法，如果ReadObject方法被序列化的类重写，虚拟机在反序列化的过程中，会调用被反序列化类的ReadObject方法。也就是说如果在user类中重写了readObject方法，虚拟机在反序列化过程中会运用user类中的ReadObject方法。下边代码进行在user类中对ReadObject方法进行重写。运行时，会执行calc的弹窗。

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

​	我们注意到 user 类实现了`Serializable`接口，并且重写了`readObject()`函数。这里需要注意：**只有实现了Serializable接口的类的对象才可以被序列化**。这里的 readObject() 执行了`Runtime.getRuntime().exec("calc")`，而 readObject() 方法的作用正是从一个源输入流中读取字节序列，再把它们反序列化为一个对象，并将其返回，readObject() 是可以重写的，可以定制反序列化的一些行为。

​	这里就知道反序列化漏洞存在，可以通过重写被序列化类的ReadObject方法来实现。但是一般的程序员都不会直接将执行命令写在ReadObject中。