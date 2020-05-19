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
		byte[] data = fd.download("https://file-examples.com/wp-content/uploads/2017/10/file-example_PDF_1MB.pdf", 262_144);
		final String downloadedFilePath = System.getProperty("java.io.tmpdir") + "sample.pdf";
		System.out.println("File has downloaded to " + downloadedFilePath);
		Path path = Paths.get(downloadedFilePath);
		OutputStream outputStream = Files.newOutputStream(path);
		outputStream.write(data);
		outputStream.flush();
		outputStream.close();

		assertEquals(1_042_157, Files.readAllBytes(Paths.get(downloadedFilePath)).length);

		Files.delete(path);
	}
}