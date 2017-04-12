package de.metas.ui.web.process;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.ad.dao.IQueryFilter;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.RecordZoomWindowFinder;
import org.adempiere.util.Services;
import org.adempiere.util.lang.IAutoCloseable;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.util.Env;
import org.compiere.util.MimeType;
import org.compiere.util.Util;
import org.slf4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import de.metas.adempiere.report.jasper.OutputType;
import de.metas.logging.LogManager;
import de.metas.printing.esb.base.util.Check;
import de.metas.process.JavaProcess;
import de.metas.process.ProcessExecutionResult;
import de.metas.process.ProcessExecutionResult.RecordsToOpen;
import de.metas.process.ProcessExecutor;
import de.metas.process.ProcessInfo;
import de.metas.ui.web.process.ProcessInstanceResult.OpenReportAction;
import de.metas.ui.web.process.ProcessInstanceResult.OpenSingleDocument;
import de.metas.ui.web.process.ProcessInstanceResult.OpenViewAction;
import de.metas.ui.web.process.descriptor.ProcessDescriptor;
import de.metas.ui.web.process.exceptions.ProcessExecutionException;
import de.metas.ui.web.view.DocumentViewCreateRequest;
import de.metas.ui.web.view.IDocumentViewSelection;
import de.metas.ui.web.view.IDocumentViewsRepository;
import de.metas.ui.web.view.json.JSONViewDataType;
import de.metas.ui.web.window.datatypes.DocumentId;
import de.metas.ui.web.window.datatypes.DocumentPath;
import de.metas.ui.web.window.datatypes.DocumentType;
import de.metas.ui.web.window.datatypes.LookupValuesList;
import de.metas.ui.web.window.datatypes.json.JSONDocumentChangedEvent;
import de.metas.ui.web.window.model.Document;
import de.metas.ui.web.window.model.Document.CopyMode;
import de.metas.ui.web.window.model.DocumentSaveStatus;
import de.metas.ui.web.window.model.IDocumentChangesCollector.ReasonSupplier;
import de.metas.ui.web.window.model.IDocumentFieldView;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * WEBUI Process instance.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public final class ProcessInstance
{
	private static final transient Logger logger = LogManager.getLogger(ProcessInstance.class);

	public static final String PARAM_ViewId = "$WEBUI_ViewId";
	public static final String PARAM_ViewSelectedIds = "$WEBUI_ViewSelectedIds";

	public static final Builder builder()
	{
		return new Builder();
	}

	private final DocumentId adPInstanceId;

	private final ProcessDescriptor processDescriptor;
	private final Document parameters;
	private final Object processClassInstance;

	private final IDocumentViewsRepository viewsRepo;
	private final String viewId;
	private final Set<DocumentId> viewSelectedDocumentIds;

	private boolean executed = false;
	private ProcessInstanceResult executionResult;

	private final ReentrantReadWriteLock readwriteLock;

	/** New instance constructor */
	private ProcessInstance(final Builder builder)
	{
		processDescriptor = builder.processDescriptor;
		adPInstanceId = builder.adPInstanceId;
		parameters = builder.parameters;
		processClassInstance = builder.processClassInstance;

		viewsRepo = builder.viewsRepo;
		viewId = builder.viewId;
		viewSelectedDocumentIds = builder.viewSelectedDocumentIds == null ? ImmutableSet.of() : ImmutableSet.copyOf(builder.viewSelectedDocumentIds);

		executed = false;
		executionResult = null;

		readwriteLock = new ReentrantReadWriteLock();
	}

	/** Copy constructor */
	private ProcessInstance(final ProcessInstance from, final CopyMode copyMode)
	{
		super();

		adPInstanceId = from.adPInstanceId;

		processDescriptor = from.processDescriptor;
		parameters = from.parameters.copy(copyMode);
		processClassInstance = from.processClassInstance;

		viewsRepo = from.viewsRepo;
		viewId = from.viewId;
		viewSelectedDocumentIds = from.viewSelectedDocumentIds;

		executed = from.executed;
		executionResult = from.executionResult;

		readwriteLock = from.readwriteLock; // always share
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("AD_PInstance_ID", adPInstanceId)
				.add("executed", "executed")
				.add("executionResult", executionResult)
				.add("processDescriptor", processDescriptor)
				.toString();
	}

	public void destroy()
	{
		// TODO Auto-generated method stub

	}

	public ProcessDescriptor getDescriptor()
	{
		return processDescriptor;
	}

	public DocumentId getAD_PInstance_ID()
	{
		return adPInstanceId;
	}

	private Document getParametersDocument()
	{
		return parameters;
	}

	public Collection<IDocumentFieldView> getParameters()
	{
		return parameters.getFieldViews();
	}

	public ProcessInstance copy(final CopyMode copyMode)
	{
		return new ProcessInstance(this, copyMode);
	}

	public IAutoCloseable activate()
	{
		return JavaProcess.temporaryChangeCurrentInstance(processClassInstance);
	}

	public LookupValuesList getParameterLookupValues(final String parameterName)
	{
		return parameters.getFieldLookupValues(parameterName);
	}

	public LookupValuesList getParameterLookupValuesForQuery(final String parameterName, final String query)
	{
		return parameters.getFieldLookupValuesForQuery(parameterName, query);
	}

	public void processParameterValueChanges(final List<JSONDocumentChangedEvent> events, final ReasonSupplier reason)
	{
		assertNotExecuted();
		parameters.processValueChanges(events, reason);
	}

	/**
	 * @return execution result or throws exception if the process was not already executed
	 */
	public ProcessInstanceResult getExecutionResult()
	{
		final ProcessInstanceResult executionResult = this.executionResult;
		if (executionResult == null)
		{
			throw new AdempiereException("Process instance does not have an execution result yet: " + this);
		}
		return executionResult;
	}

	private boolean isExecuted()
	{
		return executed;
	}

	/* package */ final void assertNotExecuted()
	{
		if (isExecuted())
		{
			throw new AdempiereException("Process already executed");
		}
	}

	public ProcessInstanceResult startProcess()
	{
		assertNotExecuted();

		//
		// Make sure it's saved in database
		if (!saveIfValidAndHasChanges(true))
		{
			// shall not happen because the method throws the exception in case of failure
			throw new ProcessExecutionException("Instance could not be saved because it's not valid");
		}

		//
		executionResult = executeADProcess(getAD_PInstance_ID(), getDescriptor(), viewsRepo);
		if (executionResult.isSuccess())
		{
			executed = false;
		}
		return executionResult;
	}

	private static final ProcessInstanceResult executeADProcess(final DocumentId adPInstanceId, final ProcessDescriptor processDescriptor, IDocumentViewsRepository viewsRepo)
	{
		//
		// Create the process info and execute the process synchronously
		final Properties ctx = Env.getCtx(); // We assume the right context was already used when the process was loaded
		final String adLanguage = Env.getAD_Language(ctx);
		final String name = processDescriptor.getCaption(adLanguage);
		final ProcessExecutor processExecutor = ProcessInfo.builder()
				.setCtx(ctx)
				.setCreateTemporaryCtx()
				.setAD_PInstance_ID(adPInstanceId.toInt())
				.setTitle(name)
				.setPrintPreview(true)
				.setJRDesiredOutputType(OutputType.PDF)
				//
				// Execute the process/report
				.buildAndPrepareExecution()
				.executeSync();
		final ProcessExecutionResult processExecutionResult = processExecutor.getResult();

		//
		// Build and return the execution result
		{
			String summary = processExecutionResult.getSummary();
			if (Check.isEmpty(summary, true) || JavaProcess.MSG_OK.equals(summary))
			{
				// hide summary if empty or MSG_OK (which is the most used non-message)
				summary = null;
			}

			final ProcessInstanceResult.Builder resultBuilder = ProcessInstanceResult.builder()
					.setAD_PInstance_ID(processExecutionResult.getAD_PInstance_ID())
					.setSummary(summary)
					.setError(processExecutionResult.isError());

			//
			// Create result post process actions
			{
				final File reportTempFile = saveReportToDiskIfAny(processExecutionResult);
				final RecordsToOpen recordsToOpen = processExecutionResult.getRecordsToOpen();

				//
				// Result: report
				if (reportTempFile != null)
				{
					resultBuilder.setAction(OpenReportAction.builder()
							.filename(processExecutionResult.getReportFilename())
							.contentType(processExecutionResult.getReportContentType())
							.tempFile(reportTempFile)
							.build());
				}
				//
				// View
				else if (recordsToOpen != null && recordsToOpen.isGridView())
				{
					final DocumentViewCreateRequest viewRequest = createViewRequest(processExecutor.getProcessInfo(), recordsToOpen);
					final IDocumentViewSelection view = viewsRepo.createView(viewRequest);
					resultBuilder.setAction(OpenViewAction.builder()
							.windowId(view.getAD_Window_ID())
							.viewId(view.getViewId())
							.build());
				}
				//
				// Single document
				else if (recordsToOpen != null && !recordsToOpen.isGridView())
				{
					final DocumentPath documentPath = extractSingleDocumentPath(recordsToOpen);
					resultBuilder.setAction(OpenSingleDocument.builder()
							.documentPath(documentPath)
							.build());
				}
			}

			final ProcessInstanceResult result = resultBuilder.build();
			return result;
		}

	}

	private static final File saveReportToDiskIfAny(final ProcessExecutionResult processExecutionResult)
	{
		//
		// If we are not dealing with a report, stop here
		final byte[] reportData = processExecutionResult.getReportData();
		if (reportData == null || reportData.length <= 0)
		{
			return null;
		}

		//
		// Create report temporary file
		File reportFile = null;
		try
		{
			final String reportFilePrefix = "report_" + processExecutionResult.getAD_PInstance_ID() + "_";

			final String reportContentType = processExecutionResult.getReportContentType();
			final String reportFileExtension = MimeType.getExtensionByType(reportContentType);
			final String reportFileSuffix = Check.isEmpty(reportFileExtension, true) ? "" : "." + reportFileExtension.trim();
			reportFile = File.createTempFile(reportFilePrefix, reportFileSuffix);
		}
		catch (final IOException e)
		{
			throw new AdempiereException("Failed creating report temporary file " + reportFile);
		}

		//
		// Write report data to our temporary report file
		Util.writeBytes(reportFile, reportData);

		return reportFile;
	}

	private static final DocumentViewCreateRequest createViewRequest(final ProcessInfo processInfo, final RecordsToOpen recordsToOpen)
	{
		final List<TableRecordReference> recordRefs = recordsToOpen.getRecords();
		if (recordRefs.isEmpty())
		{
			return null; // shall not happen
		}

		final int adWindowId_Override = recordsToOpen.getAD_Window_ID(); // optional

		//
		// Create view create request builders from current records
		final Map<Integer, DocumentViewCreateRequest.Builder> viewRequestBuilders = new HashMap<>();
		for (final TableRecordReference recordRef : recordRefs)
		{
			final int recordWindowId = adWindowId_Override > 0 ? adWindowId_Override : RecordZoomWindowFinder.findAD_Window_ID(recordRef);
			final DocumentViewCreateRequest.Builder viewRequestBuilder = viewRequestBuilders.computeIfAbsent(recordWindowId, key -> DocumentViewCreateRequest.builder(recordWindowId, JSONViewDataType.grid));

			viewRequestBuilder.addFilterOnlyId(recordRef.getRecord_ID());
		}
		// If there is no view create request builder there stop here (shall not happen)
		if (viewRequestBuilders.isEmpty())
		{
			return null;
		}

		//
		// Create the view create request from first builder that we have.
		if (viewRequestBuilders.size() > 1)
		{
			logger.warn("More than one views to be created found for {}. Creating only the first view.", recordRefs);
		}
		final DocumentViewCreateRequest viewRequest = viewRequestBuilders.values().iterator().next()
				.setReferencingDocumentPaths(extractReferencingDocumentPaths(processInfo))
				.build();
		return viewRequest;
	}

	private static final Set<DocumentPath> extractReferencingDocumentPaths(final ProcessInfo processInfo)
	{
		final String tableName = processInfo.getTableNameOrNull();
		if (tableName == null)
		{
			return ImmutableSet.of();
		}
		final TableRecordReference sourceRecordRef = processInfo.getRecordRefOrNull();

		final IQueryFilter<Object> selectionQueryFilter = processInfo.getQueryFilterOrElse(null);
		if (selectionQueryFilter != null)
		{
			// TODO: hardcoded limit. It shall be much more obvious!!!
			final int maxRecordAllowedToSelect = 200;
			final List<Integer> recordIds = Services.get(IQueryBL.class).createQueryBuilder(tableName, Env.getCtx(), ITrx.TRXNAME_ThreadInherited)
					.filter(selectionQueryFilter)
					.setLimit(maxRecordAllowedToSelect + 1)
					.create()
					.listIds();
			if (recordIds.isEmpty())
			{
				return ImmutableSet.of();
			}
			else if (recordIds.size() > maxRecordAllowedToSelect)
			{
				throw new AdempiereException("Selecting more than " + maxRecordAllowedToSelect + " records is not allowed");
			}

			final TableRecordReference firstRecordRef = TableRecordReference.of(tableName, recordIds.get(0));
			final int adWindowId = RecordZoomWindowFinder.findAD_Window_ID(firstRecordRef); // assume all records are from same window
			return recordIds.stream()
					.map(recordId -> DocumentPath.rootDocumentPath(DocumentType.Window, adWindowId, recordId))
					.collect(ImmutableSet.toImmutableSet());
		}
		else if (sourceRecordRef != null)
		{
			final int adWindowId = RecordZoomWindowFinder.findAD_Window_ID(sourceRecordRef);
			final DocumentPath documentPath = DocumentPath.rootDocumentPath(DocumentType.Window, adWindowId, sourceRecordRef.getRecord_ID());
			return ImmutableSet.of(documentPath);
		}
		else
		{
			return ImmutableSet.of();
		}
	}

	private static final DocumentPath extractSingleDocumentPath(final RecordsToOpen recordsToOpen)
	{
		final TableRecordReference recordRef = recordsToOpen.getSingleRecord();
		final int documentId = recordRef.getRecord_ID();

		int adWindowId = recordsToOpen.getAD_Window_ID();
		if (adWindowId <= 0)
		{
			adWindowId = RecordZoomWindowFinder.findAD_Window_ID(recordRef);
		}

		return DocumentPath.rootDocumentPath(DocumentType.Window, adWindowId, documentId);
	}

	/**
	 * Save
	 *
	 * @param throwEx <code>true</code> if we shall throw an exception if document it's not valid or it's not saved
	 * @return true if valid and saved
	 */
	public boolean saveIfValidAndHasChanges(final boolean throwEx)
	{
		final Document parametersDocument = getParametersDocument();
		final DocumentSaveStatus parametersSaveStatus = parametersDocument.saveIfValidAndHasChanges();
		final boolean saved = parametersSaveStatus.isSaved();
		if (!saved && throwEx)
		{
			throw new ProcessExecutionException(parametersSaveStatus.getReason());
		}

		return saved;
	}

	/* package */ final IAutoCloseable lockForReading()
	{
		final ReadLock readLock = readwriteLock.readLock();
		logger.debug("Acquiring read lock for {}: {}", this, readLock);
		readLock.lock();
		logger.debug("Acquired read lock for {}: {}", this, readLock);

		return () -> {
			readLock.unlock();
			logger.debug("Released read lock for {}: {}", this, readLock);
		};
	}

	/* package */ final IAutoCloseable lockForWriting()
	{
		final WriteLock writeLock = readwriteLock.writeLock();
		logger.debug("Acquiring write lock for {}: {}", this, writeLock);
		writeLock.lock();
		logger.debug("Acquired write lock for {}: {}", this, writeLock);

		return () -> {
			writeLock.unlock();
			logger.debug("Released write lock for {}: {}", this, writeLock);
		};
	}

	//
	//
	// Builder
	//
	//
	public static class Builder
	{
		private ProcessDescriptor processDescriptor;
		private DocumentId adPInstanceId;
		private Document parameters;

		private IDocumentViewsRepository viewsRepo;
		private String viewId;
		private Set<DocumentId> viewSelectedDocumentIds;
		private Object processClassInstance;

		private Builder()
		{
		}

		public ProcessInstance build()
		{

			return new ProcessInstance(this);
		}

		public Builder setProcessDescriptor(final ProcessDescriptor processDescriptor)
		{
			this.processDescriptor = processDescriptor;
			return this;
		}

		public Builder setAD_PInstance_ID(final DocumentId adPInstanceId)
		{
			this.adPInstanceId = adPInstanceId;
			return this;
		}

		public Builder setParameters(final Document parameters)
		{
			this.parameters = parameters;
			return this;
		}

		public Builder setViewsRepo(final IDocumentViewsRepository viewsRepo)
		{
			this.viewsRepo = viewsRepo;
			return this;
		}

		public Builder setView(final String viewId, final Set<DocumentId> selectedDocumentIds)
		{
			this.viewId = viewId;
			viewSelectedDocumentIds = selectedDocumentIds;
			return this;
		}

		public Builder setProcessClassInstance(Object processClassInstance)
		{
			this.processClassInstance = processClassInstance;
			return this;
		}
	}

}
