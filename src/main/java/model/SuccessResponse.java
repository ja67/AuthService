package model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SuccessResponse {
    public SuccessResponse(String message){
        this.message = message;
    }
    Boolean success = true;
    String message;
}
