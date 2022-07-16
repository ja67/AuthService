package model;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;


@Data
@AllArgsConstructor
public class Response {
    JSONObject responseBody;
    Integer statusCode;
}
