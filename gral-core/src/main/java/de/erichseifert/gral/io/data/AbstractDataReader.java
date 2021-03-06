/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2016 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <mseifert[at]error-reports.org>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.gral.io.data;

import java.util.HashMap;
import java.util.Map;

import de.erichseifert.gral.io.IOCapabilitiesStorage;

/**
 * Base implementation for classes that read data sources from input streams.
 */
public abstract class AbstractDataReader extends IOCapabilitiesStorage
		implements DataReader {
	/** Settings stored as (key, value) pairs. */
	private final Map<String, Object> settings;
	/** Default settings. */
	private final Map<String, Object> defaults;
	/** Data format as MIME type string. */
	private final String mimeType;

	/**
	 * Initializes a new reader with MIME type information.
	 * @param mimeType MIME type
	 */
	public AbstractDataReader(String mimeType) {
		settings = new HashMap<String, Object>();
		defaults = new HashMap<String, Object>();
		this.mimeType = mimeType;
	}

	/**
	 * Returns the MIME type.
	 * @return MIME type string.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Returns the setting for the specified key.
	 * @param <T> return type
	 * @param key key of the setting
	 * @return the value of the setting
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSetting(String key) {
		if (!settings.containsKey(key)) {
			return (T) defaults.get(key);
		}
		return (T) settings.get(key);
	}

	/**
	 * Sets the setting for the specified key.
	 * @param <T> value type
	 * @param key key of the setting
	 * @param value value of the setting
	 */
	public <T> void setSetting(String key, T value) {
		settings.put(key, value);
	}

	/**
	 * Defines a default value for the setting with the specified key.
	 * @param <T> Data type of value.
	 * @param key Setting key.
	 * @param value Default value.
	 */
	protected <T> void setDefault(String key, T value) {
		defaults.put(key, value);
	}

}
