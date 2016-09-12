package codeplus.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XpathUtil {
	public static boolean xpathBool(Node node, String expression) throws XPathExpressionException {
		return ((Boolean) evaluateXpath(expression, node, XPathConstants.BOOLEAN, null)).booleanValue();
	}

	public static String xpathString(Node node, String expression) throws XPathExpressionException {
		return ((String) evaluateXpath(expression, node, XPathConstants.STRING, null));
	}

	public static Node xpathNode(Node node, String expression) throws XPathExpressionException {
		return ((Node) evaluateXpath(expression, node, XPathConstants.NODE, null));
	}

	public static NodeList xpathNodes(Node node, String expression) throws XPathExpressionException {
		return xpathNodes(node, expression, null);
	}

	public static NodeList xpathNodes(Node node, String expression, NamespaceContext nsContext) throws XPathExpressionException {
		return ((NodeList) evaluateXpath(expression, node, XPathConstants.NODESET, nsContext));
	}

	public static Object evaluateXpath(String expression, Object node, QName returnType, NamespaceContext nsContext) throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		if (nsContext != null) {
			xpath.setNamespaceContext(nsContext);
		}
		return xpath.evaluate(expression, node, returnType);
	}
}
