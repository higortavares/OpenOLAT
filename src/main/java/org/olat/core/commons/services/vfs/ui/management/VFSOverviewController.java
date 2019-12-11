package org.olat.core.commons.services.vfs.ui.management;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.ui.management.VFSOverviewTableModel.VFSOverviewColumns;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class VFSOverviewController extends FormBasicController {

	private FlexiTableElement vfsOverviewTableElement;
	private VFSOverviewTableModel vfsOverviewTableModel;

	private FormLink largeFilesLink;
	private FormLink trashLink;
	private FormLink versionsLink;
	private FormLink thumbnailLink;

	public static final Event OPEN_TRASH_EVENT			= new Event("vfs.openTrash");
	public static final Event OPEN_LARGE_FILES_EVENT	= new Event("vfs.openLargeFiles");
	public static final Event OPEN_VERSIONS_EVENT	= new Event("vfs.cleanUpVersions");
	public static final Event RESET_THUMNBNAILS_EVENT	= new Event("vfs.resetThumbnails");

	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Autowired
	private TaskExecutorManager taskExecutor;
	private DialogBoxController confirmResetThumbnails;
	public VFSOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "vfs_overview");

		initForm(ureq);
		updateModel();
	}

	public void updateModel() {
		List<VFSOverviewTableContentRow> rows = new ArrayList<>();

		rows.add(new VFSOverviewTableContentRow("vfs.overview.files", (Long) vfsRepositoryService.getFileStats().get(0)[0], (Long) vfsRepositoryService.getFileStats().get(0)[1], largeFilesLink));
		rows.add(new VFSOverviewTableContentRow("vfs.overview.versions", (Long) vfsRepositoryService.getRevisionStats().get(0)[0], (Long) vfsRepositoryService.getRevisionStats().get(0)[1], versionsLink));
		rows.add(new VFSOverviewTableContentRow("vfs.overview.trash", (Long) vfsRepositoryService.getFileStats().get(0)[2], (Long) vfsRepositoryService.getFileStats().get(0)[3], trashLink));
		rows.add(new VFSOverviewTableContentRow("vfs.overview.thumbnails", (Long) vfsRepositoryService.getThumbnailStats().get(0)[0], (Long) vfsRepositoryService.getThumbnailStats().get(0)[1], thumbnailLink));

		vfsOverviewTableElement.setFooter(true);
		vfsOverviewTableModel.setObjects(rows);
		vfsOverviewTableElement.reset(true, true, true);

		Long size = new Long(0);
		Long amount = new Long(0);

		for(VFSOverviewTableContentRow row: rows) {
			size += row.getSize() != null ? row.getSize() : 0;
			amount += row.getAmount() != null ? row.getAmount() : 0;
		}

		vfsOverviewTableModel.setFooter(new VFSOverviewTableFooterRow("vfs.overview.total", amount, size, ""));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel nameColumn;
		DefaultFlexiColumnModel amountColumn;
		DefaultFlexiColumnModel sizeColumn;
		DefaultFlexiColumnModel actionColumn;

		nameColumn = new DefaultFlexiColumnModel(VFSOverviewColumns.name, new VFSOverviewNameCellRenderer());
		nameColumn.setFooterCellRenderer(new VFSOverviewFooterNameCellRenderer());

		amountColumn = new DefaultFlexiColumnModel(VFSOverviewColumns.amount, new VFSOverviewAmountCellRenderer());
		amountColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		amountColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		amountColumn.setFooterCellRenderer(new VFSOverviewFooterAmountCellRenderer());

		sizeColumn = new DefaultFlexiColumnModel(VFSOverviewColumns.size, new VFSOverviewSizeCellRenderer());
		sizeColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		sizeColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		sizeColumn.setFooterCellRenderer(new VFSOverviewFooterSizeCellRenderer());

		actionColumn = new DefaultFlexiColumnModel(VFSOverviewColumns.action);
		actionColumn.setAlignment(FlexiColumnModel.ALIGNMENT_LEFT);
		actionColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_LEFT);

		columnsModel.addFlexiColumnModel(nameColumn);
		columnsModel.addFlexiColumnModel(amountColumn);
		columnsModel.addFlexiColumnModel(sizeColumn);
		columnsModel.addFlexiColumnModel(actionColumn);		

		vfsOverviewTableModel = new VFSOverviewTableModel(columnsModel, getLocale());
		vfsOverviewTableElement = uifactory.addTableElement(getWindowControl(), "vfs.overview", vfsOverviewTableModel, getTranslator(), formLayout);
		vfsOverviewTableElement.setSearchEnabled(false);
		vfsOverviewTableElement.setCustomizeColumns(false);
		vfsOverviewTableElement.setNumOfRowsEnabled(false);

		largeFilesLink = uifactory.addFormLink("vfs.overview.fileslink", formLayout);
		trashLink = uifactory.addFormLink("vfs.overview.trashlink", formLayout);
		versionsLink = uifactory.addFormLink("vfs.overview.versionslink", formLayout);
		thumbnailLink = uifactory.addFormLink("vfs.overview.thumbnaillink", formLayout); 
	}

	@Override
	protected void doDispose() {

	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmResetThumbnails) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				resetThumbnails();
			}
		} 
		super.event(ureq, source, event);
	}
	
	@Override 
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == trashLink) {
			fireEvent(ureq, OPEN_TRASH_EVENT);
		} else if (source == versionsLink) {
			fireEvent(ureq, OPEN_VERSIONS_EVENT);
		} else if (source == largeFilesLink) {
			fireEvent(ureq, OPEN_LARGE_FILES_EVENT);
		} else if (source == thumbnailLink) {
			String text = translate("vfs.overview.thumbnails.reset.confirm");
			String title = translate("vfs.overview.thumbnails.reset.title");
			confirmResetThumbnails = activateYesNoDialog(ureq, title, text, confirmResetThumbnails);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}

	private void resetThumbnails() {
		flc.contextPut("recalculating", Boolean.TRUE);
		ResetThumbnails task = new ResetThumbnails();
		taskExecutor.execute(task);
	}
	
	private class ResetThumbnails implements Runnable {
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			logInfo("Start reset of thumbnails");

			String metaRoot = FolderConfig.getCanonicalMetaRoot();
			vfsRepositoryService.resetThumbnails(new File(metaRoot));
			flc.contextPut("recalculating", Boolean.FALSE);

			logInfo("Finished reset of thumbnails in " + (System.currentTimeMillis() - start) + " (ms)");
		}
	}
}