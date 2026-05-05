package com.auction.shared.network;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private String action; //yêu cầu "LOGIN", "GET_AUCTIONS", "PLACE_BID",... gửi lên sv
    private Object payload; //dữ liệu kèm theo (vd: BidTransaction)
    public Request(String action, Object payload) {
        this.action = action;
        this.payload = payload;
    }
    public String getAction() {
        return action;
    }
    public Object getPayload() {
        return payload;
    }
}
