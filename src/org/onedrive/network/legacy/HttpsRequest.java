package org.onedrive.network.legacy;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class HttpsRequest {
	public static final String NETWORK_ERR_MSG = "Network connection error. Please retry later or contact API author.";
	protected final HttpsURLConnection httpConnection;

	public HttpsRequest(@NotNull String url) throws MalformedURLException {
		this(new URL(url));
	}

	public HttpsRequest(@NotNull URL url) {
		try {
			httpConnection = (HttpsURLConnection) url.openConnection();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	/**
	 * Add {@code key}, {@code value} pair to http request's header.<br>
	 * Like: {@code key}: {@code value}.
	 *
	 * @param key   Key to add in request's header.
	 * @param value Value to add in request's header. It could be {@code null}.
	 */
	public void setHeader(@NotNull String key, String value) {
		httpConnection.setRequestProperty(key, value);
	}

	@NotNull
	public HttpsResponse doPost(String content) {
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

		return doPost(bytes);
	}

	@NotNull
	public HttpsResponse doPost(byte[] content) {
		try {
			httpConnection.setRequestMethod("POST");
			return sendContent(content);
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException("Can not use \"POST\" method.");
		}
	}

	@NotNull
	public HttpsResponse doPatch(String content) {
		byte[] bytes = content.getBytes();
		return doPatch(bytes);
	}

	@NotNull
	public HttpsResponse doPatch(byte[] content) {
		try {
			httpConnection.setRequestMethod("PATCH");
			return sendContent(content);
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException("Can not use \"PATCH\" method.");
		}
	}

	public HttpsResponse sendContent(byte[] content) {
		try {
			httpConnection.setDoOutput(true);

			httpConnection.setFixedLengthStreamingMode(content.length);

			OutputStream out = httpConnection.getOutputStream();
			out.write(content);
			out.flush();
			out.close();

			return makeResponse();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public HttpsResponse doDelete() {
		try {
			httpConnection.setRequestMethod("DELETE");
			return makeResponse();
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	@NotNull
	public HttpsResponse doGet() {
		try {
			httpConnection.setRequestMethod("GET");
			return makeResponse();
		}
		catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
	}

	/**
	 * {@// TODO: handling NOT 200 OK response.}
	 *
	 * @return Response object.
	 * @throws RuntimeException fail to network connection or fail to read response.
	 */
	@NotNull
	protected HttpsResponse makeResponse() {
		try {
			int code = httpConnection.getResponseCode();
			String message = httpConnection.getResponseMessage();
			Map<String, List<String>> header = httpConnection.getHeaderFields();
			URL url = httpConnection.getURL();

			val byteStream = new ByteArrayOutputStream();
			BufferedInputStream body;

			if (code < 400) {
				body = new BufferedInputStream(httpConnection.getInputStream());
			}
			else {
				// TODO: should be tested about not 4XX response code.
				body = new BufferedInputStream(httpConnection.getErrorStream());
				// TODO: for debug
				// throw new RuntimeException("4XX response received.");
			}

			int bytes;
			while ((bytes = body.read()) != -1) {
				byteStream.write(bytes);
			}

			byteStream.close();
			body.close();
			return new HttpsResponse(url, code, message, header, byteStream.toByteArray());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(NETWORK_ERR_MSG);
		}
		finally {
			httpConnection.disconnect();
		}
	}
}