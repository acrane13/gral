/*
 * GRAL: Vector export for Java(R) Graphics2D
 *
 * (C) Copyright 2009-2010 Erich Seifert <info[at]erichseifert.de>, Michael Seifert <michael.seifert[at]gmx.net>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.erichseifert.gral.data.AbstractDataSource;
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
 * <p>Only filtered columns are stored. Access to unfiltered columns is
 * delegated to the original data source.</p>
 */
public abstract class Filter extends AbstractDataSource implements DataListener {
	/**
	 * Behavior when engaging the borders of a column, so that the filter
	 * would need more data values than available.
	 */
	public static enum Mode {
		/**	Ignores missing values. */
		MODE_OMIT,
		/**	Treats missing values as zero. */
		MODE_ZERO,
		/**	Repeats the last value. */
		MODE_REPEAT,
		/**	Mirrors values at the last value. */
		MODE_MIRROR,
		/**	Repeats the data. */
		MODE_CIRCULAR
	};

	private final DataSource original;
	private final int[] cols;
	private final List<double[]> data;
	private Mode mode;

	/**
	 * Creates a new Filter object with the specified DataSource, Mode and
	 * columns to filter.
	 * @param original DataSource to be filtered.
	 * @param mode Mode to be used.
	 * @param cols Column indexes to be filtered.
	 */
	public Filter(DataSource original, Mode mode, int... cols) {
		this.data = new ArrayList<double[]>(cols.length);
		this.original = original;
		this.mode = mode;
		this.cols = Arrays.copyOf(cols, cols.length);
		// A sorted array is necessary for binary search
		Arrays.sort(this.cols);

		this.original.addDataListener(this);
		dataChanged(this.original);
	}

	/**
	 * Returns the value of the DataSource at the specified column and row.
	 * @param col Column index.
	 * @param row Row index.
	 * @return Original value.
	 */
	protected Number getOriginal(int col, int row) {
		int rowLast = getRowCount() - 1;
		if (row < 0 || row > rowLast) {
			if (Mode.MODE_OMIT.equals(mode)) {
				return Double.NaN;
			} else if (Mode.MODE_ZERO.equals(mode)) {
				return 0.0;
			} else if (Mode.MODE_REPEAT.equals(mode)) {
				row = MathUtils.limit(row, 0, rowLast);
			} else if (Mode.MODE_MIRROR.equals(mode)) {
				int rem = Math.abs(row) / rowLast;
				int mod = Math.abs(row) % rowLast;
				if ((rem & 1) == 0) {
					row = mod;
				} else {
					row = rowLast - mod;
				}
			} else if (Mode.MODE_CIRCULAR.equals(mode)) {
				row = Math.abs(row) % (rowLast + 1);
			}
		}
		return original.get(col, row);
	}

	/**
	 * Clears this Filter.
	 */
	protected void clear() {
		data.clear();
	}

	/**
	 * Adds the specified row to this Filter.
	 * @param row Row to be added.
	 */
	protected void add(double[] row) {
		data.add(row);
	}

	@Override
	public Number get(int col, int row) {
		int colPos = getIndexFiltered(col);
		if (colPos < 0) {
			return original.get(col, row);
		}
		return data.get(row)[colPos];
	}

	@Override
	public int getColumnCount() {
		return original.getColumnCount();
	}

	/**
	 * Returns the number of filtered columns
	 * @return Number of filtered columns
	 */
	protected int getColumnCountFiltered() {
		return cols.length;
	}

	@Override
	public int getRowCount() {
		return original.getRowCount();
	}

	@Override
	public void dataChanged(DataSource data) {
		filter();
		notifyDataChanged();
	}

	/**
	 * Returns the index of the original column using the index of the filtered column.
	 * @param col Index of the filtered column
	 * @return Index of the original column
	 */
	protected int getIndexOriginal(int col) {
		return cols[col];
	}

	/**
	 * Returns the index of the filtered column using the index of the original column.
	 * @param col Index of the original column
	 * @return Index of the filtered column
	 */
	protected int getIndexFiltered(int col) {
		return Arrays.binarySearch(cols, col);
	}

	/**
	 * Returns whether the specified column is filtered.
	 * @param col Column index.
	 * @return True, if the column is filtered.
	 */
	protected boolean isFiltered(int col) {
		return getIndexFiltered(col) >= 0;
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
		dataChanged(this);
	}
}
