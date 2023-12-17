package cipher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA {
	private KeyPair pair;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public RSA(int length) {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			// KeyPairGenerator(String algorithm)构造器是private的，所以不能通过new来创建实例，不清除为什么要这样设计
			keyGen.initialize(length);
			pair = keyGen.generateKeyPair();
			privateKey = pair.getPrivate();
			publicKey = pair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	static public String encrypt(String message, PublicKey key) throws IOException, GeneralSecurityException {
		byte[] messageInByte = message.getBytes();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = cipher.doFinal(messageInByte);
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	static public String decrypt(String encryptedMessage, PrivateKey key) throws IOException, GeneralSecurityException {
		byte[] messageInByte = Base64.getDecoder().decode(encryptedMessage);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedBytes = cipher.doFinal(messageInByte);
		return new String(decryptedBytes);
	}

	static public PrivateKey getPrivateFromFile(String filename) throws Exception {
		String privateKeyString = new String(Files.readAllBytes(Paths.get(filename)));
		byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	static public PublicKey getPublicFromFile(String filename) throws Exception {
		String publicKeyString = new String(Files.readAllBytes(Paths.get(filename)));
		byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public static void main(String[] args) {
		String message = "good morning Edmond";
		RSA rsa = new RSA(1024);
		try {
			String enMessage = RSA.encrypt(message, rsa.publicKey);
			System.out.println("->Encrypted: \n"+enMessage);
			String deMessage = RSA.decrypt(enMessage, rsa.privateKey);
			System.out.println("->Decrypted: \n"+deMessage);
		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
