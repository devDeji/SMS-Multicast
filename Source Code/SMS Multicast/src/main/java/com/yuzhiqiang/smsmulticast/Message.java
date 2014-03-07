package com.yuzhiqiang.smsmulticast;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private final UUID uuid;
    public String destination;
    public String content;
    public int status;
    public static final int PENDING = 0;
    public static final int SENDING = 1;
    public static final int SENT = 2;
    public static final int FAILED = -1;

    public Message(String destination, String content) {
        this.uuid = UUID.randomUUID();
        this.destination = destination;
        this.content = content;
        this.status = PENDING;
    }

    @Override
    public boolean equals(Object o) {
        return (((Message) o).uuid.equals(this.uuid));
    }
}
