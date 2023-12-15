package cipher;

import java.io.File;

public class CipherBox {
	String algorithm;
	int bit;
	File file;

	public CipherBox(String algorithm, int bit, String filePath) {
		this.algorithm = algorithm;
		this.bit = bit;
		this.file = new File(filePath);
		// 检查密钥是否存在
	}

	static public void createKeyFile(String algorithm, int bit, File file) {
		if (algorithm.equals("AES")) {
			ReadWrite.writeStringToFile(file, AES.generateKey(bit));
		} else if (algorithm.equals("DES")) {
			ReadWrite.writeStringToFile(file, DES.generateKey());
		} else System.out.println(algorithm+" is not implemented");
	}
}
