package net.sourceforge.sqlexplorer.informix.actions.explain;

//import java.sql.ResultSet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeColumn;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.informix.jdbc.IfxBblob;
import com.informix.jdbc.IfxLobDescriptor;
import com.informix.jdbc.IfxLocator;
import com.informix.jdbc.IfxSmartBlob;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;
import net.sourceforge.sqlexplorer.informix.actions.explain.ExplainNode;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;


public class ExplainExecution extends AbstractSQLExecution {

    private static final Log _logger = LogFactory.getLog(AbstractNode.class);
    private CallableStatement cstmt2;
    //    private PreparedStatement _prepStmt;
    
	HashMap<String,IfxExplainDescriptor> descMap = new HashMap<String,IfxExplainDescriptor>();
    
    static class MyColumnProvider extends ColumnLabelProvider {

    	int idx = 0;
    	
    	public MyColumnProvider(int colidx) {
    		this.idx = colidx;
    	}
    	    	
    	public String getToolTipText(Object element) {
            ExplainNode en = (ExplainNode) element;
            String ttip = en.getToolTipText();
            if (ttip.equals("")) return null; else return ttip;
    	}
    	
    	public Image getImage(Object element) {
//    		return ImageUtil.getFragmentImage("net.sourceforge.sqlexplorer.informix", Messages.getString("informix.images.chunk"));
    		return null;
    	}

    	public String getText(Object element) {
            ExplainNode en = (ExplainNode) element;
            if (idx == 0) return en.toString();
            if (idx == 1) {
            	int cost = en.getCost();
            	if (cost >= 0) return Integer.toString(cost); 
            }
            if (idx == 2) {
            	int cost = en.getEstRows();
            	if (cost >= 0) return Integer.toString(cost); 
            }
            
            return "";
    	}
    	
    }
   
	
    public ExplainExecution(SQLEditor editor, QueryParser queryParser) {
    	super(editor, queryParser);
        
        // set initial message
        setProgressMessage("oota..");
    }
	
    private void displayResults(final ExplainNode node, final Query query) {
        
    	getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {

            	CTabItem tabItem = allocateResultsTab(query);
            	if (tabItem == null) return;

            	Composite composite = null;
                try {
                    composite = new Composite(tabItem.getParent(), SWT.NONE); //SWT.FULL_SELECTION
                    tabItem.setControl(composite);

                    GridLayout gLayout = new GridLayout();
                    gLayout.numColumns = 2;
                    gLayout.marginLeft = 0;
                    gLayout.horizontalSpacing = 0;
                    gLayout.verticalSpacing = 0;
                    gLayout.marginWidth = 0;
                    gLayout.marginHeight = 0;
                    composite.setLayout(gLayout);

                    Composite pp = new Composite(composite, SWT.NULL);
                    pp.setLayout(new FillLayout());
                    pp.setLayoutData(new GridData(GridData.FILL_BOTH));
                    TreeViewer tv = new TreeViewer(pp, SWT.MULTI);
                    tv.getTree().setLinesVisible(true);
                    tv.getTree().setHeaderVisible(true);

                    tv.setContentProvider(new ITreeContentProvider() {
                        public void dispose() {}

                        public Object[] getChildren(Object parentElement) {
                        	ExplainNode nd = (ExplainNode)parentElement;
                        	ExplainNode[] nda = nd.getChildren();
                        	
                    		_logger.debug("Children of "+nd.getObject_name());
                        	for (int i = 0; i < nda.length; i++) {
                        		_logger.debug("--"+nda[i].getObject_name());
                        	}
                        	//                            return ((ExplainNode) parentElement).getChildren();
                        	return nda;
                        }

                        public Object[] getElements(Object inputElement) {
                            ExplainNode nd = ((ExplainNode) inputElement);
                            return nd.getChildren();
                        }

                        public Object getParent(Object element) {
                            return ((ExplainNode) element).getParent();
                        }

                        public boolean hasChildren(Object element) {
                            if (((ExplainNode) element).getChildren().length > 0) return true;
                            return false;
                        }

                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
                    });
                    
                    ColumnViewerToolTipSupport.enableFor(tv);
        		    String[] colids = new String[3];
        		    for (int i = 0; i < 3 ; i++){
        		    	TreeViewerColumn column = new TreeViewerColumn(tv, SWT.NONE);
        		    	column.setLabelProvider(new MyColumnProvider(i));
        		    	TreeColumn col  = column.getColumn();
        		    	if (i == 1) col.setText("Est. Cost");
        		    	if (i == 2) col.setText("Est. # of rows");
        		    	col.setResizable(true);
        				col.setWidth(150);
        		    	colids[i] = Integer.toString(i);
        		    }
        		    tv.setColumnProperties(colids);
                    
                    tv.setInput(node);
                    tv.refresh();
                    tv.expandAll();

                    // make columns full size
                    for (int i = 0; i < tv.getTree().getColumnCount(); i++) {
                        tv.getTree().getColumn(i).pack();
                    }
                    
                    composite.layout();
                    composite.redraw();
                    
                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SQLExplorerPlugin.error("Error creating explain tab", e);
                }
            };
        });
    
    }
    
    protected void setQueryData(ExplainNode eNode, String dataID) {

    	String ttip = "";
    	HashMap<String,IfxExplainDescriptorData> tmpMap = descMap.get(dataID).getDataMap();
		for (Map.Entry<String,IfxExplainDescriptorData> f : tmpMap.entrySet()) {
			if (f.getValue().getName().equals("Estimated Cost")) { 
				eNode.setCost(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Estimated Output Rows")) { 
				eNode.setEstRows(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Query Stmt")) { 
				eNode.setObject_name(f.getValue().getVal());
			} 
			else ttip += f.getValue().getName()+": "+f.getValue().getVal()+"\n"; 
		}
		eNode.setToolTip(ttip);
    }

    protected void setJoinData(ExplainNode eNode, String dataID) {

    	String ttip = "";
    	String name = "";
    	
    	HashMap<String,IfxExplainDescriptorData> tmpMap = descMap.get(dataID).getDataMap();
		for (Map.Entry<String,IfxExplainDescriptorData> f : tmpMap.entrySet()) {
			if (f.getValue().getName().equals("Estimated Cost")) { 
				eNode.setCost(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Estimated Output Rows")) { 
				eNode.setEstRows(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("On Filters")) { 
				name += " ("+f.getValue().getVal()+")";
			} 
			else ttip += f.getValue().getName()+": "+f.getValue().getVal()+"\n"; 
		}
		eNode.setToolTip(ttip);
		eNode.setObject_name("Result Join"+name);
    }
    
    protected void setIdxData(ExplainNode eNode, String dataID) {

    	String ttip = "";
    	String name = "";
    	
    	HashMap<String,IfxExplainDescriptorData> tmpMap = descMap.get(dataID).getDataMap();
		for (Map.Entry<String,IfxExplainDescriptorData> f : tmpMap.entrySet()) {
			if (f.getValue().getName().equals("Estimated Cost")) { 
				eNode.setCost(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Estimated Output Rows")) { 
				eNode.setEstRows(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Filters")) { 
				name += " ("+f.getValue().getVal()+")";
			} 
			else ttip += f.getValue().getName()+": "+f.getValue().getVal()+"\n"; 
		}
		eNode.setToolTip(ttip);
		eNode.setObject_name("Index Scan"+name);
    }    
    
    protected void setOtherData(ExplainNode eNode, String dataID) {

    	String ttip = "";
    	
    	HashMap<String,IfxExplainDescriptorData> tmpMap = descMap.get(dataID).getDataMap();
		for (Map.Entry<String,IfxExplainDescriptorData> f : tmpMap.entrySet()) {
			if (f.getValue().getName().equals("Estimated Cost")) { 
				eNode.setCost(Integer.parseInt(f.getValue().getVal()));
			} else
			if (f.getValue().getName().equals("Estimated Output Rows")) { 
				eNode.setEstRows(Integer.parseInt(f.getValue().getVal()));
			} 
			else ttip += f.getValue().getName()+": "+f.getValue().getVal()+"\n"; 
		}
		eNode.setToolTip(ttip);
		eNode.setObject_name(descMap.get(dataID).getName());
    }    
    
    protected ExplainNode createNodes(ExplainNode parentNode, NodeList nd) {

		ExplainNode eNode = null;
    	for (int i = 0; i < nd.getLength(); i++) {
			Node cNode = nd.item(i);
			if (cNode.getNodeName().equals("node")) {
					
				eNode = new ExplainNode(parentNode);
				
    			String typeID = cNode.getAttributes().getNamedItem("type").getNodeValue();
				String dataID = cNode.getFirstChild().getNextSibling().getAttributes().getNamedItem("descriptorid").getNodeValue();
				
				
				if (typeID.equals("0616002a")) this.setQueryData(eNode, dataID);
					else if (typeID.equals("01160027")) this.setJoinData(eNode, dataID);
						else if (typeID.equals("0108001e")) this.setIdxData(eNode, dataID);
							else this.setOtherData(eNode, dataID);

    			if (parentNode != null) parentNode.add(eNode);
    			
				this.createNodes(eNode, nd.item(i).getChildNodes());

			}
    	}
		return eNode;
    }
    
    private String escapeXML(String str) {
    	
    	StringBuffer buf = new StringBuffer(str.length() * 2);
        int i;
        for (i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);

            if (ch > 0x7F){
                int intValue = ch;
                buf.append("&#");
                buf.append(intValue);
                buf.append(';');
            }
            else {
            	switch (ch) {
            		case 34: buf.append("&quot;"); break;
            		case 38: buf.append("&amp;"); break;
            		case 39: buf.append("&apos;"); break;
            		case 60: buf.append("&lt;"); break;
            		case 62: buf.append("&gt;"); break;
            		default: buf.append(ch); break;
            	}
            }
        }
        return buf.toString();
    }  
    
	@Override
	protected void doExecution(IProgressMonitor monitor) throws Exception {

        Connection conn = null;
        ResultSet rs = null;
		String queryStr = ""; 
        
        try {
            setProgressMessage("Executing explain");

			conn = _connection.getConnection();
			cstmt2 = conn.prepareCall("{call informix.explain_sql(?, ?, ?, ?, ?, ?, ?)}");
            
			String reqLocale = "en_us.8859-1";
/*			
			String[] urlParams = conn.getMetaData().getURL().split(";");
			for (int i = 0; i < urlParams.length; i++) {
				if (urlParams[i].startsWith("CLIENT_LOCALE=") && urlParams[i].length() > 14) {
					reqLocale = urlParams[i].substring(14);
				}
			}
			
    		_logger.debug("REQLOCALE: "+reqLocale);
*/			
            Query query = null;
        	for (Iterator<Query> iter = getQueryParser().iterator(); iter.hasNext(); ) {
        		query = iter.next();
    			if (monitor.isCanceled())
    				break;

            	try {

            		if (query.getQueryType() == Query.QueryType.SELECT) {
	            		queryStr = query.getQuerySql().toString();
            		}
            		else {
                		_logger.debug("Can explain only select query!");
            			continue;
            		}
		    		String sqlIn = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><plist version=\"1.0\"><dict><key>MAJOR_VERSION</key><integer>1</integer><key>MINOR_VERSION</key><integer>0</integer><key>REQUESTED_LOCALE</key><string>"+reqLocale+"</string><key>RETAIN</key><string>N</string><key>TRACE</key><string>N</string><key>SQL_TEXT</key><string>"+escapeXML(queryStr)+"</string></dict></plist>";
            		
//            		_logger.debug("Explaining: \""+query.getQuerySql()+"\"");

		    		cstmt2.registerOutParameter( 1, Types.INTEGER );
		    		cstmt2.registerOutParameter( 2, Types.INTEGER );
		    		cstmt2.setString(3,null);
		    		cstmt2.setNull( 5, Types.BLOB );              // Filter
		    		cstmt2.registerOutParameter( 6, Types.BLOB ); // XML_OUTPUT
		    		cstmt2.registerOutParameter( 7, Types.BLOB ); // XML_MESSAGE
		
		    		byte[] buffer = new byte[8000];
		    		buffer = sqlIn.getBytes();
	
		    		IfxLobDescriptor loDesc = new IfxLobDescriptor(conn);
		    		IfxLocator loPtr = new IfxLocator();
		    		IfxSmartBlob smb = new IfxSmartBlob(conn);
		    		int loFd = smb.IfxLoCreate(loDesc, IfxSmartBlob.LO_RDWR, loPtr);
		
		    		int n = buffer.length;
		    		if (n > 0) n = smb.IfxLoWrite(loFd, buffer);
		    	  
		    		smb.IfxLoClose(loFd);
		    		Blob blb = new IfxBblob(loPtr);
		    		cstmt2.setBlob(4, blb); // set the blob column
		    		rs = cstmt2.executeQuery();

		    		if (monitor.isCanceled()) return;
		    		
	    			byte[] buf = new byte[1000000];
	    			int size = 0;
		    		while (rs.next()) {
		    			IfxBblob b = (IfxBblob) rs.getBlob(1);
	
		    			if (b != null) {
			    			IfxLocator loptr = b.getLocator();
			    			IfxSmartBlob smbl = new IfxSmartBlob(conn);
			    			int lofd = smbl.IfxLoOpen(loptr, IfxSmartBlob.LO_RDONLY);
//			    			long size = smbl.IfxLoSize(lofd); 
			    			// func returns long, but we can read only Integer.MAX into byte array,
			    			// and to parse xml with DOM/Xpath it cannot be bigger than buf[(int)]
			    			size = smbl.IfxLoRead(lofd, buf, buf.length);
			    			_logger.debug("size="+size);		    			
			    			smbl.IfxLoClose(lofd);
			    			smbl.IfxLoRelease(loptr);
		    			} else _logger.debug("b==null");
	    			}

		    		if (size > 0) {
			    		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			    		DocumentBuilder builder = domFactory.newDocumentBuilder();
			    		InputStream reader = new ByteArrayInputStream(buf, 0, size);
			    		Document doc = builder.parse(reader);
		    			reader.close();
		    			
			    		XPath xpath = XPathFactory.newInstance().newXPath();
			    		XPathExpression expr = xpath.compile("/explain/plans/descriptor");
			    		
			    		Object xpres = expr.evaluate(doc, XPathConstants.NODESET);
			    		NodeList xpnodes = (NodeList) xpres;
			    		descMap = new HashMap<String,IfxExplainDescriptor>();
			    		
			    		// Read once node names and data
			    		for (int i = 0; i < xpnodes.getLength(); i++) {
			    			NamedNodeMap attrs = xpnodes.item(i).getAttributes();
			    			String descID      = attrs.getNamedItem("id").getNodeValue(); 
			    			String descName    = attrs.getNamedItem("name").getNodeValue();
			    			IfxExplainDescriptor descriptor = new IfxExplainDescriptor(descID, descName, "");

			    			NodeList childNodes = xpnodes.item(i).getChildNodes();
				    		for (int j = 0; j < childNodes.getLength(); j++) {
				    			Node cNode = childNodes.item(j);
				    			if (cNode.getNodeType() == Node.ELEMENT_NODE) {
				    				if (cNode.getNodeName().equals("data")) {
				    					
						    			attrs = cNode.getAttributes();
						    			String dataNodeID   = attrs.getNamedItem("id").getNodeValue();
						    			String dataNodeName = attrs.getNamedItem("name").getNodeValue();
						    			String dataNodeGrp  = attrs.getNamedItem("group").getNodeValue();
				    					String dataNodeVal = "";
				    					if (cNode.getFirstChild() != null) {
				    						dataNodeVal = cNode.getFirstChild().getNodeValue();
				    					}

						    			descriptor.addData(dataNodeID, dataNodeName, dataNodeGrp, dataNodeVal);
				    				}
				    			}
				    		}
			    			descMap.put(descID, descriptor);
				    		
			    		}
			    		
			    		expr   = xpath.compile("/explain/plans/diagram/node");
			    		xpres  = expr.evaluate(doc, XPathConstants.NODESET);
			    		xpnodes = (NodeList) xpres;
			    		
			    		ExplainNode baseNode = this.createNodes(null, xpnodes);
			            this.displayResults(baseNode, query);
		    		} else _logger.debug("Explain size 0, probably error");
		    		
	    		
		    		IfxBblob outmsg_b = (IfxBblob)cstmt2.getBlob(7);
		    		if (outmsg_b == null) {
		    			_logger.debug("outmsg_b is null");
		    		}
		    		else {
//		    			byte[] buf2 = new byte[80000];
		    			
		    			IfxLocator xml_msg_loptr = outmsg_b.getLocator();
	    				IfxSmartBlob xml_msg_smbl = new IfxSmartBlob(conn);
	    				int msg_out_lofd = xml_msg_smbl.IfxLoOpen(xml_msg_loptr, IfxSmartBlob.LO_RDONLY);
//	    				int xml_msg_size = xml_msg_smbl.IfxLoRead(msg_out_lofd, buf2, 80000);
	    				xml_msg_smbl.IfxLoClose(msg_out_lofd);          
	    				xml_msg_smbl.IfxLoRelease(xml_msg_loptr);
//		                _logger.debug("ERR: "+new String(buf2));
	    		   }			
		           cstmt2.close();
		    		
            	} catch (SQLException sqlex) {
            		_logger.debug("SQLException: ");
            		_logger.debug(sqlex);
            	}
	    		
        	} 

        } catch (Exception e) {

        	StringWriter sw = new StringWriter();
        	e.printStackTrace(new PrintWriter(sw));
        	String exceptionAsStrting = sw.toString();        	
        	
        	_logger.debug("Exception: "+exceptionAsStrting);
            if (cstmt2 != null) {
                try {
                	cstmt2.close();
                	cstmt2 = null;
                } catch (Exception e1) {
                    SQLExplorerPlugin.error("Error closing statement.", e);
                }
            }
            throw e;
        }
	}

	@Override
	protected void doStop() throws Exception {
		Exception t = null;

        if (cstmt2 != null) {

            try {
            	cstmt2.cancel();
            } catch (Exception e) {
                t = e;
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
            	cstmt2.close();
            	cstmt2 = null;
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

        if (t != null) {
            throw t;
        }
	}

}
