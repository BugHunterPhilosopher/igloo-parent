package org.iglooproject.jpa.more.business.parameter.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.bindgen.Bindable;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.iglooproject.jpa.business.generic.model.GenericEntity;

@Entity
@Bindable
public class Parameter extends GenericEntity<Long, Parameter> {
	
	private static final long serialVersionUID = 4739408616523513971L;

	@Id
	@GeneratedValue
	private Long id;

	@NaturalId
	@Column(nullable = false, unique = true)
	private String name;

	@Column
	@Type(type = "org.iglooproject.jpa.hibernate.usertype.StringClobType")
	private String stringValue;
	
	public Parameter() {
		super();
	}

	public Parameter(String name, String value) {
		super();
		setName(name);
		setStringValue(value);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	@Override
	public String getDisplayName() {
		StringBuilder displayName = new StringBuilder();
		displayName.append(name);
		displayName.append(':');
		displayName.append(getStringValue());

		return displayName.toString();
	}

	@Override
	public String getNameForToString() {
		return name;
	}

}
