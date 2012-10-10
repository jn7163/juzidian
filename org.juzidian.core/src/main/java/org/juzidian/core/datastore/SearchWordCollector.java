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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.juzidian.cedict.CedictEntry;
import org.juzidian.cedict.CedictLoadHandler;
import org.juzidian.core.DictionaryEntry;

/**
 * A {@link CedictLoadHandler} that collects {@link DictionaryEntry}s that match
 * some search criteria.
 */
abstract class SearchWordCollector implements CedictLoadHandler {

	private final List<DictionaryEntry> words = new ArrayList<DictionaryEntry>();

	private long startNanos;

	/**
	 * @param word a {@link DictionaryEntry}.
	 * @return <code>true</code> if the word matches search criteria.
	 */
	protected abstract boolean matches(DictionaryEntry word);

	@Override
	public void loadingStarted() {
		this.startNanos = System.nanoTime();
	}

	@Override
	public void entryLoaded(final CedictEntry cedictEntry) {
		final DictionaryEntry word = new CedictDictionaryEntryAdaptor(cedictEntry);
		if (this.matches(word)) {
			this.words.add(word);
		}
	}

	@Override
	public void loadingFinished() {
		final long endNanos = System.nanoTime();
		final long duration = endNanos - this.startNanos;
		System.out.println(String.format("Search for '%s' took %.3fs", this.getSearchCriteriaDisplay(), duration / 1000 / 1000 / 1000f));
	}

	protected abstract String getSearchCriteriaDisplay();

	public List<DictionaryEntry> getWords() {
		return Collections.unmodifiableList(this.words);
	}

}