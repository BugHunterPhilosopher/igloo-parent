package org.iglooproject.basicapp.core.business.upgrade.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.spring.BaseSpringJdbcMigration;
import org.iglooproject.jpa.more.business.upgrade.model.DataUpgradeRecord;
import org.iglooproject.jpa.more.business.upgrade.model.IDataUpgrade;
import org.iglooproject.jpa.more.config.util.FlywaySpring;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

public abstract class AbstractDataUpgradeMigration extends BaseSpringJdbcMigration
		implements MigrationChecksumProvider, ConfigurationAware {

	@Value("${db.schema}")
	private String defaultSchema;

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
		((FlywaySpring) flywayConfiguration).getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
		final Integer id = new Integer(jdbcTemplate.queryForObject(
				String.format("SELECT NEXTVAL('%s.%s_id_seq');", defaultSchema, DataUpgradeRecord.class.getSimpleName()),
				Integer.class
		));
		
		jdbcTemplate.execute(
				String.format("INSERT INTO %s.%s (id, name, autoPerform, done) VALUES (?, ?, ?, ?)",
						defaultSchema, DataUpgradeRecord.class.getSimpleName()),
				new PreparedStatementCallback<Boolean>() {
					@Override
					public Boolean doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						ps.setInt(1, id);
						ps.setString(2, getDataUpgradeClass().getSimpleName());
						ps.setBoolean(3, true);
						ps.setBoolean(4, false);
						return ps.execute();
					}
				}
		);
	}

	protected abstract Class<? extends IDataUpgrade> getDataUpgradeClass();

	@Override
	public Integer getChecksum(){
		return getDataUpgradeClass().getSimpleName().hashCode() * 23;
	}
}
