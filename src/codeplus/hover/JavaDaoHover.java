package codeplus.hover;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import codeplus.Activator;
import codeplus.util.EditorUtil;
import codeplus.util.TextUtil;
import codeplus.util.XmlUtil;
import codeplus.util.XmlUtil.MatchFile;
import codeplus.util.mapper.FindMapperXml;

public class JavaDaoHover implements IJavaEditorTextHover, ITextHoverExtension, ITextHoverExtension2 {
	private IEditorPart currentEditor;
	private ITextViewer currentViewer;
	private IRegion currentRegion;

	private String[] getNamespaceAndSqlid() throws BadLocationException {
		String hoverStr = currentViewer.getDocument().get(currentRegion.getOffset(), currentRegion.getLength());

		String content = EditorUtil.getText(currentEditor);
		String selected = TextUtil.getSelectedWord(content, currentRegion.getOffset());
		String sqlId = null;
		String wholeSqlId = null;
		if (selected != null && selected.contains(hoverStr) && selected.contains(".")) {
			wholeSqlId = selected;
		}

		IFile file = currentEditor.getEditorInput().getAdapter(IFile.class);
		String fileName = file.getName();
		if (fileName.matches(FindMapperXml.MATCH_REG)) {
			String namespace = null;
			if (wholeSqlId != null) {
				int index = wholeSqlId.lastIndexOf(".");
				namespace = wholeSqlId.substring(0, index);
				if (namespace.trim().length() == 0) {
					String packName = TextUtil.getJavaPackage(file);
					namespace = new StringBuilder(packName).append(".").append(fileName.substring(0, fileName.lastIndexOf("."))).toString();
				}
				sqlId = wholeSqlId.substring(index + 1);
				return new String[] { namespace, sqlId };
			} else {
				String packName = TextUtil.getJavaPackage(file);
				namespace = new StringBuilder(packName).append(".").append(fileName.substring(0, fileName.lastIndexOf("."))).toString();
				sqlId = hoverStr;
				return new String[] { namespace, sqlId };
			}
		}
		return null;
	}

	@Override
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		this.currentViewer = viewer;
		this.currentRegion = region;

		try {
			IProject project = EditorUtil.getCurrentProject(currentEditor);
			IJavaProject javaProj = JavaCore.create(project);

			String[] arr = getNamespaceAndSqlid();
			if (arr != null) {
				String namespace = arr[0];
				String sqlId = arr[1];
				String s = FindMapperXml.findSql(javaProj, namespace, sqlId);
				if (s != null) {
					StringBuilder buffer = new StringBuilder();
					buffer.append("|&#x3000;&#x3000;Tips: button1 open sql xml file; button2 copy sql to clipboard.    <br>");
					buffer.append(
							"|-----------------------------------------------------------------------------------------------------------------------<br>");

					s = TextUtil.htmlEncode(s);
					s = s.replaceAll("\t", "&#x3000;&#x3000;");

					// s = s.replaceAll("\n", "<br>");
					for (String str : s.split("\n")) {
						buffer.append("|&#x3000;&#x3000;").append(str).append("<br>");
					}
					s = buffer.toString();
					return s;
				}
			}
			return null;
		} catch (BadLocationException e) {
			Activator.log(e);
		} catch (XPathExpressionException e) {
			Activator.log(e);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public Object getHoverInfo2(ITextViewer viewer, IRegion region) {
		return getHoverInfo(viewer, region);
	}

	private class MyBatisInformationControlCreator implements IInformationControlCreator {
		@Override
		public IInformationControl createInformationControl(Shell parent) {
			ToolBarManager tbm = new ToolBarManager(SWT.FLAT);

			final IInformationControlCreator me = this;
			IInformationControl iControl = new DefaultInformationControl(parent, tbm) {
				public IInformationControlCreator getInformationPresenterControlCreator() {
					return me;
				}
			};

			IAction action1 = new Action() {
				public void run() {
					try {
						IFile file = currentEditor.getEditorInput().getAdapter(IFile.class);
						String fileName = file.getName();
						if (fileName.matches(FindMapperXml.MATCH_REG)) {
							String[] arr = getNamespaceAndSqlid();
							if (arr != null) {
								String namespace = arr[0];
								String sqlId = arr[1];

								IProject project = EditorUtil.getCurrentProject(currentEditor);
								IJavaProject javaProj = JavaCore.create(project);
								IFile f = FindMapperXml.findSqlXml(javaProj, namespace, sqlId);
								if (f != null) {
									List<IFile> lst = Arrays.asList(f);

									MatchFile mf = XmlUtil.getMatchFileById(lst, sqlId);
									IFile fileToOpen = mf.getFile();
									EditorUtil.openFile(fileToOpen, mf.getMatchLineNo());
								}
							}
						}
					} catch (BadLocationException e) {
						Activator.log(e);
					} catch (XPathExpressionException e) {
						Activator.log(e);
					} catch (InterruptedException e) {
						Activator.log(e);
					}
				}
			};
			action1.setText("go");
			action1.setToolTipText("open sql xml");
			action1.setImageDescriptor(Activator.getImageDescriptor("icons/goto_input.png"));
			tbm.add(action1);

			IAction action2 = new Action() {
				public void run() {
					try {
						IFile file = currentEditor.getEditorInput().getAdapter(IFile.class);
						String fileName = file.getName();
						if (fileName.matches(FindMapperXml.MATCH_REG)) {
							String[] arr = getNamespaceAndSqlid();
							if (arr != null) {
								String namespace = arr[0];
								String sqlId = arr[1];

								IProject project = EditorUtil.getCurrentProject(currentEditor);
								IJavaProject javaProj = JavaCore.create(project);
								String s = FindMapperXml.findSql(javaProj, namespace, sqlId);
								if (s != null) {
									Toolkit toolkit = Toolkit.getDefaultToolkit();
									java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
									StringSelection stringSel = new StringSelection(s);
									clipboard.setContents(stringSel, null);
									return;
								}
							}
						}
					} catch (BadLocationException e) {
						Activator.log(e);
					} catch (XPathExpressionException e) {
						Activator.log(e);
					} catch (InterruptedException e) {
						Activator.log(e);
					}
				}
			};
			action2.setText("copy");
			action2.setToolTipText("copy sql to clipboard");
			action2.setImageDescriptor(Activator.getImageDescriptor("icons/copy.png"));
			tbm.add(action2);

			tbm.update(true);

			return iControl;
		}

	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		MyBatisInformationControlCreator c = new MyBatisInformationControlCreator();
		return c;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, final int offset) {
		return null;
	}

	@Override
	public void setEditor(IEditorPart targetEditor) {
		this.currentEditor = targetEditor;
	}

}