package fr.openwide.core.imports.excel.event;

import fr.openwide.core.commons.util.report.BatchReport;
import fr.openwide.core.imports.excel.event.exception.ExcelImportContentException;
import fr.openwide.core.imports.excel.event.exception.ExcelImportHeaderLabelMappingException;
import fr.openwide.core.imports.excel.event.exception.ExcelImportMappingException;
import fr.openwide.core.imports.excel.event.formatter.IExcelImportEventMessageFormatter;
import fr.openwide.core.imports.excel.event.formatter.RawExcelImportEventMessageFormatter;
import fr.openwide.core.imports.excel.location.ExcelImportLocation;

/**
 * A event handler that will add errors to a {@link BatchReport} and delay exception throwing until {@link #checkNoErrorOccurred()} is called.
 * <p><strong>CAUTION:</string> when using this event handler, {@link #checkNoErrorOccurred()} MUST be called before attempting any action based on the imported data.
 * Doing otherwise will result in corrupted data.
 */
public class BatchReportExcelImportEventHandler implements IExcelImportEventHandler {
	
	private final IExcelImportEventMessageFormatter formatter;
	
	private final BatchReport batchReport;
	
	private boolean hasError = false;
	
	public BatchReportExcelImportEventHandler(BatchReport batchReport) {
		this(new RawExcelImportEventMessageFormatter(), batchReport);
	}
	
	public BatchReportExcelImportEventHandler(IExcelImportEventMessageFormatter formatter, BatchReport batchReport) {
		this.formatter = formatter;
		this.batchReport = batchReport;
	}

	@Override
	public void headerLabelMappingError(String expectedHeaderLabel, int indexAmongMatchedColumns, ExcelImportLocation location) throws ExcelImportHeaderLabelMappingException {
		this.hasError = true;
		batchReport.error(formatter.formatHeaderLabelMappingErrorMessage(expectedHeaderLabel, indexAmongMatchedColumns, location));
		throw new ExcelImportHeaderLabelMappingException(
				"Could not map column '" + expectedHeaderLabel + "' (index among matched columns : " + indexAmongMatchedColumns + ")",
				expectedHeaderLabel, location
		);
	}

	@Override
	public void checkNoMappingErrorOccurred() throws ExcelImportMappingException {
		if (hasError) {
			this.hasError = false;
			throw new ExcelImportMappingException("An error occurred; see the error report for more details");
		}
	}

	@Override
	public void error(String error, ExcelImportLocation location) throws ExcelImportContentException {
		this.hasError = true;
		batchReport.error(formatter.formatErrorMessage(error, location));
	}

	@Override
	public void missingValue(String error, ExcelImportLocation location) throws ExcelImportContentException {
		this.hasError = true;
		batchReport.error(formatter.formatErrorMessage(error, location));
	}

	@Override
	public void checkNoErrorOccurred() throws ExcelImportContentException {
		if (hasError) {
			this.hasError = false;
			throw new ExcelImportContentException("An error occurred; see the error report for more details");
		}
	}
	
	@Override
	public void resetErrors() {
		this.hasError = false;
	}

}