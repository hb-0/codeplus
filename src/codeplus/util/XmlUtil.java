package codeplus.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import codeplus.Activator;

public class XmlUtil {
	public static class MatchFile {
		private IFile file;
		private Integer matchLineNo;

		public IFile getFile() {
			return file;
		}

		public void setFile(IFile file) {
			this.file = file;
		}

		public Integer getMatchLineNo() {
			return matchLineNo;
		}

		public void setMatchLineNo(Integer matchLineNo) {
			this.matchLineNo = matchLineNo;
		}

	}

	public static MatchFile getMatchFileById(List<IFile> fileLst, String id) {
		for (IFile ifile : fileLst) {
			String charset = null;
			try {
				charset = ifile.getCharset();
			} catch (CoreException e1) {
				Activator.log(e1);
				return null;
			}
			String path = ifile.getLocation().toString();

			try (FileInputStream fis = new FileInputStream(new File(path));
					InputStreamReader idr = new InputStreamReader(fis, charset);
					BufferedReader br = new BufferedReader(idr);) {
				String s = null;
				int curLine = 0;
				while ((s = br.readLine()) != null) {
					curLine++;
					if (s.contains("\"" + id + "\"")) {
						MatchFile mf = new MatchFile();
						mf.setFile(ifile);
						mf.setMatchLineNo(curLine);
						return mf;
					}
				}
			} catch (IOException e) {
				Activator.log(e);
				return null;
			}
		}
		if (fileLst.size() > 0) {
			IFile f = fileLst.get(0);
			MatchFile mf = new MatchFile();
			mf.setFile(f);
			mf.setMatchLineNo(0);
			return mf;
		}
		return null;
	}

	public static IDOMDocument getMapperDocument(IFile mapperXmlFile) {
		if (mapperXmlFile == null)
			return null;
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(mapperXmlFile);
			IDOMModel domModel = (IDOMModel) model;
			IDOMDocument mapperDocument = domModel.getDocument();
			return mapperDocument;
		} catch (Exception e) {
			Activator.log(e);
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	public static void computeStatementText(ElementImpl currentNode, StringBuilder buffer, IJavaProject project) {
		if (currentNode == null) {
			return;
		}
		try {
			NodeList childNodes = currentNode.getChildNodes();
			for (int k = 0; k < childNodes.getLength(); k++) {
				Node childNode = childNodes.item(k);
				if ((childNode instanceof TextImpl)) {
					String text = ((TextImpl) childNode).getTextContent();
					buffer.append(text);
				} else {
					if (!(childNode instanceof ElementImpl))
						continue;
					ElementImpl element = (ElementImpl) childNode;
					String elemName = element.getNodeName();
					if (element.hasChildNodes()) {
						IStructuredDocumentRegion startRegion = element.getStartStructuredDocumentRegion();
						if (startRegion != null)
							buffer.append(startRegion.getText());
						computeStatementText(element, buffer, project);
						IStructuredDocumentRegion endRegion = element.getEndStructuredDocumentRegion();
						if (endRegion != null)
							buffer.append(endRegion.getText());
					} else if ("include".equals(elemName)) {
						ElementImpl sqlElement = resolveInclude(element, buffer, project);
						computeStatementText(sqlElement, buffer, project);
					} else {
						buffer.append(element.getSource());
					}
				}
			}
		} catch (Exception e) {
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(fos));
			String trace = fos.toString();
			buffer.append(trace);
		}
	}

	public static ElementImpl resolveInclude(ElementImpl includeElement, StringBuilder buffer, IJavaProject project) {
		String refId = includeElement.getAttribute("refid");
		if (refId.indexOf('$') > -1) {
			return null;
		}
		int lastDot = refId.lastIndexOf('.');
		try {
			if (lastDot == -1) {
				Document domDoc = includeElement.getOwnerDocument();
				ElementImpl ele = (ElementImpl) XpathUtil.xpathNode(domDoc, "//sql[@id='" + refId + "']");
				if (ele != null) {
					return ele;
				} else {
					String namespace = MapperNamespaceCache.getInstance().extractNamespace(domDoc);
					String sqlId = refId;

					List<IFile> lst = MapperNamespaceCache.getInstance().get(project, namespace, null);
					if (lst != null)
						for (IFile mapperFile : lst) {
							IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
							ele = (ElementImpl) XpathUtil.xpathNode(mapperDocument, "//sql[@id='" + sqlId + "']");
							if (ele != null)
								return ele;
						}
				}
			}
			if (lastDot + 1 < refId.length()) {
				String namespace = refId.substring(0, lastDot);
				String sqlId = refId.substring(lastDot + 1);
				List<IFile> lst = MapperNamespaceCache.getInstance().get(project, namespace, null);

				for (IFile mapperFile : lst) {
					IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
					ElementImpl ele = (ElementImpl) XpathUtil.xpathNode(mapperDocument, "//sql[@id='" + sqlId + "']");
					if (ele != null)
						return ele;
				}
			}
		} catch (XPathExpressionException e) {
			Activator.log(e);
		}
		return null;
	}

}
