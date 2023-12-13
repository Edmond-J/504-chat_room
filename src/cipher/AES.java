package cipher;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AES {
	int bit;
	SecretKey key;

	public AES(int bit) {
		this.bit = bit;
	}

	/**
	* 随机生成秘钥
	*/
	public static void getKey() {
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			// 要生成多少位，只需要修改这里即可128, 192或256
			SecretKey sk = kg.generateKey();
			byte[] b = sk.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	* 使用指定的字符串生成秘钥
	*/
	public static void getKeyByPass(String password) {
		// 生成秘钥
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			// kg.init(128);//要生成多少位，只需要修改这里即可128, 192或256
			// SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以生成的秘钥就一样。
			kg.init(128, new SecureRandom(password.getBytes()));
			SecretKey sk = kg.generateKey();
			byte[] b = sk.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
	}
}
