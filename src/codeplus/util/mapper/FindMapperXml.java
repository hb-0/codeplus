package codeplus.util.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.w3c.dom.Node;

import codeplus.util.SimilarityUtil;
import codeplus.util.XmlUtil;
import codeplus.util.XpathUtil;
import codeplus.util.cache.Cache;
import codeplus.util.cache.Computable;

public class FindMapperXml {
	
	public static final String MATCH_REG = ".*(?i)(dao(impl)?|mapper)\\.java";

	private static final Map<IJavaProject, Cache<String, String>> simCache = new HashMap<>();

	private static synchronized Cache<String, String> getSimCache(final IJavaProject javaProj) {
		Cache<String, String> map = simCache.get(javaProj);
		if (map == null) {
			map = new Cache<String, String>(new Computable<String, String>() {
				@Override
				public String compute(String key) throws InterruptedException {
					Map<String, List<IFile>> map = MapperNamespaceCache.getInstance().getCacheMap(javaProj, null);
					Set<String> ns = map.keySet();

					// 包含大于相似
					int len = 0;
					String maxContains = null;
					for (String s : ns) {
						String simpleName = s.substring(s.lastIndexOf(".") + 1);
						if (key.contains(simpleName)) {
							if (simpleName.length() > len) {
								len = simpleName.length();
								maxContains = s;
							}
						}
					}

					// 没有包含再按相似度匹配
					if (maxContains == null) {
						double max = 0;
						String simNamespace = null;
						for (String s : ns) {
							double sim = SimilarityUtil.sim(key, s);
							if (sim > max) {
								max = sim;
								simNamespace = s;
							}
						}
						return simNamespace;
					}
					return maxContains;
				}
			});
			simCache.put(javaProj, map);
		}
		return map;
	}

	private static String getSimNamespace(IJavaProject javaProj, String namespace) throws InterruptedException {
		String simNamespace = getSimCache(javaProj).compute(namespace);
		return simNamespace;
	}

	public static String findSql(IJavaProject javaProj, String namespace, String sqlId)
			throws XPathExpressionException, InterruptedException {
		List<IFile> lst = MapperNamespaceCache.getInstance().get(javaProj, namespace, null);
		if (lst != null) {
			for (IFile mapperFile : lst) {
				IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
				Node node = XpathUtil.xpathNode(mapperDocument, "//*[@id='" + sqlId + "']");
				if (node != null) {
					StringBuilder buffer = new StringBuilder();
					XmlUtil.computeStatementText((ElementImpl) node, buffer, javaProj);

					String s = buffer.toString();
					return s;
				}
			}
		}

		// 采用相似度查找
		String simNamespace = getSimNamespace(javaProj, namespace);
		lst = MapperNamespaceCache.getInstance().get(javaProj, simNamespace, null);
		if (lst != null) {
			for (IFile mapperFile : lst) {
				IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
				Node node = XpathUtil.xpathNode(mapperDocument, "//*[@id='" + sqlId + "']");
				if (node != null) {
					StringBuilder buffer = new StringBuilder();
					XmlUtil.computeStatementText((ElementImpl) node, buffer, javaProj);

					String s = buffer.toString();
					return s;
				}
			}
		}

		return null;
	}

	public static IFile findSqlXml(IJavaProject javaProj, String namespace, String sqlId)
			throws XPathExpressionException, InterruptedException {
		List<IFile> lst = MapperNamespaceCache.getInstance().get(javaProj, namespace, null);
		if (lst != null) {
			for (IFile mapperFile : lst) {
				IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
				Node node = XpathUtil.xpathNode(mapperDocument, "//*[@id='" + sqlId + "']");
				if (node != null) {
					return mapperFile;
				}
			}
		}

		// 采用相似度查找
		String simNamespace = getSimNamespace(javaProj, namespace);
		lst = MapperNamespaceCache.getInstance().get(javaProj, simNamespace, null);
		if (lst != null) {
			for (IFile mapperFile : lst) {
				IDOMDocument mapperDocument = XmlUtil.getMapperDocument(mapperFile);
				Node node = XpathUtil.xpathNode(mapperDocument, "//*[@id='" + sqlId + "']");
				if (node != null) {
					return mapperFile;
				}
			}
		}
		return null;
	}

}
