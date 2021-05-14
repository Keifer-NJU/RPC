package proxy;

import lombok.extern.slf4j.Slf4j;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.transport.RpcRequestTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Keifer
 * @createTime 2021/3/9 21:07
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcRequestTransport rpcRequestTransport;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
    }

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz}, this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoke method:[{}]", method.getName());
        RpcRequest request = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .version("v1")
                .group("test")
                .build();
        RpcResponse<Object> rpcResponse = null;
        CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) rpcRequestTransport.sendRpcRequest(request);
        rpcResponse = completableFuture.get();
        return rpcResponse.getData();
    }
}
