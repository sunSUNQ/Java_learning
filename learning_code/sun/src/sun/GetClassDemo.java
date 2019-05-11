package sun;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GetClassDemo {
    public static void main( String[] args ) throws Exception, IOException{
        //得到Class的三种方式：
        //1、通过对象调用 getClass() 方法来获取；传过来一个对象，不知道具体是什么类
        Person p1 = new Person();
        Class c1 = p1.getClass();

        //2、通过类名 .class 的方式得到，该方法最可靠
        Class c2 = Person.class;

        //3、通过 Class 对象的 forName() 静态方法来获取，用的最多
        Class c3 = Class.forName("sun.Person");

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
