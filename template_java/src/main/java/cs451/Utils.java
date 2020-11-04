package cs451;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Utils {
	// stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
	public static byte[] getBytesArrayFromMessageObject(Message msg) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		byte[] msgBytes = null;
		
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(msg);
			out.flush();
			msgBytes = bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
			}
		}
		
		return msgBytes;
	}
}
