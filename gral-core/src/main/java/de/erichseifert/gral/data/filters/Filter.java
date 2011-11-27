/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2011 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael.seifert[at]gmx.net>
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
package de.erichseifert.gral.data.filters;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import de.erichseifert.gral.data.AbstractDataSource;
import de.erichseifert.gral.data.DataChangeEvent;
import de.erichseifert.gral.data.DataListener;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.util.MathUtils;


/**
 * <p>Abstract class that provides basic functions for filtering arbitrary
 * columns of a DataSource, in other words a set of one-dimensional data.</p>
 *
 * <p>Functionality includes:</p>
 * <ul>
 *   <li>Different modes for filtering (see {@link Mode})</li>
 *   <li>Support for listening for changes of the original data</li>
 *   <li>Filtering of multiple columns</li>
 * </ul>
 *
 * <p>Values of filtered columns are buffered. Access to unfiltered columns is
 * delegated to the original data source.</p>
 */
public abstract class Filter extends AbstractDataSource
		implements DataListener {
	/**
	 * Behavior when engaging the borders of a column, so that the filter
	 * would need more data values than available.
	 */
	public static enum Mode {
		/**	Ignores missing values. */
		OMIT,
		/**	Treats missing values as zero. */
		ZERO,
		/**	Repeats the last value. */
		REPEAT,
		/**	Mirrors values at the last value. */
		MIRROR,
		/**	Repeats the data. */
		CIRCULAR
	};

	/** Original data source. */
	protected final DataSource original;

	/** Columns that should be filtered. */
	private final int[] cols;
	/** Data that was produced by the filter. */
	private final ArrayList<Double[]> rows;
	/** Mode for handling. */
	private Mode mode;

	/**
	 * Initializes a new instance with the specified data source, border
	 * handling and columns to be filtered. The columns must be numeric,
	 * otherwise an {@code IllegalArgumentException} is thrown.
	 * @param original Data source to be filtered.
	 * @param mode Border handling mode to be used.
	 * @param cols Indexes of numeric columns to be filtered.
	 */
	@SuppressWarnings("unchecked")
	public Filter(DataSource original, Mode mode, int... cols) {
		this.rows = new ArrayList<Double[]>(original.getRowCount());
		this.original = original;
		this.mode = mode;

		this.cols = Arrays.copyOf(cols, cols.length);
		// A sorted array is necessary for binary search
		Arrays.sort(this.cols);

		// Check if columns are numeric
		Class<? extends Comparable<?>>[] originalColumnTypes =
			original.getColumnTypes();
		for (int colIndex : this.cols) {
			if (!original.isColumnNumeric(colIndex)) {
				throw new IllegalArgumentException(MessageFormat.format(
					"Column {0,number,integer} isn't numeric and cannot be filtered.", //$NON-NLS-1$
					colIndex));
			}
		}

		Class<? extends Comparable<?>>[] types = originalColumnTypes;
		for (int colIndex : this.cols) {
			types[colIndex] = Double.class;
		}
		setColumnTypes(types);

		this.original.addDataListener(this);
		dataUpdated(this.original);
	}

	/**
	 * Returns the value of the DataSource at the specified column and row.
	 * @param col Column index.
	 * @param row Row index.
	 * @return Original value.
	 */
	protected Comparable<?> getOriginal(int col, int row) {
		int rowLast = original.getRowCount() - 1;
		if (row < 0 || row > rowLast) {
			if (Mode.OMIT.equals(mode)) {
				return Double.NaN;
			} else if (Mode.ZERO.equals(mode)) {
				return 0.0;
			} else if (Mode.REPEAT.equals(mode)) {
				row = MathUtils.limit(row, 0, rowLast);
			} else if (Mode.MIRROR.equals(mode)) {
				int rem = Math.abs(row) / rowLast;
				int mod = Math.abs(row) % rowLast;
				if ((rem & 1) == 0) {
					row = mod;
				} else {
					row = rowLast - mod;
				}
			} else if (Mode.CIRCULAR.equals(mode)) {
				if (row >= 0) {
					row = row % (rowLast + 1);
				} else {
					row = (row + 1) % (rowLast + 1) + rowLast;
				}
			}
		}
		return original.get(col, row);
	}

	/**
	 * Clears this Filter.
	 */
	protected void clear() {
		rows.clear();
	}

	/**
	 * Adds the specified row data to this Filter.
	 * @param rowData Row data to be added.
	 */
	protected void add(Double[] rowData) {
		rows.add(rowData);
	}

	/**
	 * Adds the specified row data to this Filter.
	 * @param rowData Row to be added.
	 */
	protected void add(Number[] rowData) {
		Double[] doubleData = new Double[rowData.length];
		int i = 0;
		for (Number value : rowData) {
			doubleData[i++] = value.doubleValue();
		}
		rows.add(doubleData);
	}

	/**
	 * Returns the row with the specified index.
	 * @param col index of the column to return
	 * @param row index of the row to return
	 * @return the specified value of the data cell
	 */
	public Comparable<?> get(int col, int row) {
		int colPos = getIndex(col);
		if (colPos < 0) {
			return original.get(col, row);
		}
		return rows.get(row)[colPos];
	}

	/**
	 * Sets a new value for a specified cell.
	 * @param col Column of the cell.
	 * @param row Row of the cell.
	 * @param value New cell value.
	 * @return The previous value before it has been changed.
	 */
	protected Number set(int col, int row, Double value) {
		int colPos = getIndex(col);
		if (colPos < 0) {
			throw new IllegalArgumentException(
				"Can't set value in unfiltered column."); //$NON-NLS-1$
		}
		Double old = rows.get(row)[colPos];
		rows.get(row)[colPos] = value;
		notifyDataUpdated(new DataChangeEvent(this, col, row, old, value));
		return old;
	}

	@Override
	public int getColumnCount() {
		return original.getColumnCount();
	}

	/**
	 * Returns the number of filtered columns.
	 * @return Number of filtered columns.
	 */
	protected int getColumnCountFiltered() {
		if (cols.length == 0) {
			return original.getColumnCount();
		}
		return cols.length;
	}

	/**
	 * Returns the number of rows of the data source.
	 * @return number of rows in the data source.
	 */
	public int getRowCount() {
		return original.getRowCount();
	}

	/**
	 * Returns the number of filtered rows.
	 * @return Number of filtered rows.
	 */
	protected int getRowCountFiltered() {
		return original.getRowCount();
	}

	/**
	 * Method that is invoked when data has been added.
	 * This method is invoked by objects that provide support for
	 * {@code DataListener}s and should not be called manually.
	 * @param source Data source that has changed
	 * @param events Optional event object describing the data values that
	 *        have been added
	 */
	public void dataAdded(DataSource source, DataChangeEvent... events) {
		filter();
		notifyDataUpdated(events);
	}

	/**
	 * Method that is invoked when data has been updated.
	 * This method is invoked by objects that provide support for
	 * {@code DataListener}s and should not be called manually.
	 * @param source Data source that has changed
	 * @param events Optional event object describing the data values that
	 *        have been added
	 */
	public void dataUpdated(DataSource source, DataChangeEvent... events) {
		filter();
		notifyDataUpdated(events);
	}

	/**
	 * Method that is invoked when data has been added.
	 * This method is invoked by objects that provide support for
	 * {@code DataListener}s and should not be called manually.
	 * @param source Data source that has changed
	 * @param events Optional event object describing the data values that
	 *        have been added
	 */
	public void dataRemoved(DataSource source, DataChangeEvent... events) {
		filter();
		notifyDataUpdated(events);
	}

	/**
	 * Returns the index of the original column using the index of the
	 * filtered column.
	 * @param col Index of the filtered column
	 * @return Index of the original column
	 */
	protected int getIndexOriginal(int col) {
		if (cols.length == 0) {
			return col;
		}
		return cols[col];
	}

	/**
	 * Returns the index of the filtered column using the index of the
	 * original column.
	 * @param col Index of the original column
	 * @return Index of the filtered column
	 */
	protected int getIndex(int col) {
		if (cols.length == 0) {
			return col;
		}
		return Arrays.binarySearch(cols, col);
	}

	/**
	 * Returns whether the specified column is filtered.
	 * @param col Column index.
	 * @return True, if the column is filtered.
	 */
	protected boolean isFiltered(int col) {
		return getIndex(col) >= 0;
	}

	/**
	 * Invokes the filtering routine.
	 */
	protected abstract void filter();

	/**
	 * Returns the Mode of this Filter.
	 * @return Mode of filtering.
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Sets the Mode the specified value.
	 * @param mode Mode of filtering.
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
		dataUpdated(this);
	}

}
