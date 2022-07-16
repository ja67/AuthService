package service;

import com.sun.net.httpserver.HttpExchange;
import model.ErrorResponse;
import model.Response;
import model.SuccessResponse;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import util.HashUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static constant.Constant.*;

public class AuthRequestHandler extends AbstractRequestHandler{


    public Response post(HttpExchange httpExchange) throws NoSuchAlgorithmException {
        JSONObject request = new JSONObject(httpExchange.getResponseBody().toString());
        String userName = (String) request.get("userName");
        String password = (String) request.get("password");


        RMap<String, String> userMap = RedisClient.client.getMap(USER_MAP_KEY);
        if (!userMap.containsKey(userName)) {
            return new Response(
                    new JSONObject(new ErrorResponse("User doesn't exists")),
                    400
            );

        } else if (!HashUtil.toSHA256(password).equals(userMap.get(userName))) {
            return new Response(
                    new JSONObject(new ErrorResponse("Incorrect password")),
                    400
            );
        } else {
            String token = HashUtil.toSHA256(String.format("%s%s%s", userName, password, new Date().toString()));
            RMap<String, String> tokenMap = RedisClient.client.getMap(TOKEN_MAP);
            tokenMap.put(token, userName);
            tokenMap.expire(2L, TimeUnit.HOURS);
            return new Response(
                    new JSONObject(new SuccessResponse(token)),
                    200
            );
        }
    }

    public Response delete(HttpExchange httpExchange) throws NoSuchAlgorithmException {

        String token = null;

        String queryString = httpExchange.getRequestURI().getQuery();
        if(queryString==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        for (String query : queryString.split("&")){
            String[] split = query.split("=");
            if(split.length==2 && Objects.equals(split[0], "token")){
                token = split[1];
            }
        }
        if(token==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        RMap<String, String> tokenMap = RedisClient.client.getMap(TOKEN_MAP);
        tokenMap.remove(token);
        return new Response(
                new JSONObject(new SuccessResponse()),
                200
        );
    }
}
