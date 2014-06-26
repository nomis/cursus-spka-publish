/*
	cursus - Race series management program
	Copyright 2014  Simon Arlott

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU Affero General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Affero General Public License for more details.

	You should have received a copy of the GNU Affero General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spka.cursus.publish.website;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Ordering;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import eu.lp0.cursus.publish.html.XSLTHTMLGenerator;
import eu.lp0.cursus.xml.ExportException;
import eu.lp0.cursus.xml.ImportException;
import eu.lp0.cursus.xml.data.entity.DataXMLClass;
import eu.lp0.cursus.xml.scores.ScoresXMLFile;

@SuppressWarnings("nls")
public class ResultsPagesGenerator {
	private static final Map<String, String> CLASSES = new LinkedHashMap<String, String>();

	static {
		CLASSES.put("Junior", "Junior");
		CLASSES.put("16\" Wheel", "16\"");
	}

	private Map<String, ByteSource> pages = new LinkedHashMap<String, ByteSource>();

	public ResultsPagesGenerator(File scoresFile) throws IOException, ImportException, ExportException {
		ScoresXMLFile scores = new TransformResults(Files.asByteSource(scoresFile));
		XSLTHTMLGenerator gen = new XSLTHTMLGenerator(Constants.DATA_FILE_PREFIX + scoresFile.getName(), Files.getNameWithoutExtension(scoresFile.getName()),
				scores);

		gen.getHeaders().add("header.xml");
		gen.getFooters().add("footer.xml");
		gen.getStyleSheets().add("spka.css");
		if (scores.getData().getSeries().getName().startsWith("Celtic Challenge ")) {
			gen.getStyleSheets().add("spka-cc.css");
		}
		gen.getFlags().put("compact-race", "10");
		gen.getFlags().put("compact-event", "10");
		if (Constants.TOP_COUNTRY_SCORERS.contains(scores.getData().getSeriesResults().getScorer())) {
			gen.getFlags().put("top-country", null);
		}

		Map<String, String> classes = new TreeMap<String, String>(Ordering.explicit(new ArrayList<String>(CLASSES.keySet())));
		for (DataXMLClass class_ : scores.getData().getSeries().getClasses()) {
			if (CLASSES.containsKey(class_.getName())) {
				classes.put(class_.getName(), CLASSES.get(class_.getName()));
			}
		}
		gen.getClasses().putAll(classes);

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		scores.to(buf);
		buf.close();
		pages.put(Constants.DATA_FILE_PREFIX + scoresFile.getName(), ByteSource.wrap(buf.toByteArray()));

		for (String styleSheet : gen.getStyleSheets()) {
			pages.put(styleSheet, Resources.asByteSource(Resources.getResource(Constants.RESOURCE_PATH + styleSheet)));
		}

		pages.putAll(gen.getMenuPage());
		pages.putAll(gen.getSimplePage());
		pages.putAll(gen.getSplitPages());
		pages.putAll(gen.getCodePages());
	}

	public Map<String, ByteSource> getPages() {
		return pages;
	}
}
