package art.aukhatov.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.OptionalLong;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.out;

public class WebClient {

	private static final String HEADER_RANGE = "Range";
	private static final String RANGE_FORMAT = "bytes=%d-%d";
	private static final String HEADER_CONTENT_LENGTH = "content-length";
	private static final String HTTP_HEAD = "HEAD";
	private static final int DEFAULT_MAX_ATTEMPTS = 3;
	private static final int HTTP_PARTIAL_CONTENT = 206;

	private final HttpClient httpClient;
	private int maxAttempts;

	public WebClient() {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
	}

	public WebClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	private long contentLength(final String uri)
			throws URISyntaxException, IOException, InterruptedException {

		HttpRequest headRequest = HttpRequest
				.newBuilder(new URI(uri))
				.method(HTTP_HEAD, HttpRequest.BodyPublishers.noBody())
				.version(HttpClient.Version.HTTP_2)
				.build();

		HttpResponse<String> httpResponse = httpClient.send(headRequest, HttpResponse.BodyHandlers.ofString());

		OptionalLong contentLength = httpResponse
				.headers().firstValueAsLong(HEADER_CONTENT_LENGTH);

		return contentLength.orElse(0L);
	}

	public Response download(final String uri, int fromBytes, int toBytes)
			throws URISyntaxException, IOException, InterruptedException {

		HttpRequest request = HttpRequest
				.newBuilder(new URI(uri))
				.header(HEADER_RANGE, format(RANGE_FORMAT, fromBytes, toBytes))
				.GET()
				.version(HttpClient.Version.HTTP_2)
				.build();

		HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

		return new Response(new BufferedInputStream(response.body()), response.statusCode(), response.headers());
	}

	public Response download(final String uri)
			throws URISyntaxException, IOException, InterruptedException {

		HttpRequest request = HttpRequest
				.newBuilder(new URI(uri))
				.GET()
				.version(HttpClient.Version.HTTP_2)
				.build();

		HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

		return new Response(new BufferedInputStream(response.body()), response.statusCode(), response.headers());
	}

	/**
	 * Downloading a file partially by URI.
	 * This API uses HTTP Header Range by RFC 2616.
	 * See https://tools.ietf.org/html/rfc2616#section-14.35
	 *
	 * @param uri       The string to be parsed into a URI
	 * @param chunkSize The size of chunk in bytes to download a file partially
	 * @return a byte array containing the bytes downloaded from uri
	 * @throws InterruptedException if the operation is interrupted
	 * @throws IOException          if an I/O error occurs when sending or receiving
	 * @throws URISyntaxException   if the uri is invalid
	 */
	public byte[] download(final String uri, int chunkSize)
			throws URISyntaxException, IOException, InterruptedException {

		final int expectedLength = (int) contentLength(uri);
		int start = 0;
		int offset = chunkSize - 1;

		byte[] downloadedBytes = new byte[expectedLength];
		int downloadedLength = 0;

		int attempts = 1;

		while (downloadedLength < expectedLength && attempts < maxAttempts) {

			Response response;

			try {
				response = download(uri, start, offset);
			} catch (IOException e) {
				attempts++;
				err.println(format("I/O error has occurred. %s", e));
				out.println(format("Going to do %d attempt", attempts));
				continue;
			}

			try (response.inputStream) {
				byte[] chunkedBytes = response.inputStream.readAllBytes();

				downloadedLength += chunkedBytes.length;

				if (isPartial(response)) {
					System.arraycopy(chunkedBytes, 0, downloadedBytes, start, chunkedBytes.length);
					start = offset + 1;
					offset = Math.min(offset + chunkSize, expectedLength - 1);
				}
			} catch (IOException e) {
				attempts++;
				err.println(format("I/O error has occurred. %s", e));
				out.println(format("Going to do %d attempt", attempts));
				continue;
			}

			attempts = 1; // reset attempts counter
		}

		if (attempts >= maxAttempts) {
			err.println("A file could not be downloaded. Number of attempts are exceeded.");
		}

		return downloadedBytes;
	}

	private boolean isPartial(Response response) {
		return response.status == HTTP_PARTIAL_CONTENT;
	}

	public int maxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public static class Response {
		final BufferedInputStream inputStream;
		final int status;
		final HttpHeaders headers;

		public Response(BufferedInputStream inputStream, int status, HttpHeaders headers) {
			this.inputStream = inputStream;
			this.status = status;
			this.headers = headers;
		}
	}
}
