package com.nexr.hmc.model.bo;

import java.util.ArrayList;
import java.util.List;

import com.nexr.hmc.builder.MessageBuilder;

public class Message {
	private String id;
	private String type;
	private String messageName;
	private int dlc;
	private String transmitter;
	private List<Signal> signal;
	private MessageBuilder builder;

	public Message(MessageBuilder builder, String line) {
		String[] mes = line.split(" ");
		if (mes.length == 5) {
			this.signal = new ArrayList<Signal>();
			this.setBuilder(builder);
			this.setType(mes[0]);
			this.setId(mes[1]);
			if (mes[2].endsWith(":")) {
				this.setMessageName(mes[2].substring(0, mes[2].length()-1));
			} else { 
				this.setMessageName(mes[2]);
			}
			this.setDlc(Integer.parseInt(mes[3]));
			this.setTransmitter(mes[4]);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public List<Signal> getSignal() {
		return signal;
	}

	public void setSignal(Signal signal) {
		this.signal.add(signal);
	}

	public MessageBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(MessageBuilder builder) {
		this.builder = builder;
	}

	public int getDlc() {
		return dlc;
	}

	public void setDlc(int dlc) {
		this.dlc = dlc;
	}

	public String getTransmitter() {
		return transmitter;
	}

	public void setTransmitter(String transmitter) {
		this.transmitter = transmitter;
	}

//	@Override
//	public String toString() {
//		return "Message [id=" + id + ", type=" + type + ", messageName="
//				+ messageName + ", dlc=" + dlc + ", transmitter=" + transmitter
//				+ ", signal=" + signal
//				+ ", builder=" + builder.getName() 
//				+ " signalCnt = " + signal.size() + "]";
//	}
}