package cipher;

import java.io.File;

public class Cipher {
	String algorithm;
	int bit;
	File file;


	public Cipher(String algorithm, int bit, String filePath) {
		this.algorithm = algorithm;
		this.bit = bit;
		this.file = new File(filePath);
		//检查密钥是否存在
	}


	static public void createKeyFile(String algorithm, int bit, File file) {
		if (algorithm.equals("AES")) {
			AES aes=new AES(bit);
			ReadWrite.writeStringToFile(file, aes.getKeyString());
		}
	}
}
