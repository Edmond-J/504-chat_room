package cipher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RSA {
	KeyPairGenerator keyGen;
	KeyPair pair;
	PrivateKey privateKey;
	PublicKey publicKey;

	public RSA(int length) {
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");// KeyPairGenerator(String algorithm)
															// 构造器是private的，所以不能通过new来创建实例，不清除为什么要这样设计
			keyGen.initialize(length);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void createKeys() {
		pair = keyGen.generateKeyPair();
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();
	}

	public void writeKeyToFile(String path, byte[] key) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(key);
		fos.flush();
		fos.close();
	}

	public PrivateKey getPrivate(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	public PublicKey getPublic(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}

	public void encryptFile(byte[] input, File output, PrivateKey key) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		writeDataToFile(output, cipher.doFinal(input));
	}

	public void decryptFile(byte[] input, File output, PublicKey key) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		writeDataToFile(output, cipher.doFinal(input));
	}

	private void writeDataToFile(File output, byte[] toWrite)
			throws IllegalBlockSizeException, BadPaddingException, IOException {
		FileOutputStream fos = new FileOutputStream(output);
		fos.write(toWrite);
		fos.flush();
		fos.close();
	}

	public byte[] getFileInBytes(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		byte[] fbytes = new byte[(int)f.length()];
		fis.read(fbytes);
		fis.close();
		return fbytes;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}
}
