package remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Keifer
 * @createTime 2021/3/9 21:38
 */
@Slf4j
public class SocketRpcRequestHandler implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    public SocketRpcRequestHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            RpcRequest request = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(request);
            objectOutputStream.writeObject(RpcResponse.success(result, request.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
