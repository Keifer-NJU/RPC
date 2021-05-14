package remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author Keifer
 * @createTime 2021/3/9 20:00
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] parameters;
    private String version;
    private String group;
}
