package remoting.transport;

import remoting.dto.RpcRequest;

/**
 * @author Keifer
 * @createTime 2021/3/9 20:33
 */
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     * @param rpcRequest  message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
