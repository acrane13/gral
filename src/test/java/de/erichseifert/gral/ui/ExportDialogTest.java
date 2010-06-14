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

package de.erichseifert.gral.ui;

import static org.junit.Assert.assertEquals;

import java.awt.Component;
import java.awt.geom.Rectangle2D;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.erichseifert.gral.Drawable;
import de.erichseifert.gral.DrawableContainer;

public class ExportDialogTest {
	private static Drawable drawable;
	private ExportDialog dialog;

	private static final class TestExportDialog extends ExportDialog {
		public TestExportDialog(Component parent, Drawable drawable) {
			super(parent, drawable);
		}

		@Override
		public void setDocumentBounds(double x, double y, double w, double h) {
			// TODO Auto-generated method stub
			super.setDocumentBounds(x, y, w, h);
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		drawable = new DrawableContainer();
	}

	@Before
	public void setUp() {
		dialog = new ExportDialog(null, drawable);
	}

	@Test
	public void testCreation() {
		assertEquals(drawable.getBounds(), dialog.getDocumentBounds());
		assertEquals(ExportDialog.UserAction.CANCEL, dialog.getUserAction());
	}

	@Test
	public void testDocumentBounds() {
		Rectangle2D expected = new Rectangle2D.Double(0.0, 1.0, 2.0, 3.0);

		for (int run = 0; run < 2; run++) {
			dialog.setDocumentBounds(
					expected.getX(), expected.getY(),
					expected.getWidth(), expected.getHeight());
			assertEquals(expected, dialog.getDocumentBounds());
		}
	}

}
