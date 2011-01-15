<?php
    $path = $_SERVER['PHP_SELF'];
    $page = basename($path);
?> 


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="verify-v1" content="o1h147FC4rmjoSFUN29aNjAl3EnazrCNY3exUPNgXzs=" />
		<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
		</script>
		<script type="text/javascript">
		_uacct = "UA-153465-3";
		urchinTracker();
		</script>
		<title>Eclipse SQL Explorer</title>
		<link href="style/style.css" media="screen" type="text/css" rel="stylesheet" />
		<link rel="shortcut icon" href="favicon.ico">
	</head>


	<body>

		<table class="mainTable">
			
			<tr>
			
				<!-- HEADER -->						
				
				<td class="topLeft">
					<a href="/"><img src="images/logo.jpg" class="logo" alt="Eclipse SQL Explorer" /></a>	
				</td>
				<td class="topRight">
					<span class="headerLinks"> 
						<a href="/">Home</a> | 
						<a href="screenshots.php">Screenshots</a> | 
						<a href="http://sourceforge.net/project/showfiles.php?group_id=132863">Download</a>	| 
						<a href="http://sourceforge.net/forum/?group_id=132863">Forums</a> |
						<a href="http://sourceforge.net/tracker/?group_id=132863&amp;atid=725498">Feature Requests</a> | 
						<a href="http://sourceforge.net/tracker/?group_id=132863&amp;atid=725495">Bugs</a>
					</span>
				</td>
			</tr>
			
			<tr>

				<!-- MENU -->			
				
				<td class="leftBar">
				
				<div class="menu">
				<ul>
					<?php 
						if ($page=='index.php') { 
							echo "<li>Home</li>";
						} else {
							echo "<li><a href=\"index.php\">Home</a></li>";
						}
						if ($page=='screenshots.php') { 
							echo "<li>Screenshots</li>";
						} else {
							echo "<li><a href=\"screenshots.php\">Screenshots</a></li>";
						}
						if ($page=='features.php') { 
							echo "<li>Overview";
						} else {
							echo "<li><a href=\"features.php\">Overview</a>";
						}
					?>
					<ul>
						<?php 
							if ($page=='drivers.php') { 
								echo "<li>Driver Preferences</li>";
							} else {
								echo "<li><a href=\"drivers.php\">Driver Preferences</a></li>";
							}
							if ($page=='connections.php') { 
								echo "<li>Connections View</li>";
							} else {
								echo "<li><a href=\"connections.php\">Connections View</a></li>";
							}
							if ($page=='sqleditor.php') { 
								echo "<li>SQL Editor</li>		";
							} else {
								echo "<li><a href=\"sqleditor.php\">SQL Editor</a></li>		";
							}
							if ($page=='sqlresults.php') { 
								echo "<li>SQL Results</li>";
							} else {
								echo "<li><a href=\"sqlresults.php\">SQL Results</a></li>";
							}
							if ($page=='sqlhistory.php') { 
								echo "<li>SQL History</li>";
							} else {
								echo "<li><a href=\"sqlhistory.php\">SQL History</a></li>";
							}
							if ($page=='structure.php') { 
								echo "<li>Structure View</li>";
							} else {
								echo "<li><a href=\"structure.php\">Structure View</a></li>";
							}
							if ($page=='detail.php') { 
								echo "<li>Detail View</li>";
							} else {
								echo "<li><a href=\"detail.php\">Detail View</a></li>";
							}
							if ($page=='dbspecific.php') { 
								echo "<li>DB2, Oracle &amp; MySQL Features</li>";
							} else {
								echo "<li><a href=\"dbspecific.php\">DB2, Oracle &amp; MySQL Features</a></li>";
							}
						?>
					</ul></li>
					<?php
						if ($page=='extending.php') { 
							echo "<li>Extending SQL Explorer";
						} else {
							echo "<li><a href=\"extending.php\">Extending SQL Explorer</a>";
						}
					?>					
					<ul>
						<?php 
							if ($page=='exBasics.php') { 
								echo "<li>Create Fragment</li>";
							} else {
								echo "<li><a href=\"exBasics.php\">Create Fragment</a></li>";
							}
							if ($page=='exDetail.php') { 
								echo "<li>Detail Pages</li>";
							} else {
								echo "<li><a href=\"exDetail.php\">Detail Pages</a></li>";
							}
							if ($page=='exNodes.php') { 
								echo "<li>Structure Nodes</li>";
							} else {
								echo "<li><a href=\"exNodes.php\">Structure Nodes</a></li>";
							}
							if ($page=='exStructureAction.php') { 
								echo "<li>Structure Actions</li>";
							} else {
								echo "<li><a href=\"exStructureAction.php\">Structure Actions</a></li>";
							}
							if ($page=='exEditorActions.php') { 
								echo "<li>Editor Actions</li>";
							} else {
								echo "<li><a href=\"exEditorActions.php\">Editor Actions</a></li>";
							}
							if ($page=='exResult.php') { 
								echo "<li>Result Actions</li>";
							} else {
								echo "<li><a href=\"exResult.php\">Result Actions</a></li>";
							}
						?>
					</ul></li>
					<?php
						if ($page=='resources.php') { 
							echo "<li>Resources</li>";
						} else {
							echo "<li><a href=\"resources.php\">Resources</a></li>";
						}
						if ($page=='help.php') { 
							echo "<li>Help Wanted</li>";
						} else {
							echo "<li><a href=\"help.php\">Help Wanted</a></li>";
						}
					?>
				</ul>
				</div>
				<a href="http://sourceforge.net"><img class="sfLogo" src="http://sflogo.sourceforge.net/sflogo.php?group_id=132863&amp;type=1" alt="SourceForge.net Logo" /></a>
				</td>
				
				<td class="content">
		