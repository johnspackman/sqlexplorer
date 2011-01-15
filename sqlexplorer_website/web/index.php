<?php include("common/header.php"); ?>

	<h1>About Eclipse SQL Explorer</h1>
	<p>Eclipse SQL Explorer is a thin SQL client that allows you to query and browse any JDBC compliant database.  It supports plugins with specialized functionality for individual databases (Oracle, DB2 and MySQL) and can be extended to include specialized support for other databases.</p>
	<p>The project started as a fork from the original <a href="http://sourceforge.net/projects/jfacedbc/">JFaceDb</a> project which has gone commercial and uses some of the core libraries of <a href="http://sourceforge.net/projects/squirrel-sql/">SQuirreL SQL</a>.</p>
	<p>The application is available as a standalone client or as a plugin for Eclipse 3.3.</p>
	
	<h1>Installation Instructions</h1>
	<p><b>Standalone Client</b></p>
	<p>To install the standalone client, <a href="http://sourceforge.net/project/showfiles.php?group_id=132863">download</a> 
	Eclipse SQL Explorer RCP.  Extract the zipfile and launch sqlexplorer.exe to start the application.</p>
	<p><b>Eclipse Plugin</b></p>
	<p><i>Download</i></p>				
	<p><a href="http://sourceforge.net/project/showfiles.php?group_id=132863">Download</a> the Eclipse SQL Explorer plugin and extract the zip file in your eclipse directory (requires Eclipse 3.3 or better).<br />
	After restarting eclipse with the -clean option, a new SQL Explorer perspective should be available.</p>
	<p><i>Eclipse Update Site</i></p>
	<p>You can install and update Eclipse SQL Explorer via the eclipse update mechanism.  The update site for Eclipse SQL Explorer is http://eclipsesql.sourceforge.net/</p>

<?php include("common/footer.php"); ?>