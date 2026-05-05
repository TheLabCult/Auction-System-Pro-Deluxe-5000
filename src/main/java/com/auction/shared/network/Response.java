package com.auction.shared.network;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private String status; //"SUCCESS" / "ERROR"
    private String message; //"dat gia thanh cong", "phiên đóng", ...
    private Object data; //du lieu tra ve cho client
    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
