package org.iglooproject.wicket.bootstrap3.console.maintenance.file.page;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.RangeValidator;
import org.iglooproject.jpa.more.business.file.model.path.HashTableFileStorePathGeneratorImpl;
import org.iglooproject.wicket.bootstrap3.console.maintenance.template.ConsoleMaintenanceTemplate;
import org.iglooproject.wicket.bootstrap3.console.template.ConsoleTemplate;
import org.iglooproject.wicket.markup.html.basic.CoreLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleMaintenanceFilePage extends ConsoleMaintenanceTemplate {

	private static final long serialVersionUID = -4594419424463767339L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMaintenanceFilePage.class);

	private final IModel<Integer> hashTableByteSizeModel = new Model<Integer>(1);
	private final IModel<String> fileKeyModel = new Model<String>();
	private final IModel<String> extensionModel = new Model<String>();
	
	private final IModel<String> pathModel = new Model<String>();
	
	public ConsoleMaintenanceFilePage(PageParameters parameters) {
		super(parameters);
		
		addHeadPageTitleKey("console.maintenance.file");
		
		Form<Void> form = new Form<Void>("form") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit() {
				try {
					HashTableFileStorePathGeneratorImpl pathGenerator = new HashTableFileStorePathGeneratorImpl(hashTableByteSizeModel.getObject());
					pathModel.setObject(pathGenerator.getFilePath(fileKeyModel.getObject(), extensionModel.getObject()));
					pathModel.detach();
				} catch (Exception e) {
					LOGGER.error("Unexpected error while generating file path through hashtable.");
					pathModel.setObject(null);
					Session.get().error(getString("common.error.unexpected"));
				}
			}
			
			@Override
			protected void onError() {
				pathModel.setObject(null);
			}
		};
		add(form);
		
		form.add(
				new TextField<Integer>("hashTableByteSize", hashTableByteSizeModel, Integer.class)
						.setRequired(true)
						.setLabel(new ResourceModel("console.maintenance.file.hashTableByteSize"))
						.add(
								new RangeValidator<Integer>(
										HashTableFileStorePathGeneratorImpl.MIN_HASH_TABLE_BYTE_SIZE,
										HashTableFileStorePathGeneratorImpl.MAX_HASH_TABLE_BYTE_SIZE)
						),
				new TextField<String>("fileKey", fileKeyModel)
						.setRequired(true)
						.setLabel(new ResourceModel("console.maintenance.file.fileKey")),
				new TextField<String>("extension", extensionModel)
						.setLabel(new ResourceModel("console.maintenance.file.extension")),
				
				new CoreLabel("path", pathModel).hideIfEmpty()
		);
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		hashTableByteSizeModel.detach();
		fileKeyModel.detach();
		extensionModel.detach();
		pathModel.detach();
	}
	
	@Override
	protected Class<? extends ConsoleTemplate> getMenuItemPageClass() {
		return ConsoleMaintenanceFilePage.class;
	}

}
