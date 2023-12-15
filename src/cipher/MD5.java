package cipher;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String toMd5(String str) {
		byte[] secretBytes = null;
		try {
			secretBytes = MessageDigest.getInstance("md5").digest(str.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String md5code = new BigInteger(1, secretBytes).toString(16);
		for (int i = 0; i < 32-md5code.length(); i++) {
			md5code = "0"+md5code;
		}
		System.out.println(md5code);
		return md5code;
	}
	public static void main(String[] args) throws Exception {
		String str = "edmond";
		toMd5(str);
	}
}
