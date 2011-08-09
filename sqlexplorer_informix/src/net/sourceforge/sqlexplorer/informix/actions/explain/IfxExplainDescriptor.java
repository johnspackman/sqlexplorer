package net.sourceforge.sqlexplorer.informix.actions.explain;

import java.util.HashMap;

public class IfxExplainDescriptor {

	String id    = null;
	String name  = null;
	String type = null;
	HashMap<String,IfxExplainDescriptorData> data = new HashMap<String,IfxExplainDescriptorData>();
		
	public IfxExplainDescriptor(String _id, String _name, String _type) {
		this.id   = _id;
		this.name = _name;
		this.type = _type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addData(String _id, String _name, String _group, String _val) {
		data.put(_id, new IfxExplainDescriptorData(_name, _group, _val));
	}

	public IfxExplainDescriptorData getDataItem(String _id) {
		return data.get(_id);
	}

	public HashMap<String,IfxExplainDescriptorData> getDataMap() {
		return data;
	}
}
