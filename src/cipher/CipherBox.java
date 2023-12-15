package cipher;

import java.io.File;

public class CipherBox {
	String algorithm;
	int bit;
	File file;

	public CipherBox(String algorithm, int bit, File file) {
		this.algorithm = algorithm;
		this.bit = bit;
		this.file = file;
		// 检查密钥是否存在
	}

	static public void createKeyFile(String algorithm, int bit, File file) {
		if (algorithm.equals("AES")) {
			ReadWrite.writeStringToFile(file, AES.generateKey(bit));
		} else if (algorithm.equals("DES")) {
			ReadWrite.writeStringToFile(file, DES.generateKey());
		} else System.out.println(algorithm+" is not implemented");
	}

	public String encrypt(String message) {
		String encryptedString = "";
		if (algorithm.equals("AES")) {
			encryptedString = AES.encrypt(message, ReadWrite.readStringFromFile(file));
		} else if (algorithm.equals("DES")) {
			encryptedString = DES.encrypt(message, ReadWrite.readStringFromFile(file));
		} else System.out.println(algorithm+" is not implemented");
		return encryptedString;
	}

	static public String encrypt(String message, String algorithm, String key) {
		String encryptedString = "";
		if (algorithm.equals("AES")) {
			encryptedString = AES.encrypt(message, key);
		} else if (algorithm.equals("DES")) {
			encryptedString = DES.encrypt(message, key);
		} else System.out.println(algorithm+" is not implemented");
		return encryptedString;
	}

	public String decrypt(String message) {
		String decryptedString = "";
		if (algorithm.equals("AES")) {
			decryptedString = AES.decrypt(message, ReadWrite.readStringFromFile(file));
		} else if (algorithm.equals("DES")) {
			decryptedString = DES.decrypt(message, ReadWrite.readStringFromFile(file));
		} else System.out.println(algorithm+" is not implemented");
		return decryptedString;
	}

	static public String decrypt(String message, String algorithm, String key) {
		String decryptedString = "";
		if (algorithm.equals("AES")) {
			decryptedString = AES.decrypt(message, key);
		} else if (algorithm.equals("DES")) {
			decryptedString = DES.decrypt(message, key);
		} else System.out.println(algorithm+" is not implemented");
		return decryptedString;
	}
}
