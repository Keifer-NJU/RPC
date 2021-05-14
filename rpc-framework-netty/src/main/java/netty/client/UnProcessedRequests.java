package netty.client;

import remoting.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Keifer
 * @createTime 2021/5/14 10:14
 */
public class UnProcessedRequests {
    private  static final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> UNPROCEEDED_RESPONSE_FUTURE_MAP = new ConcurrentHashMap();

    public static void put(String key, CompletableFuture<RpcResponse> completableFuture){
        UNPROCEEDED_RESPONSE_FUTURE_MAP.put(key, completableFuture);
    }

    public static void complete(RpcResponse response){
        CompletableFuture<RpcResponse> completableFuture = UNPROCEEDED_RESPONSE_FUTURE_MAP.remove(response.getRequestId());
        if(null!=completableFuture){
            completableFuture.complete(response);
        }
        else{
            throw  new IllegalStateException();
        }
    }
}
