package remoting.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import remoting.dto.RpcRequest;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Keifer
 * @createTime 2021/3/9 21:44
 */
@Slf4j
@AllArgsConstructor
public class RpcRequestHandler {
    public Object handle(RpcRequest rpcRequest) {
        return invokeTargetMethod(rpcRequest, new HelloServiceImpl());
    }
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }
}
