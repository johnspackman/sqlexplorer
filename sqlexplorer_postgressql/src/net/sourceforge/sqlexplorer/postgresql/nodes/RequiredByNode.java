package net.sourceforge.sqlexplorer.postgresql.nodes;

/**
 * Interface for nodes we can display dependants for.
 * 
 * @author Rocco Rutte <a href="mailto:pdmef@gmx.net">&lt;pdmef@gmx.net&gt;</a>.
 * 
 */
public interface RequiredByNode {
	/**
	 * Get SQL query to obtain dependants. <strong>Note</strong> that it
	 * <em>must</em> contain four parameters:
	 * <ul>
	 * <li>Schema name</li>
	 * <li>Relation name</li>
	 * <li>Schema name</li>
	 * <li>Relation name</li>
	 * </ul>
	 * 
	 * @param params
	 *            Query parameters (ugly hack).
	 * 
	 * @return Paramterized SQL detail query.
	 */
	public String getRequiredBySQL(Object[] params);

	/**
	 * The default query's head. Put a subquery returning a list of OIDs after
	 * it.
	 */
	public String QUERY_REQUIREDBY_HEAD = "SELECT DISTINCT type AS \"${postgresql.hdr.objtype}\"," +
			"CASE WHEN nspname IS NOT NULL THEN nspname||'.'||refname ELSE refname END AS \"${postgresql.hdr.qname}\"," +
			"tmp.\"DEP_TYPE\" AS \"${postgresql.hdr.deptype}\" FROM ("
			+ "SELECT DISTINCT classid, cl.relkind,"
			+ "		       CASE"
			+ "              WHEN cl.relkind IS NOT NULL THEN "
			+ "                 CASE SUBSTR(cl.relkind,1,1) "
			+ "						WHEN 'r' THEN '${postgresql.object.Table}' "
			+ "						WHEN 'i' THEN '${postgresql.object.Index}' "
			+ "						WHEN 'S' THEN '${postgresql.object.Sequence}' "
			+ "						WHEN 'v' THEN '${postgresql.object.View}' "
			+ "						WHEN 'c' THEN '${postgresql.object.Composite}' "
			+ "						WHEN 's' THEN '${postgresql.object.Special}' "
			+ "						WHEN 't' THEN '${postgresql.object.Toast}' "
			+ "					END"
			+ "		            WHEN tg.oid IS NOT NULL THEN '${postgresql.object.Trigger}'"
			+ "		            WHEN ty.oid IS NOT NULL THEN '${postgresql.object.Type}'"
			+ "		            WHEN ns.oid IS NOT NULL THEN '${postgresql.object.Schema}'"
			+ "		            WHEN pr.oid IS NOT NULL THEN '${postgresql.object.Procedure}'"
			+ "		            WHEN la.oid IS NOT NULL THEN '${postgresql.object.Language}'"
			+ "		            WHEN rw.oid IS NOT NULL THEN '${postgresql.object.Table}/${postgresql.object.Column}'"
			+ "		            WHEN co.oid IS NOT NULL THEN '${postgresql.object.Check}' || contype"
			+ "		            ELSE '${postgresql.object.Unknown}' END AS type,"
			+ "	       COALESCE(coc.relname, clrw.relname) AS ownertable,"
			+ "	       COALESCE(nsc.nspname, nso.nspname, nsp.nspname, nst.nspname, nsrw.nspname) AS nspname,"
			+ "	       COALESCE(cl.relname || '.' || att.attname, cl.relname, conname, proname, tgname, typname, lanname, rulename, ns.nspname) AS refname,"
			+ "	       CASE deptype"
			+ "					WHEN 'n' THEN '${postgresql.dep.normal}'::text"
			+ "			        WHEN 'a' THEN '${postgresql.dep.auto}'::text"
			+ "		    	    WHEN 'i' THEN '${postgresql.dep.internal}'::text"
			+ "		    	    WHEN 'p' THEN '${postgresql.dep.pin}'::text"
			+ "		    ELSE deptype::text"
			+ "	       END as \"DEP_TYPE\""
			+ "	  FROM pg_depend dep"
			+ "	  LEFT JOIN pg_class cl ON dep.objid=cl.oid"
			+ "	  LEFT JOIN pg_attribute att ON dep.objid=att.attrelid AND dep.objsubid=att.attnum"
			+ "	  LEFT JOIN pg_namespace nsc ON cl.relnamespace=nsc.oid"
			+ "	  LEFT JOIN pg_proc pr on dep.objid=pr.oid"
			+ "	  LEFT JOIN pg_namespace nsp ON pronamespace=nsp.oid"
			+ "	  LEFT JOIN pg_trigger tg ON dep.objid=tg.oid"
			+ "	  LEFT JOIN pg_type ty on dep.objid=ty.oid"
			+ "	  LEFT JOIN pg_namespace nst ON typnamespace=nst.oid"
			+ "	  LEFT JOIN pg_constraint co ON dep.objid=co.oid"
			+ "	  LEFT JOIN pg_class coc ON conrelid=coc.oid"
			+ "	  LEFT JOIN pg_namespace nso ON connamespace=nso.oid"
			+ "	  LEFT JOIN pg_rewrite rw ON dep.objid=rw.oid"
			+ "	  LEFT JOIN pg_class clrw ON clrw.oid=rw.ev_class"
			+ "	  LEFT JOIN pg_namespace nsrw ON cl.relnamespace=nsrw.oid"
			+ "	  LEFT JOIN pg_language la ON dep.objid=la.oid"
			+ "	  LEFT JOIN pg_namespace ns ON dep.objid=ns.oid"
			+ "	WHERE dep.refobjid IN ( ";

	/**
	 * The default query's middle part. After the OID subquery, put this part
	 * followed by the OID subquery.
	 */
	public String QUERY_REQUIREDBY_MID = ""
			+ " )	UNION ALL"
			+ "	    SELECT refclassid,'','${postgresql.object.Role}',null,null,rolname,'${postgresql.dep.normal}'::text AS \"DEP_TYPE\" FROM pg_shdepend dep LEFT JOIN pg_roles r ON refclassid=1260 AND refobjid=r.oid"
			+ "	    WHERE dep.refobjid IN ( ";

	/**
	 * The default query's tail. After the 2nd OID subquery, put this part.
	 */
	public String QUERY_REQUIREDBY_TAIL = " )" + "	    ORDER BY 1"
			+ "	) AS TMP" + "	ORDER BY type";

}
