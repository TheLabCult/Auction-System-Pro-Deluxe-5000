package com.auction.shared.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.UUID;

public final class Message {

    private String requestId;
    private MessageType type;
    private JsonElement payload;

    public static Message of(MessageType type, Object payloadObj, Gson gson) {
        Message m = new Message();
        m.requestId = UUID.randomUUID().toString();
        m.type = type;
        m.payload = gson.toJsonTree(payloadObj);   
        return m;
    }

    public static Message broadcast(MessageType type, Object payloadObj, Gson gson) {
        return of(type, payloadObj, gson);  // Thay tên cho dễ hình dung ở hàm gọi
    }

    public static Message reply(String requestId, MessageType type, Object payloadObj, Gson gson) {
        Message m = new Message();
        m.requestId = requestId;                    
        m.type = type;
        m.payload = gson.toJsonTree(payloadObj);
        return m;
    }


    public String getRequestId() { return requestId; }
    public MessageType getType() { return type; }
    public JsonElement getPayload() { return payload; }

    public <T> T parsePayload(Gson gson, Class<T> clazz) {
        return gson.fromJson(payload, clazz);
    }

    @Override
    public String toString() {
        return "Message{requestId='" + requestId + "', type=" + type + '}';
    }
}
