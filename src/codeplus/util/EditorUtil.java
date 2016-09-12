package codeplus.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import codeplus.Activator;

public class EditorUtil {

	public static String getText(IEditorPart je) {
		IFile file = je.getEditorInput().getAdapter(IFile.class);
		String charset = null;
		try {
			charset = file.getCharset();
		} catch (CoreException e1) {
			Activator.log(e1);
			return null;
		}
		String path = file.getLocation().toString();

		try {
			return IOUtil.getText(new File(path), charset);
		} catch (IOException e) {
			Activator.log(e);
			return null;
		}
	}

	public static String getFileName(IEditorPart je) {
		IFile file = je.getEditorInput().getAdapter(IFile.class);
		return file.getName();
	}

	public static IProject getCurrentProject(IEditorPart part) {
		if (part != null) {
			IFile object = (IFile) part.getEditorInput().getAdapter(IFile.class);
			if (object != null) {
				IProject project = object.getProject();
				return project;
			}
		}
		return null;
	}

	public static ITextEditor getTextEditor(IEditorPart editor) {
		ITextEditor textEditor = null;
		if (editor instanceof ITextEditor)
			textEditor = (ITextEditor) editor;
		if ((textEditor == null) && (editor != null))
			textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
		return textEditor;
	}

	public static void openFile(IFile fileToOpen, int line) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, fileToOpen);

			IEditorPart editorPart = page.getActiveEditor();
			ITextEditor editor = EditorUtil.getTextEditor(editorPart);
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());

			int start = document.getLineOffset(line);
			editor.selectAndReveal(start, 0);
		} catch (PartInitException e) {
			Activator.log(e);
		} catch (BadLocationException e) {
			Activator.log(e);
		}
	}

}
