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
package org.juzidian.core.datastore;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.juzidian.core.DictionaryDataStore;
import org.juzidian.core.DictionaryEntry;
import org.juzidian.core.PinyinSyllable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

public class DbDictionaryDataStore implements DictionaryDataStore {

	private final Dao<DbDictionaryEntry, Long> dictionaryEntryDao;

	@Inject
	public DbDictionaryDataStore(final Dao<DbDictionaryEntry, Long> ormLiteDao) {
		this.dictionaryEntryDao = ormLiteDao;
	}

	private String formatPinyin(final List<PinyinSyllable> list) {
		final StringBuilder sb = new StringBuilder();
		for (final PinyinSyllable pinyinSyllable : list) {
			sb.append(pinyinSyllable.getLetters()).append(" ");
		}
		return sb.toString();
	}

	private List<String> unformatDefinitions(final String english) {
		final String[] definitions = english.split("/");
		return Arrays.asList(definitions);
	}

	@Override
	public List<DictionaryEntry> findPinyin(final List<PinyinSyllable> pinyin) {
		try {
			final PreparedQuery<DbDictionaryEntry> query = this.dictionaryEntryDao.queryBuilder().where()
					.like(DbDictionaryEntry.COLUMN_PINYIN, "%" + this.formatPinyin(pinyin) + "%").prepare();
			return this.transformEntries(this.dictionaryEntryDao.query(query));
		} catch (final SQLException e) {
			throw new DictionaryDataStoreException("Failed to execute query", e);
		}
	}

	private List<DictionaryEntry> transformEntries(final List<DbDictionaryEntry> dbEntries) {
		final List<DictionaryEntry> entries = new LinkedList<DictionaryEntry>();
		for (final DbDictionaryEntry dbEntry : dbEntries) {
			entries.add(this.createEntry(dbEntry));
		}
		return entries;
	}

	private DictionaryEntry createEntry(final DbDictionaryEntry dbEntry) {
		final String traditional = dbEntry.getTraditional();
		final String simplified = dbEntry.getSimplified();
		final String pinyin = dbEntry.getPinyin();
		final String english = dbEntry.getEnglish();
		return new BasicDictionaryEntry(traditional, simplified, this.unformatPinyin(pinyin), this.unformatDefinitions(english));
	}

	private List<PinyinSyllable> unformatPinyin(final String pinyin) {
		final String[] rawPinyin = pinyin.split("  ");
		final List<PinyinSyllable> syllables = new LinkedList<PinyinSyllable>();
		for (final String letters : rawPinyin) {
			final PinyinSyllable syllable = new PinyinSyllable(letters);
			syllables.add(syllable);
		}
		return syllables;
	}

	@Override
	public List<DictionaryEntry> findChinese(final String chineseCharacters) {
		try {
			final PreparedQuery<DbDictionaryEntry> query = this.dictionaryEntryDao.queryBuilder().where()
					.like(DbDictionaryEntry.COLUMN_HANZI_SIMPLIFIED, "%" + chineseCharacters + "%").prepare();
			return this.transformEntries(this.dictionaryEntryDao.query(query));
		} catch (final SQLException e) {
			throw new DictionaryDataStoreException("Failed to execute query", e);
		}
	}

	@Override
	public List<DictionaryEntry> findDefinitions(final String englishWords) {
		try {
			final PreparedQuery<DbDictionaryEntry> query = this.dictionaryEntryDao.queryBuilder().where()
					.like(DbDictionaryEntry.COLUMN_ENGLISH, "%" + englishWords + "%").prepare();
			return this.transformEntries(this.dictionaryEntryDao.query(query));
		} catch (final SQLException e) {
			throw new DictionaryDataStoreException("Failed to execute query", e);
		}
	}

}
