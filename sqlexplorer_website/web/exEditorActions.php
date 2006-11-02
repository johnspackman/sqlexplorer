<?php include("common/header.php"); ?>

	<h1>Creating new Editor Action Buttons</h1>
	<p>Using the net.sourceforge.sqlexplorer.editorAction extension point, we can add new action buttons to the SQL Editor.</p>
	<p><img src="screenshots/editoraction0.jpg" alt="" /></p>
	<p>Creating the extension action is done in the same way as for the other extension points, so I won't go into too much detail here.
	As you can see from the image below, there are no new unknown properties to define.</p>
	<p><img src="screenshots/editoraction1.jpg" alt="" /></p>
	<p>In the generated class we implement 2 methods:<br/>
	<i>getText()</i>: This String will be used for the ToolTip popup when hovering above the action button.<br/>
	<i>run()</i>: The main method for our action.</p>
	<p>After adding some complex code, our action will look something like this:</p>					
	<p><img src="screenshots/editoraction2.jpg" alt="" /></p>					
	<p>Again we are ready to test our code.  After starting SQL Explorer with the new code, we can see that
	our new action button is available in the editor.</p>
	<p><img src="screenshots/editoraction3.jpg" alt="" /></p>
	<p>And here's what the editor looks like after clicking the action button:</p>
	<p><img src="screenshots/editoraction4.jpg" alt="" /></p>
					
<?php include("common/footer.php"); ?>