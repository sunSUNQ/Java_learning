## 敏感信息序列化 CWE499_Sensitive_Data_Serializable

###### 漏洞描述

- ​	CWE499是由于java的反序列化机制导致的漏洞，由于对于继承使用不当，可能将敏感数据进行序列化，进而导致敏感信息泄露。

###### 漏洞代码

- ​	下边是漏洞代码，选用Juliet_Test_Suite_v1.3_for_Java 测试集，文件目录在Juliet_Test_Suite_v1.3_for_Java\Java\src\testcases\CWE499_Sensitive_Data_Serializable\CWE499_Sensitive_Data_Serializable__serializable_01_bad.java

```
package testcases.CWE499_Sensitive_Data_Serializable;

import java.sql.*;
import java.io.*;
import java.util.logging.Level;
import testcasesupport.*;


/* FLAW: Class extends a serializable class, does not explicitly deny serialization, and contains sensitive data. */ 
public class CWE499_Sensitive_Data_Serializable__serializable_01_bad extends CWE499_Sensitive_Data_Serializable__serializable_Helper
{    
    /* Sensitive field */
    private String passwordHash;
    
    protected void setPassword(String password)
    {
        passwordHash = password;
    }
    
    protected String getPassword()
    {
        return passwordHash;
    }
}
```

###### 漏洞成因

- ​	大概的意思是，第十行定义的**01_bad**这个类，是继承的父类 **Helper** ，而 **Helper** 是一个可以序列化的类，实现java.io.Serializable接口。那么在序列化这个**01_bad**类的时候就会**将隐私数据也序列化**，就是其中第十三行的 passwordHash 这个变量，如果攻击者可以获取到序列化的数据就有可能进行还原，进而获取到敏感信息。
- 可能是对于数据库中数据的定义类，例如：医患用户的id，家庭地址，电话等等信息。

###### 漏洞修复

- ​	如何进行漏洞的修复呢，大概有两种思路。
- 一是将父类的序列化操作进行重写，如果发现序列化的操作就抛异常，停止执行；
- 二是将敏感变量加上transient关键字，在变量声明前加上该关键字，可以阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值，如 int 型的是 0，对象型的是 null。

###### 修复代码1

- 将敏感变量加上transient关键字

```
package testcases.CWE499_Sensitive_Data_Serializable;

import java.sql.*;
import java.io.*;
import java.util.logging.Level;
import testcasesupport.*;

/* We would never expect to see a real class like this, but our implementation tries to ensure the fact
 * that one of the fields is sensitive */
public class CWE499_Sensitive_Data_Serializable__serializable_01_good2 extends CWE499_Sensitive_Data_Serializable__serializable_Helper
{
    /* FIX: Use the transient keyword to deny serialization */
    /* Sensitive field */
    private transient String passwordHash;
    
    protected void setPassword(String password)
    {
        passwordHash = password;
    }
    
    protected String getPassword()
    {
        return passwordHash;
    }
}
```

###### 修复代码2

- 将父类的序列化相关操作重写，如果子类执行序列化操作就抛异常。

```
package testcases.CWE499_Sensitive_Data_Serializable;

import java.sql.*;
import java.io.*;
import java.util.logging.Level;
import testcasesupport.*;


public class CWE499_Sensitive_Data_Serializable__serializable_01_good1 extends CWE499_Sensitive_Data_Serializable__serializable_Helper 
{
    /* Sensitive field */
    private String passwordHash;
    
    protected void setPassword(String password)
    {
        passwordHash = password;
    }
    
    protected String getPassword()
    {
        return passwordHash;
    }

    /* FIX: Override writeObject(), readObject(),  and readObjectNoData() methods to deny serialization attempts */
    private final void writeObject(ObjectOutputStream out) throws IOException
    {
        throw new NotSerializableException();
    }
    
    private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        throw new NotSerializableException();
    }
	
	private final void readObjectNoData() throws ObjectStreamException
	{
        throw new NotSerializableException();
    }
}
```

