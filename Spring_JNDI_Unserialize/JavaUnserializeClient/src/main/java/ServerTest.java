
import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.sun.net.httpserver.HttpServer;

import javax.naming.Reference;
import java.net.InetSocketAddress;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class ServerTest {

    public static void main(String args[]) throws Exception {

        System.out.println("Starting HTTP server");
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.createContext("/",new HttpFileHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        Registry registry = LocateRegistry.createRegistry(1099);

        Reference object = new Reference("ExportObject", "ExportObject", "http://127.0.0.1:8000/");

        ReferenceWrapper refObjWrapper = new ReferenceWrapper(object);

        System.out.println("Binding 'refObjWrapper' to 'rmi://127.0.0.1:1099/object'");

        registry.bind("object", refObjWrapper);

    }

}