package remoting.transport.socket;

import remoting.dto.RpcRequest;
import remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Keifer
 * @createTime 2021/3/9 20:50
 */
public class SocketRpcClient implements RpcRequestTransport {
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //TODO 服务发现
        try (Socket socket = new Socket("localhost", 8080)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //TODO 异常封装
        return null;
    }
}
