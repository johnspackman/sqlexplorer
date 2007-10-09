package net.sourceforge.sqlexplorer.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.TextUtil;

/**
 * SQLHistoryElement represents a single entry in the SQLHistoryView.
 */
public class SQLHistoryElement {

	public static final String ELEMENT = "element";
    private static final String ALIAS = "alias";
    private static final String EXECUTION_COUNT = "execution-count";
    private static final String LAST_EXECUTION_TIME = "last-execution-time";
    private static final String USER_NAME = "user-name";
    
    private int _executionCount = 1;

    private String _formattedTime;

    private String _rawSQLString;

    private String _searchableString;

    private User user;

    private String _singleLineText;

    private long _time;

    private static SimpleDateFormat _dateFormatter = new SimpleDateFormat(
            SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DATASETRESULT_DATE_FORMAT));


    public SQLHistoryElement(String rawSQLString, User user) {
        _rawSQLString = rawSQLString;
        this.user = user;
        _time = System.currentTimeMillis();
        initialize();
    }

    public SQLHistoryElement(String rawSQLString, User user, String time, String executions) {
        _rawSQLString = rawSQLString;
        this.user = user;

        if (time != null && time.length() != 0) {
            _time = Long.parseLong(time);
        } else {
            _time = System.currentTimeMillis();
        }

        if (executions != null && executions.length() != 0) {
            _executionCount = Integer.parseInt(executions);
        } else {
            _executionCount = 1;
        }

        initialize();
    }

    /**
     * Constructor; loads from the specified Element, which was previously generated
     * by a call to describeAsXml()
     * @param root
     */
    public SQLHistoryElement(Element root) {
    	_executionCount = Integer.parseInt(root.attributeValue(EXECUTION_COUNT));
    	_time = Long.parseLong(root.attributeValue(LAST_EXECUTION_TIME));
    	String aliasName = root.attributeValue(ALIAS);
    	Alias alias = SQLExplorerPlugin.getDefault().getAliasManager().getAlias(aliasName);
    	String userName = root.attributeValue(USER_NAME);
    	user = alias.getUser(userName);
    	if (user == null) {
    		user = new User(userName, "");
    		alias.addUser(user);
    	}
    	_rawSQLString = root.getTextTrim();
    	initialize();
    }

    /**
     * Creates an Element which can be used to reconstruct this instance at a later date
     * @return
     */
    public Element describeAsXml() {
    	Element root = new DefaultElement(ELEMENT);
    	root.addAttribute(EXECUTION_COUNT, Integer.toString(_executionCount));
    	root.addAttribute(LAST_EXECUTION_TIME, Long.toString(_time));
    	root.addAttribute(ALIAS, user.getAlias().getName());
    	root.addAttribute(USER_NAME, user.getUserName());
    	root.setText(_rawSQLString);
    	return root;
    }


    /**
     * Check if the current element matches a given sql string
     * 
     * @param rawSQL original sql statement to compare too.
     * @return true rawSQL matches this element
     */
    public boolean equals(String rawSQL) {

        return TextUtil.removeLineBreaks(rawSQL).equals(_singleLineText);
    }


    /**
     * @return number of times this statement was executed
     */
    public int getExecutionCount() {

        return _executionCount;
    }


    public String getFormattedTime() {

        return _formattedTime;
    }


    /**
     * @return unformatted sql string
     */
    public String getRawSQLString() {

        return _rawSQLString;
    }


    public String getSearchableString() {

        return _searchableString;
    }


    public User getUser() {
		return user;
	}
    
    public String getSessionDescription() {
    	return user.getAlias().getName() + '/' + user.getUserName();
    }


	/**
     * Return all text without newline separators.
     */
    public String getSingleLineText() {

        return _singleLineText;
    }


    public long getTime() {

        return _time;
    }


    /**
     * increase execution count by 1 and reset the timestamp to the current
     * time.
     */
    public void increaseExecutionCount() {

        _executionCount++;
        _time = System.currentTimeMillis();
        initialize();
    }


    /**
     * initialize our search string immediately, this allows for very fast
     * searching in the history view
     */
    private void initialize() {

        _formattedTime = _dateFormatter.format(new Date(_time));
        _searchableString = (_rawSQLString + " " + user.getUserName() + " " + _formattedTime).toLowerCase();
        _singleLineText = TextUtil.removeLineBreaks(_rawSQLString);
    }


    public void setUser(User user) {
		this.user = user;
        initialize();
	}
}
