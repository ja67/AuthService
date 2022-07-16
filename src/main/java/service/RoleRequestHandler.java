package service;

import com.sun.net.httpserver.HttpExchange;
import model.ErrorResponse;
import model.Response;
import model.SuccessResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSet;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static constant.Constant.*;

public class RoleRequestHandler extends AbstractRequestHandler {


    private static Pattern ALLOWED_ROLE_NAME_CHAR = Pattern.compile("^[a-zA-Z0-9_]*$");


    public Response get(HttpExchange httpExchange){
        String token = null;
        String userName = null;
        String roleName = null;

        String queryString = httpExchange.getRequestURI().getQuery();
        if(queryString==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Unauthorized")),
                    403
            );
        }
        for (String query : queryString.split("&")){
            String[] split = query.split("=");
            if(split.length==2)
                switch (split[0]){
                    case "userName":
                        userName = split[1];
                        break;
                    case "roleName":
                        roleName = split[1];
                        break;
                    case "token":
                        token = split[1];
                        break;

                }
            }
        if(!isTokenValid(token, userName)){
            return new Response(
                    new JSONObject(new ErrorResponse("Unauthorized")),
                    403
            );
        } else if (roleName==null) {
            return queryAllRoles(userName);
        }else {
            return checkRole(userName, roleName);
        }
    }
    private boolean isTokenValid(String token, String userName){
        if(token==null){
            return false;
        }
        else{
            RMap<String, String> tokenMap = RedisClient.client.getMap(TOKEN_MAP);
            return tokenMap.containsKey(token) && Objects.equals(tokenMap.get(token), userName);
        }
    }
    private Response queryAllRoles(String userName){

        RSet<String> authSet = RedisClient.client.getSet(AUTH_SET);
        Set<String> roleSet = authSet.readAll().stream().filter(record -> record.contains(String.format(":%s", userName))).map(record -> record.split(":")[0]).collect(Collectors.toSet());
        return new Response(
                new JSONObject(new SuccessResponse(new JSONArray(roleSet).toString())),
                200
        );
    }
    private Response checkRole(String userName, String roleName){
        RSet<String> authSet = RedisClient.client.getSet(AUTH_SET);
        boolean contains = authSet.contains(String.format("%s:%s", roleName, userName));
        return new Response(
                new JSONObject(new SuccessResponse(Boolean.toString(contains))),
                200
        );

    }
    public Response post(HttpExchange httpExchange) {
        JSONObject request = new JSONObject(httpExchange.getResponseBody().toString());
        String roleName = (String) request.get("roleName");

        RSet<String> roleSet = RedisClient.client.getSet(ROLE_SET_KEY);
        if (roleSet.contains(roleName)) {
            return new Response(
                    new JSONObject(new ErrorResponse("Role already exists")),
                    400
            );
        } else if (!ALLOWED_ROLE_NAME_CHAR.matcher(roleName).matches()) {
            return new Response(
                    new JSONObject(new ErrorResponse("Role name should contains alphanumeric characters and underscores only")),
                    400
            );
        } else {
            roleSet.add(roleName);
            return new Response(
                    new JSONObject(new SuccessResponse()),
                    200
            );
        }
    }

    public Response delete(HttpExchange httpExchange) {
        String roleName = null;

        String queryString = httpExchange.getRequestURI().getQuery();
        if(queryString==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        for (String query : queryString.split("&")){
            String[] split = query.split("=");
            if(split.length==2 && Objects.equals(split[0], "roleName")){
                roleName = split[1];
            }
        }
        if(roleName==null){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid input")),
                    400
            );
        }
        else{
            RSet<String> roleSet = RedisClient.client.getSet(ROLE_SET_KEY);
            if(!roleSet.contains(roleName)){
                return new Response(
                        new JSONObject(new ErrorResponse("Role doesn't exists")),
                        400
                );
            }
            else {
                roleSet.remove(roleName);
                return new Response(
                        new JSONObject(new SuccessResponse()),
                        200
                );
            }
        }
    }
}
