package net.sourceforge.sqlexplorer.informix.actions.explain;

public class IfxExplainDescriptorData {

	String name  = null;
	String group = null;
	String val   = null;
	
	public IfxExplainDescriptorData(String _name, String _group, String _val) {
		this.name = _name;
		this.group = _group;
		this.val = _val;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getVal() {
		return this.val;
	}

}
