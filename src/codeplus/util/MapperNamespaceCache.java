package codeplus.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import codeplus.Activator;

//import net.harawata.mybatipse.Activator;
//import net.harawata.mybatipse.util.XpathUtil;

public class MapperNamespaceCache {
	private static final MapperNamespaceCache INSTANCE = new MapperNamespaceCache();

	private IContentType mapperContentType = Platform.getContentTypeManager().getContentType("net.harawata.mybatipse.mapper");

	private final Map<String, Map<String, List<IFile>>> cache = new ConcurrentHashMap<String, Map<String, List<IFile>>>();

	public List<IFile> get(IJavaProject javaProject, String namespace, IReporter reporter) {
		Map<String, List<IFile>> map = getCacheMap(javaProject, reporter);
		return map.get(namespace);
	}

	public void clear() {
		this.cache.clear();
	}

	public void remove(IProject project) {
		this.cache.remove(project.getName());
	}

	public void remove(String projectName, IFile file) {
		Map<String, List<IFile>> map = this.cache.get(projectName);
		if (map == null)
			return;
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, List<IFile>> entry =  (Entry<String, List<IFile>>) iterator.next();
			List<IFile> lst=entry.getValue();
			if(lst.contains(file)){
				lst.remove(file);
			}
			if(lst.size()==0){
				iterator.remove();
			}
		}
	}

	public void put(String projectName, IFile file) {
		remove(projectName, file);

		Map<String, List<IFile>> map =this.cache.get(projectName);
		if (map == null) {
			return;
		}
		String namespace = extractNamespace(file);
		if (namespace != null) {
			List<IFile> lst=map.get(namespace);
			if(lst==null){
				lst=new ArrayList<IFile>();
			}
			lst.add(file);
			map.put(namespace, lst);
		}
	}

	public Map<String, List<IFile>> getCacheMap(IJavaProject javaProject, IReporter reporter) {
		String projectName = javaProject.getElementName();
		Map<String, List<IFile>> map = this.cache.get(projectName);
		if (map == null) {
			map = new ConcurrentHashMap<String, List<IFile>>();
			this.cache.put(projectName, map);
			collectMappers(javaProject, map, reporter);
		}
		return map;
	}

	private void collectMappers(IJavaProject project, final Map<String, List<IFile>> map, IReporter reporter) {
		try {
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
				if (root.getKind() != 1) {
					continue;
				}

				root.getResource().accept(new IResourceProxyVisitor() {
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if ((!proxy.isDerived()) && (proxy.getType() == 1) && (proxy.getName().endsWith(".xml"))) {
							IFile file = (IFile) proxy.requestResource();
							IContentDescription contentDesc = file.getContentDescription();
							if (contentDesc != null) {
								IContentType contentType = contentDesc.getContentType();
//								if ((contentType != null) && (contentType.isKindOf(MapperNamespaceCache.this.mapperContentType))) {
									String namespace = MapperNamespaceCache.this.extractNamespace(file);
									if (namespace != null) {
										List<IFile> lst=map.get(namespace);
										if(lst==null){
											lst=new ArrayList<IFile>();
										}
										lst.add(file);
										map.put(namespace, lst);
									}
									return false;
								}
							}
//						}
						return true;
					}
				}, 0);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private String extractNamespace(IFile file) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(file);
			IDOMModel domModel = (IDOMModel) model;
			IDOMDocument domDoc = domModel.getDocument();

			Node node = XpathUtil.xpathNode(domDoc, "//mapper/@namespace");
			String str = node == null ? null : node.getNodeValue();
			return str;
		} catch (Exception e) {
//			Activator.log(4, "Error occurred during parsing mapper:" + file.getFullPath(), e);
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}
	
	public String extractNamespace(Document domDoc) {
		try {
			Node node = XpathUtil.xpathNode(domDoc, "//mapper/@namespace");
			String str = node == null ? null : node.getNodeValue();
			return str;
		} catch (Exception e) {
//			Activator.log(4, "Error occurred during parsing mapper:" + domDoc.getBaseURI(), e);
		} finally {
		}
		return null;
	}

	public static MapperNamespaceCache getInstance() {
		return INSTANCE;
	}
}