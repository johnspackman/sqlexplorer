package net.sourceforge.sqlexplorer.db2.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLSourceTab;

public class ViewSourceTab extends AbstractSQLSourceTab {

    public String getSQL() {   
        return "select text from syscat.views where viewschema = ? and viewname = ?";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }

    public static String formatView(String view) {
    	return view.replaceAll(",",",\n").replaceAll(",\n ",",\n")
        .replaceAll(" SELECT ", "\nSELECT ")
        .replaceAll(" FROM ","\nFROM ")
        .replaceAll(" WHERE ","\nWHERE ")
        .replaceAll(" AND ","\n    AND ")
        .replaceAll(" ORDER BY ","\nORDER BY ")
        .replaceAll(" GROUP BY ","\nGROUP BY ")
        .replaceAll(" HAVING ","\nHAVING ")
        .replaceAll(" FETCH ","\nFETCH ")
        .replaceAll(" RIGHT ","\nRIGHT\n")
        .replaceAll(" LEFT ","\nLEFT\n")
        .replaceAll(" JOIN ","\nJOIN ")
        .replaceAll("LEFT\nJOIN","LEFT JOIN")
        .replaceAll("RIGHT\nJOIN","RIGHT JOIN")
        .replaceAll("LEFT\nOUTER\nJOIN","LEFT OUTER JOIN")
        .replaceAll("RIGHT\nOUTER\nJOIN","RIGHT OUTER JOIN")
        .replaceAll(" ON ","\n    ON ")
        .replaceAll(" UNION\n","\n\nUNION\n\n")
        .replaceAll("UNION\n\nALL","UNION ALL\n\n")
        .replaceAll(" EXCEPT\n","\n\nEXCEPT\n\n")
        .replaceAll("EXCEPT\n\nALL","EXCEPT ALL\n\n")
        .replaceAll(" INTERSECT\n","\n\nINTERSECT\n\n")
        .replaceAll("INTERSECT\n\nALL","INTERSECT ALL\n\n")
        .replaceAll(" select ", "\nselect ")
        .replaceAll(" from ","\nfrom ")
        .replaceAll(" where ","\nwhere ")
        .replaceAll(" and ","\n    and ")
        .replaceAll(" order by ","\norder by ")
        .replaceAll(" group by ","\ngroup by ")
        .replaceAll(" having ","\nhaving ")
        .replaceAll(" fetch ","\nfetch ")
        .replaceAll(" right ","\nright\n")
        .replaceAll(" left ","\nleft\n")
        .replaceAll(" join ","\njoin ")
        .replaceAll("left\njoin","left join")
        .replaceAll("right\njoin","right join")
        .replaceAll("left\nouter\njoin","left outer join")
        .replaceAll("right\nouter\njoin","right outer join")
        .replaceAll(" on ","\n    on ")
        .replaceAll(" union\n","\n\nunion\n\n")
        .replaceAll("union\n\nall","union all\n\n")
        .replaceAll(" except\n","\n\nexcept\n\n")
        .replaceAll("except\n\nall","except all\n\n")
        .replaceAll(" intersect\n","\n\nintersect\n\n")
        .replaceAll("intersect\n\nall","intersect all\n\n")
        .replaceAll("',\n","',")
        .replaceAll(",\n'",",'")
        .replaceAll("\0","");
    }
    public String getSource() {
    	return formatView(super.getSource());
    }
}
