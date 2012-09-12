import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.nexr.hmc.model.bo.Signal;

public class TestSignal {
	String signal_schema = " SG_ CF_Acu_NumOfFlt : 24|8@1+ (1,0) [0|0] \"aaa\" Vector__XXX";
        
	@Test
	public void testSignal() {
		Signal signal = new Signal(signal_schema);
		System.out.println(signal.toString());
		Assert.assertEquals("CF_Acu_NumOfFlt", signal.getSignalName());
		Assert.assertEquals(24, signal.getStartBit());
		Assert.assertEquals(8, signal.getLengthBit());
	}

}
