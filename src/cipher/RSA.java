package cipher;

import java.io.File;
import java.io.FileWriter;
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

/*`return Base64.getEncoder().encodeToString(encryptedBytes);` 这一步的作用是什么？为什么不能直接return new String(encryptedBytes);
这行代码的作用是将加密后得到的字节数组（`encryptedBytes`）编码为一个 Base64 字符串。这一步是重要的，因为不是所有的字节组合都可以直接转化为有效的字符串。
下面是为什么不建议直接使用 `new String(encryptedBytes)` 的几个原因：
1. **不安全的字符：** 加密后得到的字节数组可能包含任意的字节值，这些值可能不对应于任何字符编码的有效或可打印字符。在某些字符集下，这些无效的字节序列可能导致编码错误或数据丢失。
2. **数据丢失风险：** 当字节数组包含不对应字符集的值时，直接转换为 String 可能会丢失信息。这是因为字符串转换过程中无效的字节序列可能被忽略或替换为替代字符（如 `�`），从而原加密数据将不能被正确地还原。
3. **编码不一致：** 字符编码的不一致可能导致不同系统或平台间的兼容性问题。字节到字符串的转换依赖于系统的默认字符编码，如果两个系统的默认编码不同，可能会导致解码失败，数据无法正确解密。
相反，Base64 编码是一种二进制至文本的编码方法，用于在字节数据和字符串之间进行安全地转换。它将原始二进制数据编码为一组只包含 ASCII 字符的字符串，这些字符串可以安全地显示和传输，而且能确保在不同的系统和编码设置之间保持数据的完整性。
因此，使用 Base64 编码是加密数据的标准做法，在需要将二进制数据转化为字符串时应当广泛采用。在解密过程中，应当先将加密的 Base64 字符串解码回原始的字节数据，然后再进行解密。这样可以确保数据的安全、完整并保持不变。
*/
public class RSA {
	KeyPair pair;
	PrivateKey privateKey;
	PublicKey publicKey;

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

	static public void writeByteToFile(File file, byte[] toWrite) {
		try {
			String keyString = Base64.getEncoder().encodeToString(toWrite);
			FileWriter fos = new FileWriter(file);
			fos.write(keyString);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public void writeStringToFile(File file, String toWrite) {
		try {
			FileWriter fos = new FileWriter(file);
			fos.write(toWrite);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
