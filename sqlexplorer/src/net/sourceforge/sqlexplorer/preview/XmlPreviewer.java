/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.preview;

import java.util.Properties;
import java.util.Map.Entry;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;
import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.dataset.XmlDataType;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Previewer for XmlDataType objects
 * 
 * @author John Spackman
 */
public class XmlPreviewer implements Previewer {

	public void createControls(Composite parent, final Object data) throws ExplorerException {
		IXMLElement rootElem = getXml(data);
		if (rootElem == null)
			return;
		final Object[] root = new Object[] { rootElem };

		TreeViewer tree = new TreeViewer(parent, SWT.SINGLE);
		tree.setContentProvider(new ITreeContentProvider() {
			public void dispose() {
			}

			/**
			 * Called to get the top level items
			 */
			public Object[] getChildren(Object parentElement) {
				return root;
			}

			/**
			 * Called to get the item's children
			 */
			public Object[] getElements(Object inputElement) {
				IXMLElement elem = (IXMLElement)inputElement;
				return elem.getChildren().toArray();
			}

			public boolean hasChildren(Object element) {
				IXMLElement elem = (IXMLElement)element;
				return elem.hasChildren();
			}

			public Object getParent(Object element) {
				IXMLElement elem = (IXMLElement)element;
				return elem.getParent();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Nothing
			}
		});
		
		tree.setLabelProvider(new LabelProvider() {
			public String getText(Object obj) {
				IXMLElement elem = (IXMLElement)obj;
				StringBuffer result = new StringBuffer();
				result.append('<');
				result.append(elem.getName());
				Properties attrs = elem.getAttributes();
				for (Entry<Object, Object> entry : attrs.entrySet()) {
					String name = (String)entry.getKey();
					String value = (String)entry.getValue();
					result.append(' ').append(name).append('=').append('\"').append(value).append('\"');
				}
				if (!elem.hasChildren())
					result.append('/');
				result.append('>');
				return result.toString();
			}
		});
		
		tree.expandToLevel(1);
	}

	public void dispose() {
	}

	private IXMLElement getXml(Object data) throws ExplorerException {
		try {
			if (data == null)
				return null;
			
			if (data instanceof XmlDataType)
				return ((XmlDataType)data).getRootElement();
	
			String text = data.toString();
			if (text == null)
				return null;

			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader = StdXMLReader.stringReader(text);
			parser.setReader(reader);
			return (IXMLElement) parser.parse();
		}catch(XMLException e) {
			throw new ExplorerException(e);
		}catch(InstantiationException e) {
			throw new ExplorerException(e);
		}catch(IllegalAccessException e) {
			throw new ExplorerException(e);
		}catch(ClassNotFoundException e) {
			throw new ExplorerException(e);
		}
	}
	
	/**
	 * Registers this previewer type with the factory
	 */
	public static void register() {
		PreviewerFactory.getInstance().registerClass(new PreviewerFactory.Resolver() {

			/* (non-JavaDoc)
			 * @see net.sourceforge.sqlexplorer.preview.PreviewerFactory.Resolver#canAcceptMimeType(java.lang.String)
			 */
			public boolean canAcceptMimeType(String mimeType) {
				return mimeType.equals("text/xml");
			}

			/* (non-JavaDoc)
			 * @see net.sourceforge.sqlexplorer.preview.PreviewerFactory.Resolver#canAcceptObject(java.lang.Object)
			 */
			public boolean canAcceptObject(Object data) {
				return data instanceof XmlDataType;
			}

			/* (non-JavaDoc)
			 * @see net.sourceforge.sqlexplorer.preview.PreviewerFactory.Resolver#getPreviewerClass()
			 */
			public Class<? extends Previewer> getPreviewerClass() {
				return XmlPreviewer.class;
			}
		});
	}
	
}
