package com.nexr.hmc.model.bo;

import java.util.ArrayList;
import java.util.List;

public class Signal {
	private String type;
	private int startBit;
	private int lengthBit;
	private String signalName;
	private String byteOrder;
	private String valueType;
	private String factor;
	private String offset;
	private String min;
	private String max;
	private String unit;
	private String transmitter;

	public Signal(String data) {
		String line = data.trim();

		String type = line.substring(0, line.trim().indexOf(" ")).trim();
		String name = line.substring(line.indexOf(" ") + 1, line.indexOf(":"))
				.trim();
		String bits = line.substring(line.indexOf(":") + 1, line.indexOf("("))
				.trim();
		String fac_offset = line
				.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
		String min_max = line.substring(line.indexOf("[") + 1, line.indexOf("]"))
				.trim();
		String unit = line
				.substring(line.indexOf("\""), line.lastIndexOf("\"") + 1).trim();
		String transmitter = line.substring(line.lastIndexOf("\"") + 1,
				line.length()).trim();
		
		this.setType(type);
		this.setSignalName(name);
		this.setUnit(unit);
		this.setTransmitter(transmitter);

		this.setStartBit(Integer.parseInt(bits.substring(0, bits.indexOf("|"))));
		this.setLengthBit(Integer.parseInt(bits.substring(bits.indexOf("|") + 1,
				bits.indexOf("@"))));

		this.setByteOrder(bits.substring(bits.indexOf("@") + 1,
				bits.indexOf("@") + 2));
		this.setValueType(bits.substring(bits.indexOf("@") + 2, bits.length()));

		this.setFactor(fac_offset.substring(0, fac_offset.indexOf(",")));
		this.setOffset(fac_offset.substring(fac_offset.indexOf(",") + 1,
				fac_offset.length()));

		this.setMin(min_max.substring(0, min_max.indexOf("|")));
		this.setMax(min_max.substring(min_max.indexOf("|") + 1, min_max.length()));

	}

	public int getStartBit() {
		return startBit;
	}

	public void setStartBit(int startBit) {
		this.startBit = startBit;
	}

	public int getLengthBit() {
		return lengthBit;
	}

	public void setLengthBit(int lengthBit) {
		this.lengthBit = lengthBit;
	}

	public String getSignalName() {
		return signalName;
	}

	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}

	public String getByteOrder() {
		return byteOrder;
	}

	public void setByteOrder(String byteOrder) {
		this.byteOrder = byteOrder;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getFactor() {
		return factor;
	}

	public void setFactor(String factor) {
		this.factor = factor;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getTransmitter() {
		return transmitter;
	}

	public void setTransmitter(String transmitter) {
		this.transmitter = transmitter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Signal [type=" + type + ", startBit=" + startBit + ", lengthBit="
				+ lengthBit + ", signalName=" + signalName + ", byteOrder=" + byteOrder
				+ ", valueType=" + valueType + ", factor=" + factor + ", offset="
				+ offset + ", min=" + min + ", max=" + max + ", unit=" + unit
				+ ", transmitter=" + transmitter + "]";
	}

}
