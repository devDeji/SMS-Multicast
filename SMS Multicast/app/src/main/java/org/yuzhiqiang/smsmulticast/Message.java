package org.yuzhiqiang.smsmulticast;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
	public static final int NEW = 0;
	public static final int PENDING = 1;
	public static final int SENDING = 2;
	public static final int SENT = 3;
	public static final int FAILED = -1;
	private final UUID uuid;
	public String destination;
	public String content;
	public int status;

	public Message(String destination, String content) {
		this.uuid = UUID.randomUUID();
		this.destination = destination;
		this.content = content;
		this.status = NEW;
	}

	@Override
	public boolean equals(Object o) {
		return (((Message) o).uuid.equals(this.uuid));
	}

	public boolean isSentOut() {
		return (status > 0);
	}
}
