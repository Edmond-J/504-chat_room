package cipher;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

public class TestRSA {
	public TestRSA() {
	}

	public static void main(String[] args) {
		try {
			RSA rsa = new RSA(1024);
			rsa.createKeys();
			rsa.writeKeyToFile("KeyPair/publicKey", rsa.publicKey.getEncoded());
			rsa.writeKeyToFile("KeyPair/privateKey", rsa.privateKey.getEncoded());
			PrivateKey privateKey = rsa.getPrivate("KeyPair/privateKey");
			PublicKey publicKey = rsa.getPublic("KeyPair/publicKey");
			if (new File("KeyPair/text.txt").exists()) {
				rsa.encryptFile(rsa.getFileInBytes(new File("KeyPair/text.txt")),
						new File("KeyPair/text_encrypted.txt"), privateKey);
				rsa.decryptFile(rsa.getFileInBytes(new File("KeyPair/text_encrypted.txt")),
						new File("KeyPair/text_decrypted.txt"), publicKey);
			} else {
				System.out.println("Create a file text.txt under folder KeyPair");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
