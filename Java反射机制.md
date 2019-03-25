# Java的反射机制

- Java的反射机制

​	反射就是在运行状态中，对于任意的一个类，都能够知道这个类的所有的属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；并能改变它的属性。

- 得到Class的三种方式

```
//定义一个类 Person.java
package sun;

public class Person {

    // 私有属性
    private String name = "SunQing";
    // 公有属性
    public int age = 23;
    
    // 构造方法
    public Person(){}
    // 私有方法
    private void say(){
        System.out.println("private say()...");
    }
    // 公有方法
    public void work(){
        System.out.println("public work()...");
    }
}
```

```
//简单实现Class的三种调用方式，Demo.java
package sun;

import java.io.*;

public class Demo {

    public static void main(String[] args) throws Exception, IOException{
	    //得到Class的三种方式：
        //1、通过对象调用 getClass() 方法来获取；传过来一个对象，不知道具体是什么类
        Person p1 = new Person();
        Class c1 = p1.getClass();

        //2、通过类名 .class 的方式得到，该方法最可靠
        Class c2 = Person.class;

        //3、通过 Class 对象的 forName() 静态方法来获取，用的最多
        Class c3 = Class.forName("sun.Person");
        
        //一个JVM只会有一个Class实例，因此c1，c2，c3都是一个。
	}
}
```

- 通过Class类获取成员变量、成员方法、接口、超类、构造方法等

  查询API可以看到Class的方法：

  ​	getName()：获得类的完整名字。

  ​	getFields()：获得类的public类型的属性。

  ​	getDeclaredFields()：获得类的所有属性。包括private声明和继承类。

  ​	getMethods()：获得类的public类型方法。

  ​	getDeclaredMethods()：获得类的所有方法，包括private声明和继承类。

  ​	getMethod(String name, Class[] parameterTypes)：获得类的特定方法，name参数指定方法的名字，parameterTypes 参数指定方法的参数类型。
  　　getConstructors()：获得类的public类型的构造方法。
  　　getConstructor(Class[] parameterTypes)：获得类的特定构造方法，parameterTypes 参数指定构造方法的参数类型。
  　　newInstance()：通过类的不带参数的构造方法创建这个类的一个对象。

```
package sun;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SerializableDemo {

    public static void main(String[] args) throws Exception, IOException{

        //2、通过类名 .class 的方式得到，该方法最可靠
        Class c2 = Person.class;

        //获得类完整的名字
        String className = c2.getName();
        System.out.println("ClassName: " + className);//sunPerson

        //获得类的public类型的属性
        Field[] fields = c2.getFields();
        System.out.println("ClassFields: ");
        for (Field field : fields){
            System.out.println(field.getName());//age
        }

        //获得类的所有属性，包括私有的。
        Field[] allfields = c2.getDeclaredFields();
        System.out.println("ClassDeclaredFields: ");
        for (Field field : allfields){
            System.out.println(field.getName());//name, age
        }

        //获得类的public类型的方法。包括 Object 类的一些方法
        Method[] methods = c2.getMethods();
        System.out.println("ClassMethods: ");
        for (Method method : methods){
            System.out.println(method.getName());
            // work, wait, wait, wait, equals, toString,
            // hashCode, getClass ,notify, notifyAll
        }

        //获得类的所有方法
        Method[] allMethods = c2.getDeclaredMethods();
        System.out.println("ClassDeclaredMethods: ");
        for (Method method : allMethods){
            System.out.println(method.getName());//work, say
        }

        //获取指定的属性
        Field f1 = c2.getField("age");
        System.out.println("ClassFieldOne: " + f1);// public int sun.Person.age

        //获取指定的私有属性
        Field f2 = c2.getDeclaredField("name");
        //启用和禁用访问安全检查的开关，值为 true，则表示反射的对象在使用时应该取消java的访问检查。
        f2.setAccessible(true);
        System.out.println("ClassPrivateField" + f2);// java.lang.String sun.Person.name

        //创建这个类的一个对象
        Object p2 = c2.newInstance();
        //将 p2 对象的 f2 属性赋值为 Bob，f2 属性即为私有属性 name
        f2.set(p2, "Bob");
        //使用反射机制可以打破封装性，导致了java对象的属性不安全。
        System.out.println("newInstance: " + f2.get(p2));// Bob

        //获取构造方法
        Constructor[] constructors = c2.getConstructors();
        System.out.println("ClassConstructor: ");
        for (Constructor constructor : constructors){
            System.out.println(constructor.toString());//public sun.Person()
        }
    }
}
```

- 根据反射来获取父类属性Demo

```
//父类Parent类
package sun;

public class Parent {
    public String publicField = "parent_publicField";
    protected String protectField = "parent_protectField";
    String defaultField = "parent_defaultField";
    private String privateField = "parent_privateField";
}
```

```
//子类Son类
package sun;

public class Son extends Parent {
}
```

```
//Demo尝试访问父类的私有属性
package sun;

import java.lang.reflect.Field;

public class ReflectionDemo {

    public static Field getDeclaredField(Object obj, String fieldName){

        Field field = null;
        Class c = obj.getClass();
        for (; c != Object.class ; c = c.getSuperclass()){
            try{
                field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            }catch (Exception e){

            }
        }
        return null;
    }

    public static Object getFieldValue(Object object, String fieldName) throws Exception{

        Field field = getDeclaredField(object, fieldName);
        return field.get(object);

    }

    public static void main(String[] args) throws Exception{

        Class c1 = Class.forName("sun.Son");
        //获取父类的私有属性值
        System.out.println(getFieldValue(c1.newInstance(), "privateField"));
        /*parent_privateField
          获取了父类的私有属性值，获取子类的对象是不能得到父类的属性值的，
          必须根据反射获得的子类Class对象，
          调用 getSuperclass() 方法获取父类对象，
          然后通过父类的对象去获取父类的私有属性。
        */
    }
}
```

