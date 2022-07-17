package service;

import com.sun.net.httpserver.HttpExchange;
import model.ErrorResponse;
import model.Response;
import model.SuccessResponse;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import util.HashUtil;
import util.RequestBodyUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Pattern;

import static constant.Constant.*;

public class GrantRequestHandler extends AbstractRequestHandler {




    public Response post(HttpExchange httpExchange) {
        Map<String, Object> requestMap = new JSONObject(RequestBodyUtil.readBody(httpExchange)).toMap();
        if(!requestMap.containsKey("userName")||!requestMap.containsKey("roleName")){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid request")),
                    400
            );

        }
        String userName = (String) requestMap.get("userName");
        String roleName = (String) requestMap.get("roleName");
        

        RMap<String, String> userMap = RedisClient.client.getMap(USER_MAP_KEY);
        RSet<String> roleSet = RedisClient.client.getSet(ROLE_SET_KEY);
        if (!userMap.containsKey(userName)) {
            return new Response(
                    new JSONObject(new ErrorResponse("User doesn't exists")),
                    400
            );

        } else if (!roleSet.contains(roleName)) {
            return new Response(
                    new JSONObject(new ErrorResponse("Role doesn't exists")),
                    400
            );
        } else {
            RSet<String> authSet = RedisClient.client.getSet(AUTH_SET);
            authSet.add(String.format("%s:%s", roleName, userName));
            return new Response(
                    new JSONObject(new SuccessResponse()),
                    200
            );
        }
    }
}
