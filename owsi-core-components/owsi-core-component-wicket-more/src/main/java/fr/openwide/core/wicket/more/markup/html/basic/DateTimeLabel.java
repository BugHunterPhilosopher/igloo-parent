/*
 * Copyright (C) 2009-2011 Open Wide
 * Contact: contact@openwide.fr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.openwide.core.wicket.more.markup.html.basic;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import fr.openwide.core.wicket.more.util.IDatePattern;
import fr.openwide.core.wicket.more.util.convert.converters.PatternDateConverter;

public class DateTimeLabel extends Label {
	private static final long serialVersionUID = 7214422620839758144L;

	private IConverter converter;
	
	private IDatePattern datePattern;
	
	private boolean forceGmt;
	
	/**
	 * Use forceGmt = true by default.
	 */
	public DateTimeLabel(String id, IModel<Date> model, IDatePattern datePattern) {
		this(id, model, datePattern, true);
	}
	
	public DateTimeLabel(String id, IModel<Date> model, IDatePattern datePattern, boolean forceGmt) {
		super(id, model);
		
		this.datePattern = datePattern;
		this.forceGmt = forceGmt;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		this.converter = new PatternDateConverter(getString(datePattern.getJavaPatternKey()), forceGmt);
	}
	
	@Override
	public IConverter getConverter(Class<?> type) {
		return converter;
	}
	
	@Override
	protected void detachModel() {
		super.detachModel();
	}

}
