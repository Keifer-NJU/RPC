## 版本1

### 原理架构图

![img](D:\OneDriver_edu\OneDrive - smail.nju.edu.cn\Java\MarkDown_AE86\RPC\assert\1594822112983-86e253d1-4447-4510-a8e4-b5edbe2f8085.jpeg)

1. 服务消费端（client）以本地调用方法的形式调用远程服务。                                                                                   **→rpc-client**
2. 客服端 Stub 接受到调用后，负责将方法、参数等组成网络传输的消息体-RpcRequest（序列化）：                  **→RpcClientProxy**
3. 客服端stub，找到远程服务的地址（这一版直接写死），并将消息发送到服务提供端,并等待服务返回的结果    **→SocketRpcClient**
4. 服务端stub，收到消息后交给线程池处理：将其反序列化成java对象                                                                       **→SocketRpcServe**
5. 服务端stub，根据RpcRequest中的参数调用本地对应的方法（这一版同样写死了，为了先走通基本流程）       **→SocketRpcRequestHandler**
6. 服务端stub，将返回结果封装成RpcResponse，传给客服端                                                                                     **→SocketRpcRequestHandler**
7. 客服端stub，也就是第三步的等待服务返回结果
8. over!

### 技术支撑

- 序列化：Java自带序列化
- 网络传输：Socket
- Java动态代理
- 线程池

### 下一版优化

- 使用netty作为传输通信
- CompletableFuture 获取返回结果

### 参考
- [手写一个RPC框架](https://juejin.cn/post/6844903921027137544#heading-9)(思路参考)
- https://github.com/Snailclimb/guide-rpc-framework  (代码结构参考)

## 版本2

总结：增加netty作为通信； CompletableFuture获取返回结果

![image-20210514112230882](D:\OneDriver_edu\OneDrive - smail.nju.edu.cn\Java\MarkDown_AE86\RPC\assert\image-20210514112230882.png)

在rpc-framework-common的基础上，增加rpc-framework-netty模块，实现的的就是上面两种方式。然后rpc-client和rpc-rever（**这两个模块其实就是用户使用时的案例，即使用示例，不属于框架的一部分**）中使用方式，基本没大变，希望实现开闭原则，基于接口编程



### 源码讲解

![image-20210514113243891](D:\OneDriver_edu\OneDrive - smail.nju.edu.cn\Java\MarkDown_AE86\RPC\assert\image-20210514113226129.png)

- NettyRpcClient 实现RpcRequestTransport接口（面向接口编程）
  - 因为现在还没有服务发现功能，所以暂时写死地址。
  - 关于Netty的具体配置，也是框架写死，即static模块代码部分，本来想着是否可以用户自定义扩展配置，但是还没想到好的方式，以及我看Dubbo中好像也是默认配置好（后面有空再仔细看看）
  - 其它看解释注释在代码中![img](file:///C:\Users\lenovo\AppData\Local\Temp\SGPicFaceTpBq\20916\38B9023C.png)

```java
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
```

- MyNettyClientHandler
  - 读取返回结果，将其放入CompletableFuture中

```java
public class MyNettyClientHandler extends ChannelInboundHandlerAdapter{

    private static final Logger LOGGER = LoggerFactory.getLogger(MyNettyClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            RpcResponse rpcResponse = (RpcResponse) msg;
            // 找出这个请求ID相关的CompletableFuture，并将其设置为完成
            UnProcessedRequests.complete(rpcResponse);
            ctx.close();
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

}
```

- UnProcessedRequests
  - 实际是一个Map, key为请求ID, 所以请求ID可不能重复, value是CompletableFuture对象

```java
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
```