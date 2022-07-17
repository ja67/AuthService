package service;

import com.sun.net.httpserver.HttpExchange;
import model.ErrorResponse;
import model.Response;
import model.SuccessResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import util.RequestBodyUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static constant.Constant.*;

public class RoleRequestHandler extends AbstractRequestHandler {


    private static final Pattern ALLOWED_ROLE_NAME_CHAR = Pattern.compile("^[a-zA-Z0-9_]*$");


    public Response get(HttpExchange httpExchange){
        String token = null;
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
                    case "roleName":
                        roleName = split[1];
                        break;
                    case "token":
                        token = split[1];
                        break;

                }
            }
        String userName = getUserName(token);
        if(Objects.equals(userName, "")){
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
    private String getUserName(String token){
            RMap<String, String> tokenMap = RedisClient.client.getMap(TOKEN_MAP);
            return tokenMap.getOrDefault(token,"");
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
        Map<String, Object> requestMap = new JSONObject(RequestBodyUtil.readBody(httpExchange)).toMap();
        if(!requestMap.containsKey("roleName")){
            return new Response(
                    new JSONObject(new ErrorResponse("Invalid request")),
                    400
            );
        }
        String roleName = (String) requestMap.get("roleName");

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
