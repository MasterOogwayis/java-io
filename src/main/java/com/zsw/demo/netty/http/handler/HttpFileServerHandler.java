package com.zsw.demo.netty.http.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderUtil.setContentLength;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.util.CharsetUtil.UTF_8;

//import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * @author ZhangShaowei on 2019/9/27 13:29
 **/
@Slf4j
@AllArgsConstructor
@ChannelHandler.Sharable
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOW_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9.]*");

    private String rootPath;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.decoderResult().isFailure()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (!HttpMethod.GET.name().equalsIgnoreCase(request.method().name())) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.uri();
        final String path = sanitizeUri(uri);

        if (Objects.isNull(path)) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        File file = filePath.toFile();

        if (file.isHidden() || !file.exists()) {
            // 404
            sendError(ctx, NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                sendRedirect(ctx, uri + "/");
            }
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (Exception e) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = randomAccessFile.length();

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
        setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        if (isKeepAlive(request)) {
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }
        ctx.write(response);

        ChannelFuture channelFuture = ctx.write(
                new ChunkedFile(randomAccessFile, 0, fileLength, 8 * 1024), ctx.newProgressivePromise());
        channelFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if (total < 0) {
                    log.error("Transfer progress: {}", progress);
                } else {
                    log.error("Transfer progress: {}/{}", progress, total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                log.info("Transfer complete.");
            }
        });

        //如果使用Chunked编码，最后则需要发送一个编码结束的看空消息体，进行标记，表示所有消息体已经成功发送完成。
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //如果当前连接请求非Keep-Alive ，最后一包消息发送完成后 服务器主动关闭连接
        if (!isKeepAlive(request)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file));
    }


    private String sanitizeUri(String uri) {
        String decode;
        try {
            decode = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                decode = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                throw new Error(ex);
            }
        }

        if (!decode.startsWith(this.rootPath)) {
            return null;
        }

        if (!decode.startsWith("/")) {
            return null;
        }

        decode = decode.replace('/', File.separatorChar);
        if (decode.contains(File.separator + ".") || decode.contains("." + File.separator)
                || INSECURE_URI.matcher(decode).matches()) {
            return null;
        }
        return decode.startsWith(File.separator) ? decode : File.separator + decode;
    }

    private void sendListing(ChannelHandlerContext ctx, File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
        StringBuilder stringBuilder = new StringBuilder();
        String dirPath = dir.getPath();
        stringBuilder.append("<!DOCTYPE html>\r\n");
        stringBuilder.append("<html><head><title>");
        stringBuilder.append(dirPath);
        stringBuilder.append(" 目录： ");
        stringBuilder.append("</title></head><body>\r\n");
        stringBuilder.append("<h3>");
        stringBuilder.append(dirPath).append("  目录：");
        stringBuilder.append("</h3>\r\n");
        stringBuilder.append("<ul>");
        stringBuilder.append("<li>连接：<a href=\"../\">..</a></li>\r\n");
        for (File file : dir.listFiles()) {
            if (file.isHidden() || !file.canRead()) {
                continue;
            }

            String name = file.getName();
            if (!ALLOW_FILE_NAME.matcher(name).matches()) {
                continue;
            }

            stringBuilder.append("<li> 连接：<a href=\"");
            stringBuilder.append(name);
            stringBuilder.append("\">");
            stringBuilder.append(name);
            stringBuilder.append("</a></li>\r\n");
        }

        stringBuilder.append("</ul></body></html>\r\n");
        ByteBuf byteBuf = Unpooled.copiedBuffer(stringBuilder, UTF_8);
        response.content().writeBytes(byteBuf);
        byteBuf.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, FOUND);
        response.headers().set(LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", UTF_8)
        );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }
}