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
