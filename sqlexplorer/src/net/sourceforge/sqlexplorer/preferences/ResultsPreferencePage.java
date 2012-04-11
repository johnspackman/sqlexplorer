package net.sourceforge.sqlexplorer.preferences;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

public class ResultsPreferencePage extends AbstractPreferencePage{
	
	public ResultsPreferencePage() {
        super(Messages.getString("Preferences.Results.Title"), GRID); 
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IConstants.CLEAR_RESULTS_ON_EXECUTE, Messages.getString("Preferences.SQLExplorer.ClearResultsOnExecute"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.USE_LONG_CAPTIONS_ON_RESULTS, Messages.getString("Preferences.SQLExplorer.UseLongCaptionsOnResults"), getFieldEditorParent()));
		addField(new BooleanFieldEditor(IConstants.RETRIEVE_BLOB_AS_HEX, Messages.getString("Preferences.SQLExplorer.RetrieveBinaryAsHex"), getFieldEditorParent()));
		addField(new StringFieldEditor(IConstants.BLOB_AS_STRING_CHARSET, Messages.getString("Preferences.SQLExplorer.BlobAsStringCharset"), getFieldEditorParent()));

		IntegerFieldEditor iEdit = new IntegerFieldEditor(IConstants.MAX_LENGTH_OF_LONG_DATA, Messages.getString("Preferences.SQLExplorer.MaxLengthLongData"), getFieldEditorParent());
		iEdit.setValidRange(0, 1000000);
		iEdit.setErrorMessage(Messages.getString("Accepted_Range_is_0_-_1000000"));
		
		addField(iEdit);
		
		addField(new BooleanFieldEditor(IConstants.DATASETRESULT_FORMAT_DATES, Messages.getString("Preferences.Results.FormatDates"), getFieldEditorParent()));

		StringFieldEditor sEdit;
		
		sEdit = new DateFormatFieldEditor(IConstants.DATASETRESULT_DATE_FORMAT, Messages.getString("Preferences.Results.DateFormat"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(false);
		sEdit.setErrorMessage(Messages.getString("Preferences.Results.InvalidDate.Error"));
		addField(sEdit);
		
		sEdit = new DateFormatFieldEditor(IConstants.DATASETRESULT_TIME_FORMAT, Messages.getString("Preferences.Results.TimeFormat"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(false);
		sEdit.setErrorMessage(Messages.getString("Preferences.Results.InvalidDate.Error"));
		addField(sEdit);

		sEdit = new DateFormatFieldEditor(IConstants.DATASETRESULT_DATE_TIME_FORMAT, Messages.getString("Preferences.Results.DateTimeFormat"), getFieldEditorParent());
		sEdit.setEmptyStringAllowed(false);
		sEdit.setErrorMessage(Messages.getString("Preferences.Results.InvalidDate.Error"));
		addField(sEdit);
		
	}
	
}
