package sun;

public class Person {

    // 私有属性
    private String name = "SunQing";

    // 公有属性
    public int age = 23;

    // 构造方法
    public Person(){

    }

    // 私有方法
    private void say(){
        System.out.println("private say()...");
    }

    // 公有方法
    public void work(){
        System.out.println("public work()...");
    }

}
