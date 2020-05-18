package art.aukhatov.http;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebClientTest {

	@Test
	void downloadByChunk() throws IOException, URISyntaxException, InterruptedException {
		WebClient fd = new WebClient();
		byte[] data = fd.download("http://www.africau.edu/images/default/sample.pdf", 1024);
		final String downloadedFilePath = System.getProperty("java.io.tmpdir") + File.separator + "sample.pdf";
		Path path = Paths.get(downloadedFilePath);
		OutputStream outputStream = Files.newOutputStream(path);
		outputStream.write(data);
		outputStream.flush();
		outputStream.close();

		assertEquals(3028, Files.readAllBytes(Paths.get(downloadedFilePath)).length);
	}
}