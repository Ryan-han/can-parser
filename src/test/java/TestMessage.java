import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.nexr.hmc.builder.BOMessageBuilder;
import com.nexr.hmc.model.bo.Message;

public class TestMessage {
	static Logger log = Logger.getLogger(TestMessage.class);
	
	String message_schema = "BO_ 3221225472 VECTOR__INDEPENDENT_SIG_MSG: 0 Vector__XXX";
        
	@Test
	public void testSignal() {
		Message message = new Message(new BOMessageBuilder(), message_schema);
		log.info(message.toString());
		Assert.assertEquals("3221225472", message.getId());
		Assert.assertEquals("BO_", message.getType());
		Assert.assertEquals("VECTOR__INDEPENDENT_SIG_MSG", message.getMessageName());
	}

}
