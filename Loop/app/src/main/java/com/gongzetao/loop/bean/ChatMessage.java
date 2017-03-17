package com.gongzetao.loop.bean;

/**
 * Created by baixinping on 2016/8/12.
 */
public class ChatMessage {
    private int type;
    private long time;
    private String content;

    public int getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public ChatMessage(int type, long time, String content) {
        this.type = type;
        this.time = time;
        this.content = content;

    }
}
