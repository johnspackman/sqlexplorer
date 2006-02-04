package net.sourceforge.sqlexplorer;

import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

public interface IConstants {
	String CLIP_EXPORT_COLUMNS ="clip_export_columns"; 
	String CLIP_EXPORT_SEPARATOR ="clip_export_separator"; 
	String AUTO_COMMIT="autocommit"; 
	String COMMIT_ON_CLOSE="commitonclose"; 
	String MAX_SQL_ROWS="maxSQLRows"; 
	String PRE_ROW_COUNT="preRowCount"; 
	String FONT="font"; 
	String SQL_ASSIST="assist"; 
	String SQL_EDITOR_CLASS = SQLEditor.class.getName();
    String SQL_QRY_DELIMITER = "SQLEditor.QueryDelimiter";
    String SQL_ALT_QRY_DELIMITER = "SQLEditor.AltQueryDelimiter";
    String SQL_COMMENT_DELIMITER = "SQLEditor.CommentDelimiter";
    String INCLUDE_COLUMNS_IN_TREE = "SQLEditor.IncludeColumns";
}
