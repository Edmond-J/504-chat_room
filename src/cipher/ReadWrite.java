package cipher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ReadWrite {
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

	static public String readStringFromFile(File file) {
		try {
			byte[] keyBytes = Files.readAllBytes(file.toPath());
			return new String(keyBytes, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
