<?php include("common/header.php"); ?>

	<h1>Creating new Result Table Context Actions</h1>
	<p>Using the net.sourceforge.sqlexplorer.dataSetTableContextAction extension point, it's possible to add new context
	actions to the result table.</p>
	<p><img src="screenshots/results4.jpg" /></p>
	<p>In comparison to the other extension points, we have one new property available in the extension point definition: group.
	This value can be left blank, or you can enter the only supported value at this time: export.  When export is entered as a value, 
	the action will be appended to actions in the export submenu rather than the main menu.</p>
	<p><img src="screenshots/resultaction1.jpg" /></p>
	<p>Every action must extend AbstractDataSetTableContextAction.  This provides the protected attribute _table, which is a handle to the 
	result table table widget.  Have a look 
	<a href="http://eclipsesql.cvs.sourceforge.net/eclipsesql/sqlexplorer/src/net/sourceforge/sqlexplorer/dataset/actions/ExportCSVAction.java?revision=1.5&view=markup">here</a> 
	for some sample code on how the export to CSV works.</p>

<?php include("common/footer.php"); ?>