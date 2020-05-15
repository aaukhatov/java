package art.aukhatov.http;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class WebClientTest {

	@Test
	void downloadByChunk() throws IOException, URISyntaxException, InterruptedException {
		WebClient fd = new WebClient();
		byte[] data = fd.download("http://www.africau.edu/images/default/sample.pdf", 1024);
		Path path = Paths.get(System.getProperty("java.io.tmpdir") + File.separator + "sample.pdf");
		OutputStream outputStream = Files.newOutputStream(path);
		outputStream.write(data);
		outputStream.flush();
		outputStream.close();
	}
}