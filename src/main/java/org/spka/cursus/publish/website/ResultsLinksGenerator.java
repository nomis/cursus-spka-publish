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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.spka.cursus.publish.website.ftp.FileCache;

import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.net.UrlEscapers;

import eu.lp0.cursus.xml.ImportException;
import eu.lp0.cursus.xml.data.entity.DataXMLSeries;
import eu.lp0.cursus.xml.scores.ScoresXMLFile;

@SuppressWarnings("nls")
public class ResultsLinksGenerator {
	private static final String PREFIX = Constants.RESULTS_DIR + "/" + Constants.DATA_FILE_PREFIX;
	private static final Escaper URL_PATH = UrlEscapers.urlPathSegmentEscaper();
	private static final Escaper HTML = HtmlEscapers.htmlEscaper();
	private Map<String, ByteSource> pages = new HashMap<String, ByteSource>();

	public ResultsLinksGenerator(FileCache files) throws IOException, ImportException {
		Map<String, String> results = new TreeMap<String, String>(new ResultsPageComparator());

		for (Map.Entry<String, ByteSource> file : files.entrySet()) {
			if (file.getKey().startsWith(PREFIX) && file.getKey().endsWith(".xml")) {
				String fileName = Files.getNameWithoutExtension(file.getKey().substring(PREFIX.length()));
				ScoresXMLFile scores = new ScoresXMLFile(file.getValue());
				String seriesName = createSeriesName(scores.getData().getSeries(), scores.getData().getSeriesResults().getScorer());

				if (seriesName != null) {
					results.put(seriesName, fileName);
				}
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resources.asByteSource(Resources.getResource(Constants.RESOURCE_PATH + "results.shtml_header")).copyTo(out);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Charsets.ISO_8859_1));
		for (Map.Entry<String, String> result : results.entrySet()) {
			String seriesName = result.getKey();
			String fileName = result.getValue();

			pw.append("\t<li>");
			pw.append("<a href=\"results/").append(URL_PATH.escape(fileName + "s1.xml")).append("\">");
			pw.append(HTML.escape(seriesName).replace("—", "&ndash;")).append("</a>");
			pw.append(" (<a href=\"results/").append(URL_PATH.escape(fileName + "a.xml")).append("\">print</a>");
			pw.append(" | <a href=\"results/").append(URL_PATH.escape(fileName + "t.xml")).append("#s1\">tabs</a>)");
			pw.println("</li>");
		}
		pw.flush();
		Resources.asByteSource(Resources.getResource(Constants.RESOURCE_PATH + "results.shtml_footer")).copyTo(out);
		out.close();
		pages.put("results.shtml", ByteSource.wrap(out.toByteArray()));
	}

	public Map<String, ByteSource> getPages() {
		return pages;
	}

	private static String createSeriesName(DataXMLSeries series, String scorer) {
		if (series.getName().startsWith("SPKA Race Series ")) {
			return series.getName().substring(5);
		}

		if (series.getName().startsWith("Celtic Challenge ")) {
			if (Constants.TOP_COUNTRY_SCORERS.contains(scorer)) {
				return series.getName() + " — Top Country";
			} else {
				return series.getName() + " — Normal Scoring";
			}
		}

		return null;
	}

	private static class ResultsPageComparator implements Comparator<String> {
		private static final String SPKA_RACE_SERIES = "Race Series ";

		@Override
		public int compare(String o1, String o2) {
			String s1 = o1.split("—", 2)[0];
			String s2 = o2.split("—", 2)[0];
			return ComparisonChain.start().compareTrueFirst(o1.startsWith(SPKA_RACE_SERIES), o2.startsWith(SPKA_RACE_SERIES)).compare(s2, s1).compare(o1, o2)
					.result();
		}
	}
}
