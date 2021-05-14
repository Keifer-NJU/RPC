package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Keifer
 * @createTime 2021/3/9 20:16
 */
@AllArgsConstructor
@Getter
public enum  RpcResponseCodeEnum {
    /**
     * SUCCESS, 调用成功的返回的code和message
     */
    SUCCESS(200, "remote call success"),
    /**
     * FAIL, 调用成功的返回的code和message
     */
    FAIL(400, "remote call fail");
    private final int code;
    private final String message;
}
