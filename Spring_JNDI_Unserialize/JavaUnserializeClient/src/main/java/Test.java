import java.rmi.registry.LocateRegistry;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Test {

    public static void initPerson() throws Exception{

        //配置JNDI工厂和JNDI的url和端口。如果没有配置这些信息，会出现NoInitialContextException异常
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

        initPerson();
        findPerson();
    }
}
