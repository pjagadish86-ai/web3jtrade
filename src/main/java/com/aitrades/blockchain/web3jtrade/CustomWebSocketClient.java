package com.aitrades.blockchain.web3jtrade;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.framing.Framedata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.websocket.WebSocketClient;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CustomWebSocketClient extends WebSocketClient {
	
    private static final Logger log = LoggerFactory.getLogger(CustomWebSocketClient.class);

    public interface  ReconnectHandlerInterface{
        void onReconnect();
    }
    Queue<ReconnectHandlerInterface> handlers = new ConcurrentLinkedQueue<ReconnectHandlerInterface>();
    @Synchronized
    private void doReconnect(){
        if(this.isClosed()){
            try {
                this.reconnectBlocking();
                for (ReconnectHandlerInterface handler : handlers) {
                    handler.onReconnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Current thread needs to shutdown, Reconnect to websocket failed");
            }
        }
    }
    public void addReconnectHandler(ReconnectHandlerInterface handler){
        handlers.add(handler);
    }
    public void removeReconnectHandler(ReconnectHandlerInterface handler){
        handlers.remove(handler);
    }
    public CustomWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public CustomWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void send(String text) throws NotYetConnectedException {
        doReconnect();
        super.send(text);
    }

    @Override
    public void send(byte[] data) throws NotYetConnectedException {
        doReconnect();
        super.send(data);
    }

    @Override
    public void sendPing() throws NotYetConnectedException {
        doReconnect();
        super.sendPing();
    }

    @Override
    @Synchronized
    public void onClose(int code, String reason, boolean remote) {
        if(remote || code!=1000){
            this.doReconnect();
            log.info("Reconnecting WebSocket connection to {}, because of disconnection reason: '{}'.",uri, reason);
        }else {
            super.onClose(code, reason, remote);
        }
    }

    @Override
    public void sendFragmentedFrame(Framedata.Opcode op, ByteBuffer buffer, boolean fin) {
        doReconnect();
        super.sendFragmentedFrame(op, buffer, fin);
    }

    @Override
    public void send(ByteBuffer bytes) throws IllegalArgumentException, NotYetConnectedException {
        doReconnect();
        super.send(bytes);
    }

    @Override
    public void sendFrame(Framedata framedata) {
        doReconnect();
        super.sendFrame(framedata);
    }

    @Override
    public void sendFrame(Collection<Framedata> frames) {
        doReconnect();
        super.sendFrame(frames);
    }
}