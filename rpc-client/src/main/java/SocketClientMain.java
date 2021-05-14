import com.example.serviceapi.HelloService;
import com.example.serviceapi.Person;
import netty.client.NettyRpcClient;
import proxy.RpcClientProxy;
import remoting.transport.RpcRequestTransport;
import remoting.transport.socket.SocketRpcClient;

/**
 * @author Keifer
 * @createTime 2021/3/9 21:57
 */
public class SocketClientMain {
    public static void main(String[] args){
//        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcRequestTransport rpcRequestTransport = new NettyRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Person("AE86", 18,"handsome"));
        System.out.println(hello);
    }
}
