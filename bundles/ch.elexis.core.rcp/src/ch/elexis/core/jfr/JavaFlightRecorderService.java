package ch.elexis.core.jfr;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Duration;

import org.osgi.service.component.annotations.Component;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;

@Component(immediate = true, service = JavaFlightRecorderService.class)
public class JavaFlightRecorderService {

	private Recording recording;

	public void startRecording() throws IOException, ParseException {

		if (recording == null) {
			recording = new Recording();
		}

		if (recording != null) {
			Configuration conf = Configuration.getConfiguration("default");
			recording = new Recording(conf);
			recording.setMaxAge(Duration.ofMinutes(2));
			recording.setToDisk(false);

			System.gc();
			recording.start();
		}
	}

	public void stopRecording() {
		if (recording != null) {
			recording.stop();
			recording = null;
		}
	}

	public void dumpFile(Path filename) throws IOException {
		if (recording != null && filename != null) {
			recording.dump(filename);
		}
	}

	public boolean isRecording() {
		return recording != null;
	}

}
