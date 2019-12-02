package org.iglooproject.jpa.business.generic.model.migration;

import java.io.Serializable;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

import org.iglooproject.jpa.hibernate.dialect.PerTableSequenceStyleGenerator;

/**
 * Sequence generator used to define the new id from the id used in an old application.
 * 
 * <pre>
 * {@code
 * @Id
 * @GeneratedValue(generator = "Ticket_id")
 * @GenericGenerator(name = "Ticket_id", strategy = MigratedFromOldApplicationSequenceGenerator.CLASS_NAME)
 * @DocumentId
 * private Long id;
 * }
 * </pre>
 */
public class MigratedFromOldApplicationSequenceGenerator extends PerTableSequenceStyleGenerator {
	
	public static final String CLASS_NAME = "org.iglooproject.jpa.business.generic.model.migration.MigratedFromOldApplicationSequenceGenerator";
	
	@Override
	public Object generate(SharedSessionContractImplementor session, Object obj) {
		if (obj instanceof IMigratedFromOldApplicationEntity) {
			IMigratedFromOldApplicationEntity<?> migratedEntity = (IMigratedFromOldApplicationEntity<?>) obj;
			Serializable oldApplicationId = migratedEntity.getOldApplicationId();
			if (oldApplicationId != null) {
				migratedEntity.setMigrated(true);
				return oldApplicationId;
			}
		}
		
		return super.generate(session, obj);
	}

}
