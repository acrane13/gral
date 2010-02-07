/* GRAL : a free graphing library for the Java(tm) platform
 *
 * (C) Copyright 2009-2010, by Erich Seifert and Michael Seifert.
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.erichseifert.gral;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.erichseifert.gral.data.DataTests;
import de.erichseifert.gral.io.IoTests;
import de.erichseifert.gral.plots.PlotsTests;
import de.erichseifert.gral.util.UtilTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	UtilTests.class,
	DataTests.class,
	PlotsTests.class,
	DrawablePanelTest.class,
	EdgeLayoutTest.class,
	StackedLayoutTest.class,
	IoTests.class
})
public class AllTests {
}
