package org.iglooproject.basicapp.core.business.user.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.bindgen.Bindable;

@Bindable
@Cacheable
@Entity
public class BasicUser extends User {

	private static final long serialVersionUID = 7202814609595947705L;

}
