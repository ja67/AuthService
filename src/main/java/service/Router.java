package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router implements HttpHandler {
    private static Map<String, Class<? extends AbstractRequestHandler>> routingTable = new HashMap<>();
    static {
        routingTable.put("/auth(\\?.*)?", AuthRequestHandler.class);
        routingTable.put("/grant(\\?.*)?", GrantRequestHandler.class);
        routingTable.put("/role(\\?.*)?", RoleRequestHandler.class);
        routingTable.put("/user(\\?.*)?", UserRequestHandler.class);
    }
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        Logger logger = Logger.getGlobal();

        String requestMethod = httpExchange.getRequestMethod();

        Class<? extends AbstractRequestHandler> handlerClass = null;

        for(String key: routingTable.keySet()){
            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(path);
            if(matcher.matches()){
                handlerClass = routingTable.get(key);
            }
        }
        if(handlerClass == null){
            return;
        }

        try {
            Method method = handlerClass.getMethod(requestMethod.toLowerCase(Locale.ROOT), HttpExchange.class);
            Response response = (Response) method.invoke(handlerClass.newInstance(), httpExchange);
            handleResponse(httpExchange, response.getResponseBody().toString(), response.getStatusCode());

        } catch (NoSuchMethodException e) {
            methodNotSupportedResponse(httpExchange);
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            logger.severe(stringWriter.toString());
            internalServerErrorResponse(httpExchange);
        }
    }
    void handleResponse(HttpExchange httpExchange, String responseBody, Integer responseCode) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        httpExchange.sendResponseHeaders(responseCode, responseBody.length());
        outputStream.write(responseBody.getBytes());
        outputStream.flush();
        outputStream.close();
    }


    private void methodNotSupportedResponse(HttpExchange httpExchange) throws IOException {
        handleResponse(httpExchange,
                new JSONObject(new Error("Method not supported")).toString(),
                405);
    }
    private void internalServerErrorResponse(HttpExchange httpExchange) throws IOException {
        handleResponse(httpExchange,
                new JSONObject(new Error("Internal Server Error")).toString(),
                500);
    }
}
