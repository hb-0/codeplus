package codeplus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class IOUtil {
	public static String getText(File file,String charset) throws IOException {
		try (FileInputStream fis = new FileInputStream(file);
				InputStreamReader idr = new InputStreamReader(fis, charset);
				StringWriter sw = new StringWriter();) {
			int c = 0;
			while ((c = idr.read()) != -1) {
				sw.append((char) c);
			}
			return sw.toString();
		} catch (IOException e) {
			throw e;
		}
	}
}
