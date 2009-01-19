package net.sourceforge.sqlexplorer.mysql.nodes;

import net.sourceforge.sqlexplorer.dbstructure.nodes.AbstractNode;

public class DummyNode extends AbstractNode {

	public DummyNode() {
		super("hidden");
	}
	
	@Override
	public String getLabelText() {
		return "";
	}

	@Override
	public void loadChildren() {

	}

}
