package cipher;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DES {
//	SecretKey key;
//	String keyString;

	static public String generateKey() {
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("DES");
			SecretKey key = keyGen.generateKey();
			String keyString = Base64.getEncoder().encodeToString(key.getEncoded());
			return keyString;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public String encrypt(String message, String keyString) {
		try {
			byte[] raw = Base64.getDecoder().decode(keyString);
			SecretKey key = new SecretKeySpec(raw, 0, raw.length, "DES");
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] byteDataToEncrypt = message.getBytes();
			byte[] encrypted = cipher.doFinal(byteDataToEncrypt);
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public String decrypt(String message, String keyString) {
		try {
			byte[] raw = Base64.getDecoder().decode(keyString);
			SecretKey key = new SecretKeySpec(raw, 0, raw.length, "DES");
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] byteDataToDecrypt = Base64.getDecoder().decode(message);
			byte[] decrypted = cipher.doFinal(byteDataToDecrypt);
			return new String(decrypted, "utf-8");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		//for testing purpose
		String keyString=DES.generateKey();
		String messString = "Good morning, EdmondJin! It's a good day";
		String encryptedMessage = DES.encrypt(messString, keyString);
		String decryptedMessage = DES.decrypt(encryptedMessage, keyString);
		System.out.println(encryptedMessage);
		System.out.println(decryptedMessage);
	}
}
