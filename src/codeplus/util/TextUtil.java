package codeplus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import codeplus.Activator;

public class TextUtil {

	private static String breakWordReg = "(\"|<|>|\\(|\\))";

	public static String getSelectedWord(String wholeText, int offset) {
		if (offset >= wholeText.length() || offset < 0) {
			return null;
		}
		String s = wholeText.substring(offset, offset + 1);
		if (s.matches("\\s") || s.matches(breakWordReg)) {
			return null;
		}
		StringBuilder sb = new StringBuilder(s);
		traverse(wholeText, sb, offset, 1, breakWordReg, "\\s");
		traverse(wholeText, sb, offset, -1, breakWordReg, "\\s");
		return sb.toString();
	}

		public static String getSelectedMethod(String wholeText, int offset) {
			if (offset >= wholeText.length() || offset < 0) {
				return null;
			}
			String s = wholeText.substring(offset, offset + 1);
			if (s.matches("\\s") ) {
				return null;
			}
			StringBuilder sb = new StringBuilder(s);
			traverse(wholeText, sb, offset, 1,"\\)");
			traverse(wholeText, sb,  offset, -1,breakWordReg, "\\s");
			return sb.toString();
		}

	private static void traverse(String wholeText, StringBuilder sb, int start, int flag, String breadWordOne, String... breakWordMore) {
		if (flag > 0) {
			if (start + 1 >= wholeText.length()) {
				return;
			}
			char c = wholeText.charAt(start + 1);
			String s = new String(new char[] { c });
			if (s.matches(breadWordOne)) {
				return;
			}
			for (String bw : breakWordMore) {
				if (s.matches(bw)) {
					return;
				}
			}
			sb.append(wholeText.charAt(start + 1));
			traverse(wholeText, sb, start + 1, flag, breadWordOne, breakWordMore);
		} else {
			if (start - 1 < 0) {
				return;
			}
			char c = wholeText.charAt(start - 1);
			String s = new String(new char[] { c });
			if (s.matches(breadWordOne)) {
				return;
			}
			for (String bw : breakWordMore) {
				if (s.matches(bw)) {
					return;
				}
			}
			sb.insert(0, wholeText.charAt(start - 1));
			traverse(wholeText, sb, start - 1, flag, breadWordOne, breakWordMore);
		}
	}

	public static String getJavaPackage(IFile file) {
		String path = file.getLocation().toString();
		String charset = null;
		try {
			charset = file.getCharset();
		} catch (CoreException e1) {
			Activator.log(e1);
			return null;
		}

		try (FileInputStream fis = new FileInputStream(new File(path));
				InputStreamReader idr = new InputStreamReader(fis, charset);
				BufferedReader br = new BufferedReader(idr);) {
			String s = null;
			while ((s = br.readLine()) != null) {
				if (s.contains("package ")) {
					s = s.substring(0, s.indexOf(";"));
					s = s.replace("package ", "");
					s = s.trim();
					return s;
				}
			}
		} catch (IOException e) {
			Activator.log(e);
			return null;
		}
		return null;
	}

	public static String htmlEncode(String source) {
		if (source == null) {
			return "";
		}
		String html = "";
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			switch (c) {
			case '<':
				buffer.append("&lt;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '&':
				buffer.append("&amp;");
				break;
			case '"':
				buffer.append("&quot;");
				break;
			//            case 10:
			//            case 13:
			//                break;
			default:
				buffer.append(c);
			}
		}
		html = buffer.toString();
		return html;
	}

	public static String convertToHTMLContentWithWhitespace(String source) {
		String html = htmlEncode(source);
		return "<span style='white-space:pre'>" + html + "</span>";
	}

}
