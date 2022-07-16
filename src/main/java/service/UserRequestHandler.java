package service;

import com.sun.net.httpserver.HttpExchange;
import model.ErrorResponse;
import model.Response;
import model.SuccessResponse;
import org.json.JSONObject;
import org.redisson.api.RMap;
import util.HashUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Pattern;

import static constant.Constant.*;

public class UserRequestHandler extends AbstractRequestHandler {


    private static Pattern ALLOWED_USER_NAME_CHAR = Pattern.compile("^[a-zA-Z0-9_]*$");


    public Response post(HttpExchange httpExchange) throws NoSuchAlgorithmException {
        JSONObject request = new JSONObject(httpExchange.getResponseBody().toString());
        String userName = (String) request.get("userName");

        RMap<String, String> userMap = RedisClient.client.getMap(USER_MAP_KEY);
        if (userMap.containsKey(userName)) {
            return new Response(
                    new JSONObject(new ErrorResponse("User already exists")),
                    400
            );
        } else if (!ALLOWED_USER_NAME_CHAR.matcher(userName).matches()) {
            return new Response(
                    new JSONObject(new ErrorResponse("Username should contains alphanumeric characters and underscores only")),
                    400
            );

        } else {
            String password = (String) request.get("userName");

            userMap.put(userName, HashUtil.toSHA256(password));
            return new Response(
                    new JSONObject(new SuccessResponse()),
                    200
            );
        }
    }

    public Response delete(HttpExchange httpExchange) {
        String userName = null;

        String queryString = httpExchange.getRequestURI().getQuery();
        if(queryString==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        for (String query : queryString.split("&")){
                String[] split = query.split("=");
                if(split.length==2 && Objects.equals(split[0], "userName")){
                    userName = split[1];
                }

        }
        if(userName==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        else{
            RMap<String, String> userMap = RedisClient.client.getMap(USER_MAP_KEY);
            if(!userMap.containsKey(userName)){
                return new Response(
                        new JSONObject(new ErrorResponse("User doesn't exists")),
                        400
                );
            }
            else {
                userMap.remove(userName);
                return new Response(
                        new JSONObject(new SuccessResponse()),
                        200
                );
            }
        }
    }
}
