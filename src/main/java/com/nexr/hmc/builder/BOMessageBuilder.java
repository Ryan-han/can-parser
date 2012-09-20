package com.nexr.hmc.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.nexr.hmc.MessageFectory;
import com.nexr.hmc.model.bo.Message;
import com.nexr.hmc.model.bo.Signal;

public class BOMessageBuilder implements MessageBuilder {
    static Logger log = Logger.getLogger(BOMessageBuilder.class);
    static SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss a yyyy", Locale.ENGLISH);
    static String dateFlag = "Begin Triggerblock";

    Message message = null;
    long beginTime;

    public String getName() {
        return "BO_MessageBuilder";
    }

    public void loadMessage(String line) {
        if (!line.startsWith(" ")) {
            if (line.length() > 0) {
                if (message != null) {
                    MessageFectory.messageList.put(message.getId(), message);
                }
                message = new Message(this, line);
            }
        } else {
            message.setSignal(new Signal(line));
        }
    }

    public List<String> dataParse(String data) {
        List<String> result = new ArrayList<String>();
        String dataLine = data.toString();
        if (dataLine.startsWith(dateFlag)) {
            System.out.println(dataLine.substring(dataLine.indexOf(dateFlag) + dateFlag.length(), dataLine.length()).trim());
            try {
                beginTime = format.parse(dataLine.substring(dataLine.indexOf(dateFlag) + dateFlag.length(), dataLine.length()).trim()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (dataLine.contains("Rx")) {
            String[] lineCols = data.trim().split(" ");
            List<String> cols = new ArrayList<String>();
            for (String col : lineCols) {
                if (col.trim().length() > 0) {
                    cols.add(col);
                }
            }

            if (cols.size() < 6) {
                log.error(data + "doesn't have data");
            } else {
                String msgParserId = String.valueOf(Integer.parseInt(cols.get(2), 16));
                if (MessageFectory.messageList.containsKey(msgParserId)) {
                    Message message = MessageFectory.messageList.get(msgParserId);
                    log.debug("[" + cols.get(2) + "] Find " + msgParserId + "  Parser" + message.toString());
                    for (Signal signal : MessageFectory.messageList.get(msgParserId).getSignal()) {
                        String dec = hexTodec(cols);
                        int startPos = signal.getStartBit();
                        int length = signal.getLengthBit();
                        StringBuilder body = new StringBuilder();
                        if (dec.length() >= startPos + length) {
                            long be = getNanoTimes(beginTime, cols.get(0));
                            body.append(message.getId() + "\t" + null + "\t" + null + "\t" + be + "\t" + signal.getSignalName() + "\t" + signal.getFactor() + "\t" + signal.getOffset() + "\t"
                                    + signal.getMin() + "\t" + signal.getMax() + "\t"
                                    + getValue(dec.substring(startPos, startPos + length), signal.getValueType(), signal.getMin(), signal.getMax()) + "\t"
                                    + signal.getTransmitter());

                            log.debug(body.toString());

                            result.add(body.toString());
                        } else {
                            // log.error("Invalid Data " + data);
                            break;
                        }
                    }
                } else {
                    // log.error("Not Find [" + cols.get(2) + "] " + msgParserId);
                }
            }
        }

        return result;
    }

    int invalidCount = 0;

    private long getValue(String binary, String type, String min, String max) {
        if (binary.length() == 1) {
            return Integer.parseInt(binary);
        }
        long result = 0;
        if (type.equals("-")) {
            result = signedValue(binary);
        } else {
            result = unsignedValue(binary);
        }

        double dmin = Double.parseDouble(min);
        double dmax = Double.parseDouble(max);

        if (result < dmin || result > dmax) {
            invalidCount++;
            // log.error(signalName + " Result invalid Value : " + result + " Min : "
            // + dmin + " Max : " + dmax + " Data :" + data + " " + invalidCount);
        }
        return result;

    }

    public String hexTodec(List<String> cols) {
        List<String> datas = cols.subList(6, cols.size());
        String cHex = "";
        for (String data : datas) {
            int dataHex = Integer.parseInt(data, 16);
            String g = String.format("%08d", Integer.parseInt(Integer.toBinaryString(dataHex)));
            cHex += g;

        }
        return cHex;
    }

    public long signedValue(String binary) {
        long result = 0;
        boolean negative = false;
        if (binary.startsWith("1")) {
            negative = true;
            for (int i = 0; i < binary.length(); i++) {
                if (binary.charAt(i) == '0') {
                    result += Math.pow(2, binary.length() - (i + 1));
                }
            }
        } else {
            for (int i = 1; i < binary.length(); i++) {
                if (binary.charAt(i) == '1') {
                    result += Math.pow(2, binary.length() - (i + 1));
                }
            }
        }

        if (negative) {
            result += 1;
            result -= result * 2;
        }
        return result;
    }

    public long unsignedValue(String binary) {
        long result = 0;
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                result += Math.pow(2, binary.length() - (i + 1));
            }
        }
        return result;
    }

    private static long getNanoTimes(long current, String underSecs) {
        long result = 0;
        if (!underSecs.substring(0, underSecs.indexOf(".")).contains("*")) {
            long seconds = Long.parseLong(underSecs.substring(0, underSecs.indexOf("."))) * 1000 * 1000;
            long nanos = Long.parseLong(underSecs.substring(underSecs.indexOf(".") + 1, underSecs.trim().length()));

            result = current * 1000;
            result = (result + seconds + nanos) * 1000;
        } else {
            System.out.println(underSecs);
        }
        return result;

    }
    
    public static void main(String args[]) {
    	String data = "4F";
    	
    	int dataHex = Integer.parseInt(data, 16);
      String g = String.format("%08d", Integer.parseInt(Integer.toBinaryString(dataHex)));
      System.out.println(g);
      
      int myint = Integer.parseInt(g, 2);
      
      
      System.out.println(myint);
      
     System.out.println(Integer.toString(myint, 8)); 

    }
}
