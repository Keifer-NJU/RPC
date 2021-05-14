import netty.server.NettyRpcServer;
import remoting.transport.socket.SocketRpcServe;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Keifer
 * @createTime 2021/3/9 22:04
 */
public class SocketServerMain {
    public static void main(String[] args) {
//        SocketRpcServe socketRpcServe = new SocketRpcServe(new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
//        socketRpcServe.start();

        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.start();
    }
}
