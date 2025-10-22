/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.importer.div.importers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.service.holder.LabImportUtilHolder;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.hl7.HL7PatientResolver;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.v26.HL7Constants;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HL7Parser {
	private static final Logger logger = LoggerFactory.getLogger(HL7Parser.class);
	public static final String CFG_IMPORT_ENCDATA = "hl7Parser/importencdata";
	public static final String CFG_IMPORT_ENCDATA_CATEGORY = "hl7Parser/importencdataCategory";

	private HL7PatientResolver patientResolver;

	public String myLab = "?"; //$NON-NLS-1$

	public HL7Reader hl7Reader;
	private IPatient pat;
	private TimeTool date;
	private TimeTool commentDate;
	private ILabImportUtil labImportUtil;
	private ILabContactResolver labContactResolver;
	private ImportHandler importHandler;
	private boolean importEncapsulatedData;

	public HL7Parser(String mylab, HL7PatientResolver patientResolver, ImportHandler importHandler,
			ILabContactResolver labContactResolver, boolean importEncapsulatedData) {
		this(mylab, patientResolver, LabImportUtilHolder.get(), importHandler, labContactResolver,
				importEncapsulatedData);
	}

	public HL7Parser(String mylab, HL7PatientResolver patientResolver, ILabImportUtil labImportUtil,
			ImportHandler importHandler, ILabContactResolver labContactResolver, boolean importEncapsulatedData) {
		this.myLab = mylab;
		this.patientResolver = patientResolver;
		this.labImportUtil = labImportUtil;
		this.importHandler = importHandler;
		this.labContactResolver = labContactResolver;
		this.importEncapsulatedData = importEncapsulatedData;
	}

	public Result<Object> parse(final HL7Reader hl7Reader, boolean createPatientIfNotFound) {
		return parse(hl7Reader, null, createPatientIfNotFound);
	}

	public Result<Object> parse(final HL7Reader hl7Reader, ILabItemResolver labItemResolver,
			boolean createPatientIfNotFound) {
		return parse(hl7Reader, null, null, createPatientIfNotFound);
	}

	/**
	 * Parser the content using the HL7Reader and create or merge LabResults.
	 *
	 * @param hl7Reader
	 * @param labItemResolver
	 * @param labContactResolver
	 * @param createPatientIfNotFound
	 *
	 * @return the orderId of the import
	 */
	public Result<Object> parse(final HL7Reader hl7Reader, ILabItemResolver labItemResolver,
			ILabContactResolver labContactResolver, boolean createPatientIfNotFound) {
		final TimeTool transmissionTime = new TimeTool();
		String orderId = StringUtils.EMPTY;

		ILabContactResolver labResolver = labContactResolver != null ? labContactResolver : this.labContactResolver;

		// assure resolvers are initialized
		if (labResolver == null) {
			throw new IllegalArgumentException("labContactResolver must not be null");
		}
		if (labItemResolver == null) {
			labItemResolver = new DefaultLabItemResolver();
		}

		try {
			ILaboratory labor = labResolver.getLabContact(myLab, hl7Reader.getSender());
			// stop here if lab does not exist
			if (labor == null) {
				logger.warn("Exiting parsing process as labor is null");
				return new Result<>(SEVERITY.ERROR, 2, "Labor contact is null", StringUtils.EMPTY, true);
			}

			ObservationMessage obsKIMessage = hl7Reader.readObservation(patientResolver, createPatientIfNotFound);
			if (obsKIMessage != null && obsKIMessage.getObservations().stream()
					.anyMatch(o -> o instanceof ch.elexis.hl7.model.TextData td
							&& "AI Consultation".equalsIgnoreCase(td.getGroup()))) {
				logger.info("Detected KI Consultation – skipping lab import (no LabResultData will be created).");
				return new Result<>(SEVERITY.OK, 0, "KI consultation imported into encounter only", null, false);
			}

			ObservationMessage obsMessage = hl7Reader.readObservation(patientResolver, createPatientIfNotFound);
			IPatient patient = hl7Reader.getPatient();
			if (patient == null) {
				return new Result<>(SEVERITY.ERROR, 2, Messages.HL7_PatientNotInDatabase, obsMessage.getPatientId(),
						true);
			}

			pat = labImportUtil.loadCoreModel(patient.getId(), IPatient.class).orElse(null);
			if (pat == null) {
				return new Result<>(SEVERITY.ERROR, 2, Messages.HL7_PatientNotInDatabase, obsMessage.getPatientId(),
						true);
			}

			OrcMessage orcMessage = hl7Reader.getOrcMessage();
			if (orcMessage != null) {
				if (orcMessage.getNames().isEmpty()) {
					logger.info("Cannot parse mandant name for ORC message");
				}
			}

			int number = 0;
			List<TransientLabResult> results = new ArrayList<>();
			List<IValueType> observations = obsMessage.getObservations();
			initCommentDate(obsMessage);

			TimeTool obrDateTime = null;
			TimeTool obxDateTime = null;

			for (IValueType iValueType : observations) {
				if (iValueType instanceof LabResultData) {
					LabResultData hl7LabResult = (LabResultData) iValueType;
					if (hl7LabResult.getDate() == null) {
						if (obsMessage.getDateTimeOfMessage() != null) {
							hl7LabResult.setDate(obsMessage.getDateTimeOfMessage());
						} else {
							hl7LabResult.setDate(transmissionTime.getTime());
						}
					}
					date = new TimeTool(hl7LabResult.getDate());
					if (hl7LabResult.getOBRDateTime() == null) {
						hl7LabResult.setOBRDateTime(transmissionTime.toString(TimeTool.TIMESTAMP));
					}
					obrDateTime = new TimeTool(hl7LabResult.getOBRDateTime());
					obxDateTime = new TimeTool(hl7LabResult.getDate());

					ILabItem labItem = labImportUtil.getLabItem(hl7LabResult.getCode(), labor);
					if (labItem == null) {
						LabItemTyp typ = LabItemTyp.NUMERIC;
						if (hl7LabResult.isNumeric() == false) {
							typ = LabItemTyp.TEXT;
						}
						String refMale = StringUtils.EMPTY;
						String refFemale = StringUtils.EMPTY;
						if (pat.getGender().equals(Gender.MALE)) {
							refMale = hl7LabResult.getRange();
						} else {
							refFemale = hl7LabResult.getRange();
						}

						labItem = labImportUtil.createLabItem(hl7LabResult.getCode(), hl7LabResult.getName(), labor,
								refMale, refFemale, hl7LabResult.getUnit(), typ,
								labItemResolver.getTestGroupName(hl7LabResult),
								labItemResolver.getNextTestGroupSequence(hl7LabResult));
						logger.debug("LabItem created [{}]", labItem);
					}

					boolean importAsLongText = (hl7LabResult.isFormatedText() || hl7LabResult.isPlainText());
					if (importAsLongText) {
						if (hl7LabResult.isNumeric()) {
							importAsLongText = false;
						}
					}
					if (importAsLongText) {
						if (hl7LabResult.getValue().length() < 20) {
							importAsLongText = false;
						}
					}
					if (!importAsLongText) {
						if (hl7LabResult.getValue().length() > 255) {
							importAsLongText = true;
						}
					}
					if (importAsLongText) {

						Integer flag = null;
						if (hl7LabResult.getFlag() != null) {
							flag = (hl7LabResult.getFlag().booleanValue()) ? LabResultConstants.PATHOLOGIC : 0;
						}
						TransientLabResult importedResult = new TransientLabResult.Builder(pat, labor, labItem, "text")
								.date(obrDateTime)
								.comment(StringTool.unNull(hl7LabResult.getValue()) + StringUtils.LF
										+ StringTool.unNull(hl7LabResult.getComment()))
								.flags(flag).rawAbnormalFlags(hl7LabResult.getRawAbnormalFlag())
								.unit(hl7LabResult.getUnit()).ref(hl7LabResult.getRange()).observationTime(obrDateTime)
								.analyseTime(obxDateTime).transmissionTime(transmissionTime).orcMessage(orcMessage)
								.subId(hl7LabResult.getSubId()).build(labImportUtil);
						results.add(importedResult);
						logger.debug(importedResult.toString());
					} else {
						Integer flag = null;
						if (hl7LabResult.getFlag() != null) {
							flag = (hl7LabResult.getFlag().booleanValue()) ? LabResultConstants.PATHOLOGIC : 0;
						}
						TransientLabResult importedResult = new TransientLabResult.Builder(pat, labor, labItem,
								hl7LabResult.getValue()).date(obrDateTime)
										.comment(StringTool.unNull(hl7LabResult.getComment())).flags(flag)
										.rawAbnormalFlags(hl7LabResult.getRawAbnormalFlag())
										.unit(hl7LabResult.getUnit()).ref(hl7LabResult.getRange())
										.observationTime(obrDateTime).analyseTime(obxDateTime)
										.transmissionTime(transmissionTime).orcMessage(orcMessage)
										.subId(hl7LabResult.getSubId()).build(labImportUtil);
						results.add(importedResult);
						logger.debug(importedResult.toString());
					}
				} else if (iValueType instanceof EncapsulatedData) {
					if (importEncapsulatedData) {
						EncapsulatedData hl7EncData = (EncapsulatedData) iValueType;
						if (hl7EncData.getDate() == null) {
							if (obsMessage.getDateTimeOfMessage() != null) {
								hl7EncData.setDate(obsMessage.getDateTimeOfMessage());
							} else {
								hl7EncData.setDate(transmissionTime.getTime());
							}
						}
						date = new TimeTool(hl7EncData.getDate());
						String dateString = date.toString(TimeTool.DATETIME_XML).replace(":", StringUtils.EMPTY);
						dateString = dateString.replace("-", StringUtils.EMPTY);
						String title = "Lab-" + dateString + "-" + hl7EncData.getSequence();

						String fileType = StringUtils.EMPTY;
						if (hl7EncData.getName().contains("/")) {
							String[] split = hl7EncData.getName().split("/");
							if (split.length == 2) {
								fileType = split[1];
								title = title + "." + fileType;
							}
						}

						// get or create LabItem and create labresult
						String liShort = "doc";
						String liName = "Dokument";

						ILabItem labItem = labImportUtil.getDocumentLabItem(liShort, liName, labor).orElse(null);
						if (labItem == null) {
							labItem = labImportUtil.createLabItem(liShort, liName, labor, StringUtils.EMPTY,
									StringUtils.EMPTY, fileType, LabItemTyp.DOCUMENT, hl7EncData.getGroup(),
									StringUtils.EMPTY);
						}

						TransientLabResult importedResult = new TransientLabResult.Builder(pat, labor, labItem, title)
								.date(date).orcMessage(orcMessage).build(labImportUtil);
						results.add(importedResult);

						// create document manager (omnivore) entry
						labImportUtil.createDocumentManagerEntry(title, labor.getCode(), hl7EncData.getData(),
								hl7EncData.getName(), date, pat);
					}
				}

				if (iValueType instanceof TextData) {
					TextData hl7TextData = (TextData) iValueType;

					// add comments as a LabResult
					if (hl7TextData.getName().equals(HL7Constants.COMMENT_NAME)) {
						if (hl7TextData.getDate() == null) {
							hl7TextData.setDate(commentDate.getTime());
						}
						TimeTool commentsDate = new TimeTool(hl7TextData.getDate());

						// find, or create LabItem
						ILabItem labItem = labImportUtil
								.getLabItem(HL7Constants.COMMENT_CODE, HL7Constants.COMMENT_NAME, labor).orElse(null);
						if (labItem == null) {
							labItem = labImportUtil.createLabItem(HL7Constants.COMMENT_CODE, HL7Constants.COMMENT_NAME,
									labor, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, LabItemTyp.TEXT,
									HL7Constants.COMMENT_GROUP, Integer.toString(number));
						}

						// add LabResult, only add comments not yet existing
						List<ILabResult> existingResults = labImportUtil.getLabResults(pat, labItem, commentsDate, null,
								null);
						if (existingResults.isEmpty()) {
							StringBuilder comment = new StringBuilder();
							if (hl7TextData.getText() != null) {
								comment.append(hl7TextData.getText());
							}
							if (hl7TextData.getComment() != null) {
								comment.append(hl7TextData.getComment());
							}
							TransientLabResult commentsResult = new TransientLabResult.Builder(pat, labor, labItem,
									"text").date(commentsDate).comment(comment.toString()).build(labImportUtil);
							results.add(commentsResult);
						}
						number++;
					}
				}
			}

			if (StringUtils.isNotBlank(obsMessage.getPatientNotesAndComments())) {
				ILabItem labItem = labImportUtil.getLabItem("NOTE", labor);
				if (labItem == null) {
					labItem = labImportUtil.createLabItem("NOTE", Messages.HL7Parser_LabItem_Note_Name, labor,
							StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, LabItemTyp.TEXT, "AA", "1");
					logger.debug("LabItem created [{}]", labItem);
				}

				TransientLabResult patientNoteAndComment = new TransientLabResult.Builder(pat, labor, labItem, "text")
						.comment(obsMessage.getPatientNotesAndComments()).date(obrDateTime).flags(0)
						.observationTime(obrDateTime).analyseTime(obxDateTime).orcMessage(orcMessage)
						.build(labImportUtil);
				results.add(patientNoteAndComment);
			}

			if (patientResolver.userResolved()) {
				results.forEach(r -> r.setUserResolved(Boolean.TRUE));
			}

			orderId = labImportUtil.importLabResults(results, importHandler);

		} catch (Exception e) {
			logger.error("Parsing HL7 failed", e);
			return new Result<>(SEVERITY.ERROR, 2, Messages.Core_Error_while_importing_HL7, e.getMessage(),
					true);
		}
		return new Result<>(SEVERITY.OK, 0, "OK", orderId, false); //$NON-NLS-1$
	}

	private void initCommentDate(ObservationMessage obsMessage) {
		if (obsMessage.getDateTimeOfTransaction() != null) {
			commentDate = new TimeTool(obsMessage.getDateTimeOfTransaction());
		} else if (obsMessage.getDateTimeOfMessage() != null) {
			commentDate = new TimeTool(obsMessage.getDateTimeOfMessage());
		} else {
			commentDate = new TimeTool();
		}
	}

	public Result<?> importFile(final File file, final File archiveDir, boolean bCreatePatientIfNotExists)
			throws IOException {
		return importFile(file, archiveDir, null, null, bCreatePatientIfNotExists);
	}

	public Result<?> importFile(final File file, final File archiveDir, ILabItemResolver labItemResolver,
			boolean bCreatePatientIfNotExists) throws IOException {
		return importFile(file, archiveDir, labItemResolver, null, bCreatePatientIfNotExists);
	}

	public Result<?> importFile(File hl7file, File archiveDir, ILabItemResolver labItemResolver,
			ILabContactResolver labContactResolver, boolean bCreatePatientIfNotExists) throws IOException {

		IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(hl7file);
		IVirtualFilesystemHandle archiveDirHandle = VirtualFilesystemServiceHolder.get().of(archiveDir);
		return importFile(fileHandle, archiveDirHandle, labItemResolver, labContactResolver, bCreatePatientIfNotExists);
	}

	/**
	 * @throws IOException
	 * @see HL7Parser#importFile(File, File, ILabItemResolver, ILabContactResolver,
	 *      boolean)
	 */
	public Result<?> importFile(final IVirtualFilesystemHandle file, final IVirtualFilesystemHandle archiveDir,
			boolean bCreatePatientIfNotExists) throws IOException {
		return importFile(file, archiveDir, null, bCreatePatientIfNotExists);
	}

	public Result<?> importFile(IVirtualFilesystemHandle hl7file, IVirtualFilesystemHandle archiveDir,
			ILabItemResolver labItemResolver, ILabContactResolver labContactResolver, boolean bCreatePatientIfNotExists)
			throws IOException {
		if (labContactResolver != null) {
			this.labContactResolver = labContactResolver;
		}
		return importFile(hl7file, archiveDir, labItemResolver, bCreatePatientIfNotExists);
	}

	/**
	 * Import the given HL7 file. Optionally, move the file into the given archive
	 * directory
	 *
	 * @param file                      the file to be imported (full path)
	 * @param archiveDir                a directory where the file should be moved
	 *                                  to on success, or null if it should not be
	 *                                  moved.
	 * @param labItemResolver           implementation of the
	 *                                  {@link ILabItemResolver}, or null if
	 *                                  {@link DefaultLabItemResolver} should be
	 *                                  used
	 * @param bCreatePatientIfNotExists indicates whether a patient should be
	 *                                  created if not existing
	 * @return the result as type Result
	 */
	public Result<?> importFile(final IVirtualFilesystemHandle file, final IVirtualFilesystemHandle archiveDir,
			ILabItemResolver labItemResolver, boolean bCreatePatientIfNotExists) throws IOException {
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);

		for (HL7Reader hl7Reader : hl7Readers) {
			this.hl7Reader = hl7Reader;
			Result<?> ret = parse(hl7Reader, labItemResolver, labContactResolver, bCreatePatientIfNotExists);
			// move result to archive
			if (ret.isOK()) {
				if (archiveDir != null) {
					if (archiveDir.exists() && archiveDir.isDirectory()) {
						if (file.exists() && !file.isDirectory() && file.canRead()) {

							IVirtualFilesystemHandle newFile = archiveDir.subFile(file.getName());
							if (newFile.exists()) {
								// on multiple move to archive dir:
								// first time use own filename
								// n+ times use filename_timestamp
								String fnwts = file.getName() + "_" + new TimeTool().toString(TimeTool.TIMESTAMP);
								newFile = archiveDir.subFile(fnwts);
							}

							file.moveTo(newFile);
						}
					}
				}
			}
			return ret;
		}
		return new Result<>(SEVERITY.ERROR, 2, Messages.Core_Error_while_importing_HL7, "No reader for file", true); //$NON-NLS-1$
	}

	public void importFromDir(final IVirtualFilesystemHandle dir, final IVirtualFilesystemHandle archiveDir,
			Result<?> res, boolean bCreatePatientIfNotExists) throws IOException {

		IVirtualFilesystemHandle[] files = dir.listHandles(handle -> {

			boolean isDirectory = false;
			try {
				isDirectory = handle.isDirectory();
			} catch (IOException e) {
				logger.warn("Error checking [" + handle + "], treating as file", e);
			}

			if (isDirectory) {
				if (!handle.getName().equalsIgnoreCase(archiveDir.getName())) {
					return true;
				}
			} else {
				if ("hl7".equalsIgnoreCase(handle.getExtension())) { //$NON-NLS-1$
					return true;
				}
			}
			return false;
		});
		for (IVirtualFilesystemHandle file : files) {
			if (file.isDirectory()) {
				importFromDir(file, archiveDir, res, bCreatePatientIfNotExists);
			} else {
				Result<?> r = importFile(file, archiveDir, bCreatePatientIfNotExists);
				if (res == null) {
					res = r;
				} else {
					res.add(r.getSeverity(), 1, StringUtils.EMPTY, null, true);
				}
			}
		}
	}

	/**
	 * Equivalent to importFile(new File(file), null)
	 *
	 * @param filepath the file to be imported (full path)
	 * @return
	 * @throws IOException
	 */
	public Result<?> importFile(final String filepath, boolean bCreatePatientIfNotExists) throws IOException {
		IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(filepath);
		return importFile(fileHandle, null, bCreatePatientIfNotExists);
	}

	public Result<?> importMessage(String message, boolean bCreatePatientIfNotExists) throws IOException {
		HL7Reader hl7Reader = HL7ReaderFactory.INSTANCE.getReader(message);

		this.hl7Reader = hl7Reader;
		Result<?> ret = parse(hl7Reader, bCreatePatientIfNotExists);
		if (ret.isOK()) {
			return new Result<Object>("OK");
		}
		return ret;
		// ResultAdapter.displayResult(ret,
		// ch.elexis.core.ui.importer.div.importers.Messages.Core_Error_while_reading);

		// ElexisEventDispatcher.reload(LabItem.class);
		// return ret;
	}

	public IPatient getPatient() {
		return pat;
	}

	public void setPatient(IPatient pat) {
		this.pat = pat;
	}

	public TimeTool getDate() {
		return date;
	}

	public void setDate(TimeTool date) {
		this.date = date;
	}
}
