package codeplus.popup;

import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

import codeplus.Activator;
import codeplus.util.EditorUtil;
import codeplus.util.TextUtil;
import codeplus.util.XmlUtil;
import codeplus.util.XmlUtil.MatchFile;
import codeplus.util.mapper.FindMapperXml;

public class GoSqlXmlActionDelegate extends ActionDelegate implements IEditorActionDelegate {

	private IEditorPart currentEditor;

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ISelection i = currentEditor.getEditorSite().getSelectionProvider().getSelection();
		if (i instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) i;
			int off = ts.getOffset();

			IFile file = (IFile) currentEditor.getEditorInput().getAdapter(IFile.class);
			String fileName = file.getName();
			if (fileName.matches(FindMapperXml.MATCH_REG)) {
				String content = EditorUtil.getText(currentEditor);
				String sqlId = TextUtil.getSelectedWord(content, off);

				String namespace = null;
				if (sqlId.contains(".")) {
					int index = sqlId.lastIndexOf(".");
					namespace = sqlId.substring(0, index);
					if (namespace.trim().length() == 0) {
						String packName = TextUtil.getJavaPackage(file);
						namespace = new StringBuilder(packName).append(".").append(fileName.substring(0, fileName.lastIndexOf("."))).toString();
					}
					sqlId = sqlId.substring(index + 1);
				} else {
					String packName = TextUtil.getJavaPackage(file);
					namespace = new StringBuilder(packName).append(".").append(fileName.substring(0, fileName.lastIndexOf("."))).toString();
				}

				IProject project = EditorUtil.getCurrentProject(currentEditor);
				IJavaProject javaProj = JavaCore.create(project);
				IFile f = null;
				try {
					f = FindMapperXml.findSqlXml(javaProj, namespace, sqlId);
				} catch (XPathExpressionException | InterruptedException e) {
					Activator.log(e);
				}
				if (f != null) {
					List<IFile> lst = Arrays.asList(f);

					MatchFile mf = XmlUtil.getMatchFileById(lst, sqlId);
					IFile fileToOpen = mf.getFile();
					EditorUtil.openFile(fileToOpen, mf.getMatchLineNo());
				}
			}
		}
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.currentEditor = targetEditor;
	}

}
