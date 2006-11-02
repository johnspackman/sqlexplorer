<?php include("common/header.php"); ?>
		
	<h1>Extending SQL Explorer</h1>
	<p>Extending SQL Explorer is easy!  With just a little Java knowledge and a little database knowledge, you can get a long way.</p>
	<p>SQL Explorer 3.0.0 was designed with extensibility in mind.  Several eclipse extension points have been made available to make it
	 easy for you to create that feature you've always wanted.<br />Here are some example extension points that are available:</p>
	<ul>
		<li><i>Editor actions:</i> Why not create a new action that will show an explain plan for your own database?</li>
		<li><i>Nodes in the structure tree:</i> You can't see a list of procedures in the database structure view because you are using an estoric database?
		  No problem, you can easily add them using this extension point.</li>
		<li><i>Detail Tabs:</i> Perfect for displaying that extra little bit of information you've always wanted.</li>
		<li><i>Structure node actions:</i> Want to add a new right click option when you click on the new procedure node you created above?  This is the extension point for it.</li>												
		<li><i>Result table actions:</i> Need a new custom format export of your query results?  Look no further than this extension point..</li>
	</ul>
	<p>For each of the available extension points, you can find a small tutorial on how to use them in the next sections.
	</p>
	<p>The SQL Explorer code has been divided in two parts. The first part is the SQL Explorer core.  
	This is the main SQL Explorer plugin which contains all non database specific features.  You can find this in CVS as the sqlexplorer project.<br />
	The second part are the database specific plugin fragments.  These are in cvs under sqlexplorer_&lt;dbname&gt;. Currently 
	3 db specific fragments are already available: Oracle, DB2 and MySQL.</p>
	<p>If you want to add features to an already existing database fragment (or why not the sql explorer core?), 
	the best way to get started is too check out the source code from CVS.<br />Once you've added your features and
	everything works like you want it, you can combine your changes in a patch and upload them on sourceforge.
	Your patch will be included in the next release and as of then every SQL Explorer user can enjoy your new cool features.</p>
	<p>On the other hand, if you want to create a database specific plugin for which SQL Explorer doesn't have any support yet,
	the best way to get started is to create a new fragment project.  A small tutorial on how to start is available in the <a href="exBasics.php">next section</a>.</p>
					
<?php include("common/footer.php"); ?>