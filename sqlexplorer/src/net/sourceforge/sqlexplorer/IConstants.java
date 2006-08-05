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

    String CLIP_EXPORT_COLUMNS = "SQLEditor.ClipExportColumns";

    String CLIP_EXPORT_SEPARATOR = "SQLEditor.ClipExportSeparator";

    String AUTO_COMMIT = "SQLEditor.AutoCommit";

    String COMMIT_ON_CLOSE = "SQLEditor.CommitOnClose";

    String MAX_SQL_ROWS = "SQLEditor.MaxSQLRows";

    String PRE_ROW_COUNT = "SQLEditor.PreRowCount";

    String FONT = "SQLEditor.Font";

    String SQL_ASSIST = "SQLEditor.Assist";

    String SQL_EDITOR_CLASS = SQLEditor.class.getName();

    String SQL_QRY_DELIMITER = "SQLEditor.QueryDelimiter";

    String SQL_ALT_QRY_DELIMITER = "SQLEditor.AltQueryDelimiter";

    String SQL_COMMENT_DELIMITER = "SQLEditor.CommentDelimiter";

    String INCLUDE_COLUMNS_IN_TREE = "SQLEditor.IncludeColumns";

    String WARN_IF_LARGE_LIMIT = "SQLEditor.WarnIfLargeLimit";

    String WARN_LIMIT = "SQLEditor.WarnLimit";

    String DEFAULT_DRIVER = "Drivers.DefaultDriverName";

    /** The color key for multi-line comments in Java code. */
    String SQL_MULTILINE_COMMENT = "SQLEditor.MultiLineCommentColor";

    /** The color key for single-line comments in Java code. */
    String SQL_SINGLE_LINE_COMMENT = "SQLEditor.SingleLineCommentColor";

    /** The color key for SQL keywords in Java code. */
    String SQL_KEYWORD = "SQLEditor.KeywordColor";

    /** The color key for string and character literals in Java code. */
    String SQL_STRING = "SQLEditor.StringColor";

    /** The color key for database tables names */
    String SQL_TABLE = "SQLEditor.TableColor";

    /** The color key for database tables column names */
    String SQL_COLUMS = "SQLEditor.ColumnsColor";

    String HISTORY_AUTOSAVE_AFTER = "SQLHistory.AutoSaveAfterXXStatements"; 
    
    /**
     * The color key for everthing in SQL code for which no other color is
     * specified.
     */
    String SQL_DEFAULT = "SQLEditor.DefaultColor";
    
    String DATASETRESULT_FORMAT_DATES = "DataSetResult.FormatDates";
    
    String DATASETRESULT_DATE_FORMAT = "DataSetResult.DateFormat";

    String WORD_WRAP = "SQLEditor.AutoWrap";
}
