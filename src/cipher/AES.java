package cipher;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	static public String generateKey(int length) {
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(length);
			SecretKey key = generator.generateKey();
			String keyString = Base64.getEncoder().encodeToString(key.getEncoded());
			return keyString;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public String encrypt(String message, String keyString) throws Exception {
		byte[] raw = keyString.getBytes("utf-8");
		SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encrypted = cipher.doFinal(message.getBytes("utf-8"));
		return Base64.getEncoder().encodeToString(encrypted);
	}

	static public String decrypt(String message, String keyString) throws Exception {
		byte[] raw = keyString.getBytes("utf-8");
		SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] byteDataToDecrypt = Base64.getDecoder().decode(message);// 先用base64解密
		byte[] decrypted = cipher.doFinal(byteDataToDecrypt);
		return new String(decrypted, "utf-8");
	}

	static public String fakeEncryption(String message) {
		return message;
	}

	static public String fakeDecryption(String message) {
		return message;
	}

	public static void main(String[] args) throws Exception {
		String keyString = generateKey(128);
		System.out.println(keyString);
		String messString = "Good morning, EdmondJin! It's a good day";
		String encryptedMessage = AES.encrypt(messString, keyString);
		String decryptedMessage = AES.decrypt(encryptedMessage, keyString);
		System.out.println(encryptedMessage);
		System.out.println(decryptedMessage);
	}
}
