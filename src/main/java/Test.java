import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nexr.hmc.ssh.JschRunner;
import com.nexr.hmc.ssh.SshExecCommand;
import com.nexr.hmc.ssh.util.IoUtil;


public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		File f = new File("/Users/ryan/workspaces/dev-nexr-collector/can-parser/tmp.txt");
		JschRunner runner = new JschRunner("nexr", "nexr02");
    runner.setPassword("nexr1234");
    SshExecCommand command = new SshExecCommand("ls " + "~/", IoUtil.closeProtectedStream(new FileOutputStream(f)));
    try {
			runner.run(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    
    InputStream is;
    try {
        is = runner.openFile("~/memtest.sh");
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        System.out.println(out.toString());
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
	}
}
