package remoting.dto;

import enums.RpcResponseCodeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Keifer
 * @createTime 2021/3/9 20:05
 */
@NoArgsConstructor
@Setter
@Getter
public class RpcResponse<T> implements Serializable {
    private final static Long serialVersionUID = 3L;
    private String requestId;
    private Integer code;
    private String message;
    private T data;
    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        response.setData(data);
        return response;
    }
    public static <T> RpcResponse<T> fail(){
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        return response;
    }
}
