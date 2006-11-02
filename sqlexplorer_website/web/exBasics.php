<?php include("common/header.php"); ?>

	<h1>Creating a new plugin fragment for SQL Explorer</h1>
	<p>This short tutorial shows you how to create a new plugin fragment for SQL Explorer using eclipse.  
	MySQL was used as an example for this tutorial, but don't create a new MySQL plugin, it already exists.
	Feel free to enhance it though ;-)</p>  
	<p>Before you start you need to have the SQL Explorer plugin available in eclipse.  It can be either installed as
	a plugin, or if you downloaded the source from CVS and have a SQL Explorer project in your workspace, that will work too.</p>
	<p>Ok, let's get started.</p>
	<p>Step 1: Select File &gt; New Project from the menu.</p>
		<p>
		<img src="screenshots/basics1.jpg" />
		</p>
	<p>Step 2: In the new project wizard that has just appeared, select Fragment Project and click next.</p>
		<p>
		<img src="screenshots/basics2.jpg" />
		</p>
	<p>Step 3: Enter your project details.  As the project name, use sqlexplorer_&lt;yourdbname&gt;.  
	The targeted runtime should be set to eclipse 3.2.</p>
		<p>
		<img src="screenshots/basics3.jpg" />
		</p>
	<p>Step 4: Here we have to enter a few more details.<br/>As the fragment id, use 'net.sourceforge.sqlexplorer.&lt;yourdbname&gt;'.<br/>
	For the fragment version, you can start from 1.0.0 if you like.  For the fragment name, I suggest using '&lt;yourdbname&gt; extension for SQL Explorer', but you are free
	to use anything you like.  The fragment provider should obviously be YOUR name.<br/>As the plugin ID, enter 'net.sourceforge.sqlexplorer'.  This is how your fragment will know
	that it is an extension to the core SQL Explorer plugin.  For the minimum version, enter the version number of the SQL Explorer version that you've got installed.  This should be 3.0.0 or higher.</p>
		<p>
		<img src="screenshots/basics4.jpg" />
		</p>
	<p>Step 5: We're almost done.  Eclipse has created your brand new fragment and it should look almost the same
	as the one below.  There are just a few more things to add.  In the project root, add a new folder 'icons', we'll need
	this later on.  In the source folder, add a new package named 'net.sourceforge.sqlexplorer.&lt;yourdbname&gt;' by right clicking
	on the folder and selecting New &gt; Package.  Once the package is created, right click on the package and select New &gt; File.
	Create an empty file called 'text.properties'.  We'll use this file later on to store all text strings for the project.  It is important
	that this file is in the main package, otherwise it might not be found by the core plugin.
	</p>
		<p>
		<img src="screenshots/basics5.jpg" />
		</p>	
	<p>Congratulations.  You've just created a new fragment project for SQL Explorer.  Now head on over to the 
	<a href="exDetail.php">next section</a> and create that feature you've been waiting to code!
	</p>							
					
<?php include("common/footer.php"); ?>