<?php
/*
 * Created    : 20.02.2008 l2[a,b]
 * Version    : $Id$
 * Description:
 * 
 * All methods to retrieve or store db data
 * Caller have to define:
 * DB_HOST - MySql server name/ip
 * DB_USER - user to login
 * DB_PASS - pasword for login
 * DB_ANME - database to use 
 * 
 */

// get local settings
include_once("db/local.inc.php");

class DbMySQL
{
	var $dbLink;

	/*************************************************************
	 * Constructor
	 * creates DB connection
	 *
	 * Parameters:
	 *   none
	 * Returns:
	 *   nothing
	 *
	 **************************************************************/	
	function __construct($dbHost = false, $dbUser = false, $dbPass = false, $dbName = false)
	{
		if(!$dbHost)
		{
			$dbHost = DB_HOST;
		}	
		if(!$dbUser)
		{
			$dbUser = DB_USER;
		}	
		if(!$dbPass)
		{
			$dbPass = DB_PASS;
		}	
		if(!$dbName)
		{
			$dbName = DB_NAME;
		}	
		$this->dbLink = mysql_pconnect($dbHost, $dbUser, $dbPass);
		if(!$this->dbLink)
		{
        	trigger_error("Could not connect to database. " . mysql_error());
        	return;
        }
		mysql_select_db(DB_NAME, $this->dbLink);
//		mysql_set_charset('utf8', $this->dbLink);		
		mysql_query("SET NAMES 'utf8'", $this->dbLink);
		mysql_query("SET character_set_results = NULL", $this->dbLink);
	}
	
	/*************************************************************
	 * returns true if there is a valid db handle
	 *
	 * Parameters:
	 *   none
	 * Returns:
	 *   true or false
	 *
	 **************************************************************/	
	function isConnected()
	{
		return $this->dbLink != false;
	}	
	
	/*************************************************************
	 * encapsulate all DB queries
	 * for later analysis
	 *
	 * Parameters:
	 *   $query = SQL-Statement
	 * Returns:
	 *   mysql result
	 *
	 **************************************************************/	
	function execQuery($aQuery)
	{
//		startSqlTraceTime();
		$retval = mysql_query($aQuery, $this->dbLink);
//		stopSqlTraceTime($aQuery, $retval  == false);			
		if(!$retval)
		{
			trigger_error("Invalid query: " . $aQuery . " Error: " . mysql_error($this->dbLink));
		}
		return $retval; 
	}
	/*************************************************************
	 * execute a batch or stored procedure
	 *
	 * Parameters:
	 *   $query = SQL-Statement-Batch or SP-Call
	 * Returns:
	 *   mysql result
	 *
	 **************************************************************/	
	function execMultiQuery($aQuery)
	{
		return $this->execQuery($aQuery);
	}
	
	
	/*************************************************************
	 * execute the given query and return the fetched objects as
	 * an array or false if any error occured.
	 *
	 * Parameters:
	 *   aQuery - string with the query to execute
	 * Returns:
	 *   Array of objects or false
	 *
	 **************************************************************/	
	function getAllObjectsFor($aQuery)
	{
		$queryResult = $this->execQuery($aQuery);
		if(!$queryResult)
		{
			return false;
		} 
		$retval = array();
		while ($row = mysql_fetch_object($queryResult)) 
		{
			$retval[] = $row;                     
		}
		mysql_free_result($queryResult);
		return $retval; 
	}

	/*************************************************************
	 * execute the given query and return the fetched assoc arrays as
	 * an array or false if any error occured.
	 *
	 * Parameters:
	 *   aQuery - string with the query to execute
	 * Returns:
	 *   Array of assoc arrays or false
	 *
	 **************************************************************/	
	function getAllAssocsFor($aQuery)
	{
		$queryResult = $this->execQuery($aQuery);
		if(!$queryResult)
		{
			return false;
		} 
		$retval = array();
		while ($row = mysql_fetch_assoc($queryResult)) 
		{
			$retval[] = $row;                     
		}
		mysql_free_result($queryResult);
		return $retval; 
	}


	/*************************************************************
	 * execute the given query and return the first fetched value as an int.
	 *
	 * Parameters:
	 *   aQuery - string with the query to execute
	 *   pNotFoundValue value to be returned if nothing was found
	 * Returns:
	 *   $pNotFoundValue if any error occures or the result was empty otherwise the 
	 *   first fetched value
	 *
	 **************************************************************/	
	function getFirstValueFor($aQuery, $pNotFoundVale = -1)
	{
		$queryResult = $this->execQuery($aQuery);
		if(!$queryResult)
		{
			return $pNotFoundVale;
		} 
		$row = mysql_fetch_array($queryResult);
		if(!$row)
		{
			return $pNotFoundVale;
		}
		mysql_free_result($queryResult);
		return $row[0];
	}

	/*************************************************************
	 * return the last inserted id
	 *
	 * Parameters:
	 *   nothing
	 * Returns:
	 *   the id or 0
	 *
	 **************************************************************/	
	function getLastInsertedId()
	{
		return mysql_insert_id($this->dbLink);
	}
}

/*
 * $Log$
 * Revision 1.2  2008-10-26 08:05:50  andrej
 * weird cvs syncing on my laptop
 *
 * Revision 1.1  2008/02/20 16:39:53  cvs_heiko
 * deactivate user as admoin module, reworked mail functions
 *
 */
?>