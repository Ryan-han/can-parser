import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.nexr.hmc.ssh.JschRunner;
import com.nexr.hmc.ssh.SshExecCommand;
import com.nexr.hmc.ssh.util.IoUtil;

public class Test {
	public static void main(String[] args) throws IOException {
		// File f = new File(
		// "/Users/ryan/workspaces/dev-nexr-collector/can-parser/tmp.txt");
		// File out = new File(
		// "/Users/ryan/workspaces/dev-nexr-collector/can-parser/out.txt");

		// JschRunner runner = new JschRunner("nexr", "nexr02");
		// runner.setPassword("nexr1234");
		// SshExecCommand command = new SshExecCommand("ls " + "~/",
		// IoUtil.closeProtectedStream(new FileOutputStream(f)));
		// try {
		// runner.run(command);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		//
		// InputStream is;
		// try {
		// is = runner.openFile("~/memtest.sh");
		// StringBuffer out = new StringBuffer();
		// byte[] b = new byte[4096];
		// for (int n; (n = is.read(b)) != -1;) {
		// out.append(new String(b, 0, n));
		// }
		// System.out.println(out.toString());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		// FileOutputStream fos = new FileOutputStream(out);
		// FileInputStream fis = new FileInputStream(f);
		// InputStreamReader in = new InputStreamReader(fis);
		//
		// for (byte i=1; i<=10; i++) {
		// fos.write(i);
		// }
		//
		// int c;
		// while ((c=in.read()) != -1) {
		// System.out.println(c + " ");
		// }
		//
		// in.close();
		// fos.close();

		// TODO Auto-generated method stub
		try {
			String dirPath = "./";

			String fileName = "BINARY.dat";
			File f = new File(dirPath, fileName); // 파일객체생성
			f.createNewFile(); // 파일생성
			FileOutputStream out = new FileOutputStream(f);

			// AS_KEYWORD 필드에 200byte 설정, SUM_COUNT 필드에 10byte 설정
			// C언어에서 이진탐색을 하기 위함
			String AS_KEYWORD = "111111";
			// EUC-KR로 설정해야 C언어에서 char[]배열로 정상적으로 읽어들일 수 있음
			System.out.println(AS_KEYWORD.getBytes().length);
			byte[] bin_AS_KEYWORD = AS_KEYWORD.getBytes("UTF-8");

			if (bin_AS_KEYWORD.length < 200) // 200byte로 잘라서 저장
			{
				out.write(bin_AS_KEYWORD);
				// 200byte 미만인 경우 나머지는 빈 배열로 채워서 저장
				byte[] bin_dummy1 = new byte[200 - bin_AS_KEYWORD.length];
				out.write(bin_dummy1);
			} else {
				out.write(bin_AS_KEYWORD, 0, 200);
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}
}
