package cipher;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	String keyString;

	public AES(int length) {
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(length);
			SecretKey key = generator.generateKey();
			keyString = Base64.getEncoder().encodeToString(key.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
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
		try {
			byte[] raw = keyString.getBytes("utf-8");
			SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] encrypted1 = Base64.getDecoder().decode(message);// 先用base64解密
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original, "utf-8");
				return originalString;
			} catch (Exception e) {
				System.out.println(e.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}

	public String getKeyString() {
		return keyString;
	}

	static public String fakeEncryption(String message) {
		return message;
	}

	static public String fakeDecryption(String message) {
		return message;
	}

	public static void main(String[] args) throws Exception {
		AES aes = new AES(128);
		System.out.println(aes.keyString);
		String messString = "Good morning, EdmondJin! It's a good day";
		String encryptedMessage = AES.encrypt(messString, aes.keyString);
		String decryptedMessage = AES.decrypt(encryptedMessage, aes.keyString);
		System.out.println(encryptedMessage);
		System.out.println(decryptedMessage);
	}
}
