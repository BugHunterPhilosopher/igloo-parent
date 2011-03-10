package fr.openwide.core.hibernate.search.bridge;

import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.StringBridge;

import fr.openwide.core.hibernate.business.generic.model.GenericEntity;

public abstract class AbstractGenericEntityIdFieldBridge implements FieldBridge, StringBridge {
	
	@Override
	public String objectToString(Object object) {
		if (!(object instanceof GenericEntity)) {
			throw new IllegalArgumentException("This FieldBridge only supports GenericEntity properties.");
		}
		GenericEntity<?, ?> entity = (GenericEntity<?, ?>) object;
		return entity.getId().toString();
	}
	
}