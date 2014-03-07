package com.yuzhiqiang.smsmulticast;

import java.io.Serializable;

public class Message implements Serializable{
    public String destination;
    public String content;

    public Message(String destination, String content) {
        this.destination = destination;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        Message m = (Message) o;
        return (destination.equals(m.destination) && content.equals(m.content));
    }
}
