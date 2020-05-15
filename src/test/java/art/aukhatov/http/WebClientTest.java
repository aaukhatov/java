package art.aukhatov.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

class WebClientTest {

	@Test
	void downloadByChunk() throws IOException, URISyntaxException, InterruptedException {
		WebClient fd = new WebClient();
		byte[] data = fd.download("http://www.africau.edu/images/default/sample.pdf", 1024);
		final OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/sample3.pdf"));
		outputStream.write(data);
		outputStream.flush();
		outputStream.close();
	}
}