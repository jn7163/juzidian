/*
 * Copyright Nathan Jones 2012
 *
 * This file is part of Juzidian.
 *
 * Juzidian is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Juzidian is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Juzidian.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.juzidian.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Creates a {@link DbDictionaryDataStore} database schema.
 */
public class DictionaryDataStoreSchemaCreator {

	final static List<Class<?>> DICTIONARY_ENTITY_CLASSES = new ArrayList<Class<?>>();

	static {
		DICTIONARY_ENTITY_CLASSES.add(DictionaryDataStoreEntry.class);
		DICTIONARY_ENTITY_CLASSES.add(DictionaryDataStoreMetadata.class);
	}

	/**
	 * Create or re-create a {@link DbDictionaryDataStore} DB schema using the
	 * given OrmLite connection source, destroying any existing data.
	 */
	public void createSchema(final ConnectionSource connectionSource) {
		try {
			this.createTables(connectionSource, DICTIONARY_ENTITY_CLASSES);
		} catch (final SQLException e) {
			throw new RuntimeException("Failed to created tables", e);
		}
	}

	private void createTables(final ConnectionSource connectionSource, final Collection<Class<?>> entityClasses) throws SQLException {
		for (final Class<?> entityClass : entityClasses) {
			this.createTable(connectionSource, entityClass);
		}
	}

	private void createTable(final ConnectionSource connectionSource, final Class<?> dataClass) throws SQLException {
		TableUtils.dropTable(connectionSource, dataClass, true);
		TableUtils.createTable(connectionSource, dataClass);
	}

}
