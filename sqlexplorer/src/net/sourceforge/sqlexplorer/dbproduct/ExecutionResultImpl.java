/**
 * 
 */
package net.sourceforge.sqlexplorer.dbproduct;

import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbproduct.DatabaseProduct.ExecutionResults;
import net.sourceforge.sqlexplorer.parsers.NamedParameter;

public final class ExecutionResultImpl implements ExecutionResults {
	
	// Current state - IE, which set of results we're currently looking for
	private enum State {
		PRIMARY_RESULTS,		// We're providing the main results, from Statement.getResults()
		SECONDARY_RESULTS,		// We're providing resultsets from Statement.getMoreResults()
		PARAMETER_RESULTS,		// We're returning resultsets from output parameters
		OUTPUT_PARAMETERS,		// We're returning a fake result set listing output parameters
		CLOSED					// All done
	}
	
	private State state = State.PRIMARY_RESULTS;
	private AbstractDatabaseProduct product;
	private CallableStatement stmt;
	private LinkedList<NamedParameter> parameters;
	private int paramColumnIndex;
	private Iterator<NamedParameter> paramIter;
	private int updateCount;
	private ResultSet currentResultSet;

	public ExecutionResultImpl(AbstractDatabaseProduct product, CallableStatement stmt, LinkedList<NamedParameter> parameters) throws SQLException {
		super();
		this.product = product;
		this.stmt = stmt;
		this.parameters = parameters;
		
		if (!stmt.execute())
			state = State.SECONDARY_RESULTS;
	}

	public DataSet nextDataSet() throws SQLException {
		// Close the current one
		if (currentResultSet != null) {
			currentResultSet.close();
			currentResultSet = null;
		}

		// Anything more to do?
		if (state == State.CLOSED)
			return null;
		
		// Get the first set
		if (state == State.PRIMARY_RESULTS) {
			currentResultSet = stmt.getResultSet();
			state = State.SECONDARY_RESULTS;
			if (currentResultSet != null)
				return new DataSet(currentResultSet, null);
		}
		
		// While we have more secondary results (i.e. those that come directly from Statement but after the first getResults())
		while (state == State.SECONDARY_RESULTS) {
			if (stmt.getMoreResults())
				currentResultSet = stmt.getResultSet();
			else {
				int updateCount = stmt.getUpdateCount();
				if (updateCount != -1)
					this.updateCount += updateCount;
				else
					state = State.PARAMETER_RESULTS;
			}
		}
		
		// Got one? Then exit
		if (currentResultSet != null) {
			this.updateCount += stmt.getUpdateCount();
			return new DataSet(currentResultSet, null);
		}
		
		// Look for output parameters which return resultsets
		if (state == State.PARAMETER_RESULTS && parameters != null) {
			if (paramIter == null) {
				paramIter = parameters.iterator();
				paramColumnIndex = 1;
			}
			while (paramIter.hasNext()) {
				NamedParameter param = paramIter.next();
				if (param.getDataType() == NamedParameter.DataType.CURSOR)
					currentResultSet = product.getResultSet(stmt, param, paramColumnIndex);
				paramColumnIndex++;
				if (currentResultSet != null)
					return new DataSet(Messages.getString("DataSet.Cursor") + ' ' + param.getName(), currentResultSet, null);
			}
		}

		// Generate a dataset for output parameters
		state = State.CLOSED;
		if (parameters == null)
			return null;
		LinkedHashSet<NamedParameter> params = new LinkedHashSet<NamedParameter>();
		for (NamedParameter param : parameters)
			if (param.getDataType() != NamedParameter.DataType.CURSOR && param.isOutput()) {
				if (!params.contains(param))
					params.add(param);
			}
		if (params.isEmpty())
			return null;
		Comparable[][] rows = new Comparable[params.size()][2];
		int columnIndex = 1;
		int rowIndex = 0;
		for (NamedParameter param : params) {
			Comparable[] row = rows[rowIndex++];
			row[0] = param.getName();
			row[1] = stmt.getString(columnIndex);
			columnIndex++;
		}
		return new DataSet(Messages.getString("DataSet.Parameters"), new String[] { 
				Messages.getString("SQLExecution.ParameterName"),
				Messages.getString("SQLExecution.ParameterValue")
			}, rows);
	}
	
	public void close() throws SQLException {
		try {
			stmt.close();
		} catch(SQLException e) {
			// Nothing
		}
		if (currentResultSet != null)
			currentResultSet.close();
	}

	public int getUpdateCount() throws SQLException {
		return updateCount;
	}
}