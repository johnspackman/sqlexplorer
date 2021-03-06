<?php include("common/header.php"); ?>

	<h1>Creating new Database Structure Context Actions</h1>
	<p>In this tutorial we will be extending the net.sourceforge.sqlexplorer.nodeContextAction extension point to add
	a new context action (also known as right-click menu option) for table nodes in the Database Structure tree. Let's create a 'Rename Table' feature for a MySQL table.</p>
	<p>As you probably remember from the previous tutorials, the first step is to add the extension point on the extensions tab of the fragment.xml.</p>
	<p><img src="screenshots/strucaction1.jpg" alt="" /></p>
	<p>Next, we create a new action by right clicking the extension point and adding a new action.</p>
	<p><img src="screenshots/strucaction2.jpg" alt="" /></p>
	<p>The details for the extension point are very similar to the previous extension points. The only new option we see here
	is the node-type field.  Using this field, we can select for which node types our action will be available.  For this example we will use 'table'
	as the node type.  It's possible to target multiple node types, by entering a comma separated list of types in this field.</p>
	<p><img src="screenshots/strucaction3.jpg" alt="" /></p>					
	<p>After you've generated the Java class, there are only 2 methods that we need to implement: <br/>
	<i>public String getText()</i>: This method should return the text for our new action that is displayed in the context menu.<br/>
	<i>public void run()</i>: This method is executed when the action is run by the user.</p>
	<p>For the sake of brevity, only part of the code for the run method is shown below.  The full source is available <a href="http://eclipsesql.cvs.sourceforge.net/eclipsesql/sqlexplorer_mysql/src/net/sourceforge/sqlexplorer/mysql/actions/RenameTable.java?view=markup">here</a>. 
	In the mean time, let's have a look at some of the more interesting lines.</p>
	<p><i>Line 37</i>: _selectedNodes is a protected attribute that contains an array of selected nodes in the database structure view.</p>
	<p><i>Line 40</i>: _view is a protected attribute that contains a reference to the DatabaseStructureView.  You can use this to get to many things, including a shell.</p>					
	<p><i>Line 40-52</i>: Standard SWT dialogs are used to prompt for a new table name.</p>					
	<p><i>Line 57</i>: Starting from the selected node, we can retrieve a session and it's corresponding SQLConnection.  Actions should always use the interactive connection and never the
	background connection, which is reserved for executing statements in a background thread.</p>															
	<p><i>Line 64</i>: Because our action will change the name of the current node, we'll need to force the tables' parent node to reload it's child nodes.</p>					
	<p><i>Line 65</i>: _treeViewer is a protected attribute that contains the SWT TreeViewer widget.  Here we tell it to refresh the widget UI starting from the tables' parent node.</p>															
	<p><img src="screenshots/strucaction4.jpg" alt="" /></p>				
	<p>Now we are ready to test our new action.  When we run our application, we see the new option available in the context menu.</p>
	<p><img src="screenshots/strucaction5.jpg" alt="" /></p>					
	<p>After selecting the rename option, a dialog appears.  Here we can enter a new table name.</p>
	<p><img src="screenshots/strucaction6.jpg" alt="" /></p>																				
	<p>The table has just been renamed to abrandnewtablename. Another new feature succesfully implemented!</p>
	<p><img src="screenshots/strucaction7.jpg" alt="" /></p>					

<?php include("common/footer.php"); ?>