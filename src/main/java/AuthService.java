import com.sun.net.httpserver.HttpServer;

import service.Router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class AuthService {

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        Logger logger = Logger.getGlobal();
        httpServer.createContext("/", new Router());
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();

        logger.info("AuthService has started"); // Display the string.
    }
}
