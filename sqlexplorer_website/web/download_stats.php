<?php 
include("common/header.php"); 

function formatDate($pDate) 
{
	if(!$pDate)
	{
		return '';
	}
	return substr($pDate, 6, 2).'.'.substr($pDate, 4, 2).'.'.substr($pDate, 2, 2);
}

require_once("db/DbMySQL.cls.php");

$pastDate = getdate(strtotime("-30 day"));
	
$last30 = sprintf("%04d%02d%02d000000",$pastDate['year'],$pastDate['mon'],$pastDate['mday']);

$db = new DbMySql();
$query = 
	"SELECT" .
		" substr( DATE_INSERT, 1, 8 ) AS date," .
		" FILE as file ," .
		" count( DISTINCT REMOTE_ADDRESS ) as count" .
	" FROM" .
		" DOWNLOAD_LOG" .
	" WHERE" .
		" TYPE = 'F'" .
		" and DATE_INSERT >= '".$last30."'" .
	" GROUP BY" .
		" date," .
		" file" .
	" ORDER BY 1 desc,2";
$data = $db->getAllObjectsFor($query);

echo '<h1>Download Statistics</h1>';
echo '<table class="data" style="border-collapse:collapse;">';
echo '<tr><th>Date</th><th>File</th><th>Count</th></tr>';
foreach($data as $row)
{
	echo '<tr><td>'.formatDate($row->date).'</td><td>'.$row->file.'</td><td>'.$row->count.'</td></tr>';
} 
echo '</table>';

include("common/footer.php"); 
?>
