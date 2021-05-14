package netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.transport.RpcRequestTransport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * @author Keifer
 * @createTime 2021/5/12 16:19
 */
public class NettyRpcClient implements RpcRequestTransport {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    public static final String host = "localhost";
    public static final int port = 8080;
    public  static final Bootstrap bootstrap;

    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //JDK 自带的序列化方式
                        socketChannel.pipeline().addLast(new ObjectEncoder());
                        socketChannel.pipeline().addLast(new ObjectDecoder(new ClassResolver() {
                            @Override
                            public Class<?> resolve(String className) throws ClassNotFoundException {
                                return Class.forName(className);
                            }
                        }));
                        // 加入自己的处理器
                        socketChannel.pipeline().addLast(new MyNettyClientHandler());
                    }
                });

    }

    // sendRpcRequest() 为用户调用API
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 获取返回结果的CompletableFuture,之后会放在一个map中
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();
        try{
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();
            // 实际是一个Map, key为请求ID, 所以请求ID可不能重复, value是CompletableFuture对象
            UnProcessedRequests.put(rpcRequest.getRequestId(), completableFuture);
            if(channel!=null){
                // 写入远程传输数据，并添加了一个监听器（起初一直想在这个监听器里面获取结果，发现future中其实没数据，因为没地方设置）
                channel.writeAndFlush(rpcRequest).addListener(future->{
                    if(future.isSuccess()){
                        logger.info("client send message :[{}]", rpcRequest.toString());

                    }else{
                        logger.error("Send message failed:",future.cause());
                    }
                });
                channel.closeFuture();
                // 返回CompletableFuture 对象，用于后面获取数据（即completableFuture.get(), 但他会阻塞，直到completableFuture完成数据注入,在MyNettyClientHandler中完成）
                // 用 CompletableFuture 获取结果比用Channel的AttributeMap获取结果好处，是这里不用等待调用完成（即需要channel.closeFuture().sync();），具体参考https://www.yuque.com/books/share/b7a2512c-6f7a-4afe-9d7e-5936b4c4cab0/rvlui6
                return completableFuture;
            }

        } catch (InterruptedException  e) {
            e.printStackTrace();
        }
        return completableFuture;
    }
}
