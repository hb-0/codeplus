package codeplus.popup;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import codeplus.Activator;
import codeplus.util.EditorUtil;
import codeplus.util.MapperNamespaceCache;
import codeplus.util.TextUtil;
import codeplus.util.XmlUtil;
import codeplus.util.XmlUtil.MatchFile;

public class GoSqlXmlActionDelegate extends ActionDelegate implements IEditorActionDelegate {

	private IEditorPart currentEditor;

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		//		box.setMessage("Executing: " + getClass());
		//		box.open();

		ISelection i = currentEditor.getEditorSite().getSelectionProvider().getSelection();
		if (i instanceof ITextSelection) {
			ITextSelection ts = (ITextSelection) i;
			int off = ts.getOffset();

			IFile file = currentEditor.getEditorInput().getAdapter(IFile.class);
			String fileName = file.getName();
			if (fileName.matches(".*(?i)(Dao|Mapper)\\.java")) {
				String content = EditorUtil.getText(currentEditor);
				String sqlId = TextUtil.getSelectedWord(content, off);
				String packName = TextUtil.getJavaPackage(file);
				String namespace = new StringBuilder(packName).append(".").append(fileName.substring(0, fileName.lastIndexOf("."))).toString();

				IProject project = EditorUtil.getCurrentProject(currentEditor);
				IJavaProject javaProj = JavaCore.create(project);
				List<IFile> lst = MapperNamespaceCache.getInstance().get(javaProj, namespace, null);

				if (lst != null) {
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
