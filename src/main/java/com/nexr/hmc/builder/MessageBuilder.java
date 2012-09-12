package com.nexr.hmc.builder;

import java.util.List;

public interface MessageBuilder {
	
	public String getName();
	
	public void loadMessage(String line);
	
	public List<String> dataParse(String data);
}
