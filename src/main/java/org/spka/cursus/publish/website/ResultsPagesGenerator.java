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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.spka.cursus.scoring.CCConstants;

import com.google.common.collect.ImmutableSet;
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
	private static final Set<String> TOP_COUNTRY_SCORERS = ImmutableSet.of(CCConstants.UUID_2008, CCConstants.UUID_2009, CCConstants.UUID_2013);
	private static final String DATA_FILE_PREFIX = "__";

	private Map<String, ByteSource> pages = new LinkedHashMap<String, ByteSource>();

	public ResultsPagesGenerator(File scoresFile) throws IOException, ImportException, ExportException {
		ScoresXMLFile scores = new ScoresXMLFile(Files.asByteSource(scoresFile));
		XSLTHTMLGenerator gen = new XSLTHTMLGenerator(DATA_FILE_PREFIX + scoresFile.getName(), Files.getNameWithoutExtension(scoresFile.getName()), scores);

		gen.getHeaders().add("header.xml");
		gen.getFooters().add("footer.xml");
		gen.getStyleSheets().add("spka.css");
		if (scores.getData().getSeries().getName().startsWith("Celtic Challenge ")) {
			gen.getStyleSheets().add("spka-cc.css");
		}
		gen.getFlags().put("compact-race", "10");
		gen.getFlags().put("compact-event", "10");
		if (TOP_COUNTRY_SCORERS.contains(scores.getData().getSeriesResults().getScorer())) {
			gen.getFlags().put("top-country", null);
		}

		for (DataXMLClass class_ : scores.getData().getSeries().getClasses()) {
			if (class_.getName().equals("16\" Wheel")) { //$NON-NLS-1$
				gen.getClasses().put(class_.getName(), "16\""); //$NON-NLS-1$
			} else if (class_.getName().equals("Junior")) { //$NON-NLS-1$
				gen.getClasses().put(class_.getName(), class_.getName());
			}
		}

		pages.put(DATA_FILE_PREFIX + scoresFile.getName(), Files.asByteSource(scoresFile));

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
