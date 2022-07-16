package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ErrorResponse {

    public ErrorResponse(String error){
        this.error = error;
    }
    String error;
    Boolean success = false;
}
