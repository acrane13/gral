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
package de.erichseifert.gral.examples.pieplot;

import java.awt.BorderLayout;
import java.util.Random;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.examples.ExamplePanel;
import de.erichseifert.gral.plots.PiePlot;
import de.erichseifert.gral.plots.colors.MultiColor;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;


public class SimplePiePlot extends ExamplePanel {
	private static final long serialVersionUID = 1L;
	private static final int SAMPLE_COUNT = 15;
	/** Instance to generate random data values. */
	private static Random random = new Random();

	@SuppressWarnings("unchecked")
	public SimplePiePlot() {
		// Create data
		DataTable data = new DataTable(Integer.class);
		for (int i = 0; i < SAMPLE_COUNT; i++) {
			int val = random.nextInt(10) + 1;
			data.add((random.nextDouble() <= 0.15) ? -val : val);
		}

		// Create new pie plot
		PiePlot plot = new PiePlot(data);

		// Format plot
		plot.setSetting(PiePlot.TITLE, getDescription());
		// Change relative size of pie
		plot.setSetting(PiePlot.RADIUS, 0.9);
		// Change relative size of inner region
		plot.setSetting(PiePlot.RADIUS_INNER, 0.4);
		// Change the width of gaps between segments
		plot.setSetting(PiePlot.GAP, 0.2);
		// Change the colors
		MultiColor colors = new MultiColor(COLOR1, COLOR2);
		//colors.setColorVariance(new float[] {0.5345f, 0.0f, 0.7250f, 0.2f, 0.7843f, 0.3f});
		plot.setSetting(PiePlot.COLORS, colors);
		plot.setInsets(new Insets2D.Double(20.0, 40.0, 40.0, 40.0));

		// Add plot to Swing component
		add(new InteractivePanel(plot), BorderLayout.CENTER);
	}

	@Override
	public String getTitle() {
		return "Donut plot";
	}

	@Override
	public String getDescription() {
		return String.format("Donut plot of %d random data values", SAMPLE_COUNT);
	}

	public static void main(String[] args) {
		new SimplePiePlot().showInFrame();
	}
}
