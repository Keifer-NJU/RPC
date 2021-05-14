package remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author Keifer
 * @createTime 2021/3/9 21:25
 */
@Slf4j
public class SocketRpcServe {
    private final ExecutorService threadPool;

    public SocketRpcServe(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket()){
            serverSocket.bind(new InetSocketAddress("localhost",8080));
            Socket socket;
            while((socket = serverSocket.accept())!=null){
                log.info("client connected[{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandler(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
