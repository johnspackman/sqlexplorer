<?php 
require_once("db/DbMySQL.cls.php");

if(! isset($_GET["pg"]))
{
	header("HTTP/1.0 404 Not Found");;
	return;	
}

if(!defined("UPDATE_SITE_DIR"))
{
	define("UPDATE_SITE_DIR",'updates/');
}
$page = $_GET["pg"];
$fileName = UPDATE_SITE_DIR . $page;

$f = fopen($fileName, 'r');
if(!$f)
{
	header("HTTP/1.0 404 Not Found");;
	return;
}
fclose($f);


/*
// PIWIK integration
// disabled for now, the results are not as expected
$f = fopen('http://apps.sourceforge.net/piwik/eclipsesql/piwik.php?idsite=1&download='.$page.'&rand='.rand().'&redirect=0','r');
if($f)
{
	fread($f, 10);
	fclose($f);
}
*/

$cacheTime = 3600 * 24 * 21;
// date of this file.
$mtime = filemtime($fileName);

// Create a HTTP conformant date, example 'Mon, 22 Dec 2003 14:16:16 GMT'
$gmt_mtime = gmdate('D, d M Y H:i:s', $mtime).' GMT';

// check if the last modified date sent by the client is the the same as
// the last modified date of the requested file. If so, return 304 header
// and exit.
if(isset($_SERVER['HTTP_IF_MODIFIED_SINCE']))
{
	if ($_SERVER['HTTP_IF_MODIFIED_SINCE'] == $gmt_mtime)
	{
		header('HTTP/1.1 304 Not Modified');
		return;
	}
}
// check if the Etag sent by the client is the same as the Etag of the
// requested file. If so, return 304 header and exit.
if (isset($_SERVER['HTTP_IF_NONE_MATCH']))
{
	if (str_replace('"', '', stripslashes($_SERVER['HTTP_IF_NONE_MATCH'])) == md5($mtime.$file))
	{
		header('HTTP/1.1 304 Not Modified');

		// abort processing and exit
		return;
	}
}

// check content type
$file_extension = strtolower(substr(strrchr($fileName,"."),1));

$ctype="application/octet-stream";
switch ($file_extension) 
{
    case "xml": $ctype="text/xml"; break;
    case "jar": $ctype="application/x-java-archive"; break;
    case "zip": $ctype="application/zip"; break;
}

// set the content-type
header('Content-Type: ' . $ctype);
header('Content-Length: ' . filesize($fileName));

// send a unique 'strong' identifier. This is always the same for this 
// particular file while the file itself remains the same.
header('ETag: "'.md5($mtime.$fileName).'"');


// output last modified header using the last modified date of the file.
header('Last-Modified: '.$gmt_mtime);
// this resource expires one month from now.
header('Expires: '.gmdate('D, d M Y H:i:s', time() + $cacheTime).' GMT');
// tell all caches that this resource is publically cacheable.
header('Cache-Control: public,max-age=' . $cacheTime );

readfile($fileName);

// dump server params for debug purposes
/*
$dump = "------------------------------------\n";
foreach($_SERVER as $key => $value)
{
	$dump .= $key .": " . $value . "\n";
}
echo $dump;
*/

// try to log the download
$db = new DbMySql();
$remoteIp = isset($_SERVER["HTTP_X_REMOTE_ADDR"]) ? $_SERVER["HTTP_X_REMOTE_ADDR"] : (isset($_SERVER["REMOTE_ADDR"]) ? $_SERVER["REMOTE_ADDR"] : '???');
$userAgent = isset($_SERVER["HTTP_USER_AGENT"]) ? $_SERVER["HTTP_USER_AGENT"] : '???';
$type = $file_extension == "xml" ? "x" : "?";
list($msec, $sec) = explode(" ", microtime());
$micro = sprintf("%03d", ((float)$msec) * 1000);
$now = strftime("%Y%m%d%H%M%S").$micro;

if($type == "?")
{
	$type = substr($page,0,1);
}
$query = "insert into DOWNLOAD_LOG(DATE_INSERT,TYPE,FILE,REMOTE_ADDRESS,USER_AGENT) values(" .
	"'".$now."',".
	"'".$type."',".
	"'".$page."',".
	"'".$remoteIp."',".
	"'".$userAgent."')";
$db->execQuery($query);
return; 
 
?>
