package ch.elexis.core.mail.ui.preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerializableFileUtil {

	public static byte[] serializeData(ArrayList<SerializableFile> serial) {
		byte[] serializedData = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(baos)) {
			out.writeObject(serial);
			serializedData = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serializedData;
	}

	@SuppressWarnings("unchecked")
	public static List<SerializableFile> deserializeData(byte[] data) {
		List<SerializableFile> fileList = new ArrayList<>();
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(bais)) {
			fileList = (List<SerializableFile>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return fileList;
	}
}
