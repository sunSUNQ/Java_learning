# Java对象的序列化（Serialization）和反序列化详解

2018年02月13日 15:56:02 [随风yy](https://me.csdn.net/yaomingyang) 阅读数：4463

 版权声明：本文为博主原创文章，未经博主允许不得转载。	https://blog.csdn.net/yaomingyang/article/details/79321939

#### 1.序列化和反序列化

序列化（Serialization）是将对象的状态信息转化为可以存储或者传输的形式的过程，一般将一个对象存储到一个储存媒介，例如档案或记忆体缓冲等，在网络传输过程中，可以是字节或者XML等格式；而字节或者XML格式的可以还原成完全相等的对象，这个相反的过程又称为反序列化；

#### 2.Java对象的序列化和反序列化

在Java中，我们可以通过多种方式来创建对象，并且只要对象没有被回收我们都可以复用此对象。但是，我们创建出来的这些对象都存在于JVM中的堆（stack）内存中，只有JVM处于运行状态的时候，这些对象才可能存在。一旦JVM停止，这些对象也就随之消失；

但是在真实的应用场景中，我们需要将这些对象持久化下来，并且在需要的时候将对象重新读取出来，Java的序列化可以帮助我们实现该功能。

对象序列化机制（object serialization）是[java语言](https://www.baidu.com/s?wd=java%E8%AF%AD%E8%A8%80&tn=24004469_oem_dg&rsv_dl=gh_pl_sl_csd)内建的一种对象持久化方式，通过对象序列化，可以将对象的状态信息保存未字节数组，并且可以在有需要的时候将这个字节数组通过反序列化的方式转换成对象，对象的序列化可以很容易的在JVM中的活动对象和字节数组（流）之间进行转换。

在JAVA中，对象的序列化和反序列化被广泛的应用到RMI（远程方法调用）及网络传输中；

#### 3.序列化及反序列化相关接口及类

Java为了方便开发人员将java对象序列化及反序列化提供了一套方便的API来支持，其中包括以下接口和类：

```
java.io.Serializable1
java.io.Externalizable1
ObjectOutput1
ObjectInput1
ObjectOutputStream1
ObjectInputStream1
```

#### 4.Serialization接口详解

Java类通过实现java.io.Serialization接口来启用序列化功能，未实现此接口的类将无法将其任何状态或者信息进行序列化或者反序列化。可序列化类的所有子类型都是可以序列化的。序列化接口没有方法或者字段，仅用于标识可序列化的语义。

当试图对一个对象进行序列化时，如果遇到一个没有实现java.io.Serialization接口的对象时，将抛出NotSerializationException异常。

如果要序列化的类有父类，要想将在父类中定义过的变量序列化下来，那么父类也应该实现java.io.Serialization接口。

下面是一个实现了java.io.Serialization接口的类：

```
package common.lang;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class User1 implements Serializable{

    private String name;
    private int age;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                                   .append("name", name)
                                   .append("age", age)
                                   .toString();
    }
}
```

通过下面的代码进行序列化及反序列化：

```
package common.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableDemo1 {

    public static void main(String[] args) throws Exception, IOException {
        //初始化对象
        User1 user = new User1();
        user.setName("yaomy");
        user.setAge(23);
        System.out.println(user);
        //序列化对象到文件中
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("template"));
        oos.writeObject(user);
        oos.close();
        //反序列化
        File file = new File("template");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        User1 newUser = (User1)ois.readObject();
        System.out.println(newUser.toString());
    }
}
```

#### 5.Java还提供了另一个序列化接口java.io.Externalizable

为了了解Externalizable接口和Serializable接口的区别先来看代码，我们将上面的User1类改为实现java.io.Externalization接口；

```
package common.lang;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class User1 implements Externalizable{

    private String name;
    private int age;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                                   .append("name", name)
                                   .append("age", age)
                                   .toString();
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }
}
package common.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableDemo1 {

    public static void main(String[] args) throws Exception, IOException {
        //初始化对象
        User1 user = new User1();
        user.setName("yaomy");
        user.setAge(23);
        System.out.println(user);
        //序列化对象到文件中
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("template"));
        oos.writeObject(user);
        oos.close();
        //反序列化
        File file = new File("template");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        User1 newUser = (User1)ois.readObject();
        System.out.println(newUser.toString());
        ois.close();
    }
}
```

输出结构是：

```
common.lang.User1@6ef64f64[
  name=yaomy
  age=23
]
common.lang.User1@184c9860[
  name=<null>
  age=0
]
```

通过上面的实例可以发现，对User1进行序列化然后再进行反序列化之后对象的属性都恢复成了默认值，也就是说之前的哪个对象的状态并没有被持久化下来，这就是Externalization和Serialization接口之间的区别；

Externalizable继承了Serializable，该接口中定义了两个抽象方法：writeExternal()与readExternal()。当使用Externalizable接口来进行序列化与反序列化的时候需要开发人员重写writeExternal()与readExternal()方法。由于上面的代码中，并没有在这两个方法中定义序列化实现细节，所以输出的内容为空。还有一点值得注意：在使用Externalizable进行序列化的时候，在读取对象时，会调用被序列化类的无参构造器去创建一个新的对象，然后再将被保存对象的字段的值分别填充到新对象中。所以，实现Externalizable接口的类必须要提供一个public的无参的构造器。

按照要求修改之后的代码是：

```
package common.lang;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class User1 implements Externalizable{

    private String name;
    private int age;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                                   .append("name", name)
                                   .append("age", age)
                                   .toString();
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeInt(age);

    }
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String)in.readObject();
        age = in.readInt();

    }
}
package common.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableDemo1 {

    public static void main(String[] args) throws Exception, IOException {
        //初始化对象
        User1 user = new User1();
        user.setName("yaomy");
        user.setAge(23);
        System.out.println(user);
        //序列化对象到文件中
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("template"));
        oos.writeObject(user);
        oos.close();
        //反序列化
        File file = new File("template");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        User1 newUser = (User1)ois.readObject();
        System.out.println(newUser.toString());
        ois.close();
    }
}
```

输出结果是：

```
common.lang.User1@6cd66725[
  name=yaomy
  age=23
]
common.lang.User1@19160e64[
  name=yaomy
  age=23
]
```

这样就可以将之前的对象状态保存下来了，如果User类中没有无参数的构造函数，在运行时会抛出异常：java.io.InvalidClassException；

#### 6.静态变量的序列化

静态变量序列化代码：

```
public class Test implements Serializable {

    private static final long serialVersionUID = 1L;

    public static int staticVar = 5;

    public static void main(String[] args) {
        try {
            //初始时staticVar为5
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("result.obj"));
            out.writeObject(new Test());
            out.close();

            //序列化后修改为10
            Test.staticVar = 10;

            ObjectInputStream oin = new ObjectInputStream(new FileInputStream("result.obj"));
            Test t = (Test) oin.readObject();
            oin.close();

            //再读取，通过t.staticVar打印新的值
            System.out.println(t.staticVar);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
```

main 方法中，将对象序列化后，修改静态变量的数值，再将序列化对象读取出来，然后通过读取出来的对象获得静态变量的数值并打印出来，打印出来的是10还是5？

最后的输出是 10，对于无法理解的读者认为，打印的 staticVar 是从读取的对象里获得的，应该是保存时的状态才对。之所以打印 10 的原因在于序列化时，并不保存静态变量，这其实比较容易理解，序列化保存的是对象的状态，静态变量属于类的状态，因此 序列化并不保存静态变量。

#### 7.Transient 关键字使用

Transient 关键字的作用是控制变量的序列化，在变量声明前加上该关键字，可以阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值，如 int 型的是 0，对象型的是 null。

参考：[Java序列化高级进阶](https://www.ibm.com/developerworks/cn/java/j-lo-serial/) 
参考：[深入分析Java的序列化和反序列化](http://www.importnew.com/18024.html)