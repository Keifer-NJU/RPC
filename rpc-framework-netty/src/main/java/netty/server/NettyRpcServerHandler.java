package netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import remoting.dto.RpcRequest;
import remoting.dto.RpcResponse;
import remoting.handler.RpcRequestHandler;

/**
 * @author Keifer
 * @createTime 2021/5/12 17:27
 */
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyRpcServerHandler.class);
    private static final RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) (msg);
            Object result = rpcRequestHandler.handle(rpcRequest);
            ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
