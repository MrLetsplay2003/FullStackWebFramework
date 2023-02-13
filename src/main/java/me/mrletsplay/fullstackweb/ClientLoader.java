package me.mrletsplay.fullstackweb;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import me.mrletsplay.simplehttpserver.http.document.HttpDocumentProvider;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;

public class ClientLoader {

	public static final String DEFAULT_WAR_PATH = "/client.zip";
	public static final Map<String, String> FILE_TYPES;

	static {
		Map<String, String> fileTypes = new HashMap<>();
		fileTypes.put(".html", "text/html");
		fileTypes.put(".js", "application/javascript");
		FILE_TYPES = Collections.unmodifiableMap(fileTypes);
	}

	public static void serveDefaultClient(HttpDocumentProvider documentProvider) {
		serveClient(documentProvider, DEFAULT_WAR_PATH);
	}

	public static void serveClient(HttpDocumentProvider documentProvider, String warPath) {
		System.out.println("Loading client from " + warPath + "...");
		try {
			ZipInputStream zip = new ZipInputStream(ClientLoader.class.getResourceAsStream(warPath));
			ZipEntry entry;
			while((entry = zip.getNextEntry()) != null) {
				System.out.println(entry.getName());
				String entryName = entry.getName();
				String path = entryName;
				if(entry.getName().equals("index.html")) path = "";
				byte[] bytes = zip.readNBytes((int) entry.getSize());
				documentProvider.registerDocument("/" + path, () -> {
					HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
					ctx.getServerHeader().setContent(getMimeType(entryName), bytes);
				});
			}
			System.out.println("Client loaded!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getMimeType(String fileName) {
		return FILE_TYPES.entrySet().stream()
			.filter(en -> fileName.endsWith(en.getKey()))
			.map(en -> en.getValue())
			.findFirst().orElse("application/unknown");
	}

}
