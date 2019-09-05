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
package org.spka.cursus.publish.website.results;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.spka.cursus.publish.website.Constants;
import org.spka.cursus.publish.website.ftp.FileCache;

import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;
import com.google.common.xml.XmlEscapers;

import uk.uuid.cursus.xml.ImportException;
import uk.uuid.cursus.xml.data.entity.DataXMLSeries;
import uk.uuid.cursus.xml.scores.ScoresXMLFile;

@SuppressWarnings("nls")
public class ResultsLinksGenerator {
	private static final String PREFIX = Constants.RESULTS_DIR + "/" + Constants.DATA_FILE_PREFIX;
	private static final Escaper URL_PATH = UrlEscapers.urlPathSegmentEscaper();
	private static final Escaper HTML = HtmlEscapers.htmlEscaper();
	private static final Escaper ENTITY = EntityEscaper.entityEscaper();
	private static final Escaper XML_CONTENT = XmlEscapers.xmlContentEscaper();
	private static final Escaper XML_ATTRIBUTE = XmlEscapers.xmlAttributeEscaper();
	private Map<String, ByteSource> pages = new HashMap<String, ByteSource>();

	public ResultsLinksGenerator(FileCache files) throws IOException, ImportException {
		Map<String, String> resultsReverse = new TreeMap<String, String>(new ResultsPageComparator(true));
		Map<String, String> resultsForward = new TreeMap<String, String>(new ResultsPageComparator(false));

		for (Map.Entry<String, ByteSource> file : files.entrySet()) {
			if (file.getKey().startsWith(PREFIX) && file.getKey().endsWith(".xml")) {
				String fileName = Files.getNameWithoutExtension(file.getKey().substring(PREFIX.length()));
				ScoresXMLFile scores = new ScoresXMLFile(file.getValue());
				String seriesName = createSeriesName(scores.getData().getSeries(), scores.getData().getSeriesResults().getScorer());

				if (seriesName != null) {
					resultsReverse.put(seriesName, fileName);
					resultsForward.put(seriesName, fileName);
				}
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, Charsets.ISO_8859_1));
		for (Map.Entry<String, String> result : resultsReverse.entrySet()) {
			String seriesName = result.getKey();
			String fileName = result.getValue();

			pw.append("<li>");
			pw.append("<a href=\"/results-uk2019/").append(HTML.escape(URL_PATH.escape(fileName + "s1.xml"))).append("\">");
			pw.append(ENTITY.escape(HTML.escape(seriesName))).append("</a>");
			pw.append(" (<a href=\"/results-uk2019/").append(HTML.escape(URL_PATH.escape(fileName + "a.xml"))).append("\">print</a>");
			pw.append(" | <a href=\"/results-uk2019/").append(HTML.escape(URL_PATH.escape(fileName + "t.xml"))).append("#s1\">tabs</a>)");
			pw.println("</li>");
		}
		pw.flush();
		out.close();
		pages.put(Constants.RESULTS_FILE, ByteSource.wrap(out.toByteArray()));

		out = new ByteArrayOutputStream();
		pw = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8));
		pw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		pw.println("<div id=\"footer\">");
		pw.println("\t<hr/>");
		pw.println(
				"\t<p><a href=\"/\" title=\"Scottish Power Kite Association\"><img src=\"/_images/spkalogo.gif\" alt=\"SPKA\"/><img src=\"/_images/activities.gif\" alt=\"\"/></a></p>");
		pw.println("\t<ul class=\"menu\">");
		for (Map.Entry<String, String> result : resultsForward.entrySet()) {
			String seriesName = result.getKey();
			String fileName = result.getValue();

			pw.append("\t\t<li> <a href=\"").append(XML_ATTRIBUTE.escape(URL_PATH.escape(fileName + "s1.xml"))).append("\">");
			pw.append(ENTITY.escape(XML_CONTENT.escape(seriesName))).append("</a> </li>");
		}
		pw.println("\t</ul>");
		pw.println("</div>");
		pw.flush();
		out.close();
		pages.put(Constants.RESULTS_DIR + "/footer.xml", ByteSource.wrap(out.toByteArray()));
	}

	public Map<String, ByteSource> getPages() {
		return pages;
	}

	private static String createSeriesName(DataXMLSeries series, String scorer) {
		if (series.getName().startsWith("SPKA Race Series ")) {
			return series.getName().substring(5);
		}

		if (series.getName().startsWith("4 Nations Championship ")) {
			return series.getName().split("\\(")[1].split("\\)")[0];
		}

		if (series.getName().startsWith("Celtic Challenge ")) {
			if (Constants.TOP_COUNTRY_SCORERS.contains(scorer)) {
				return series.getName() + " – Top Country";
			} else {
				return series.getName() + " – Normal Scoring";
			}
		}

		return null;
	}

	private static class ResultsPageComparator implements Comparator<String> {
		private static final String ALL = "Combined";
		private static final String MEN = "Men";
		private static final String WOMEN = "Women";
		private static final String COUNTRY = "Top Country";

		public ResultsPageComparator(boolean reverse) {

		}

		@Override
		public int compare(String o1, String o2) {
			return ComparisonChain.start().compareTrueFirst(o1.equals(ALL), o2.equals(ALL)).compareTrueFirst(o1.equals(MEN), o2.equals(MEN))
					.compareTrueFirst(o1.equals(WOMEN), o2.equals(WOMEN)).compareTrueFirst(o1.equals(COUNTRY), o2.equals(COUNTRY)).compare(o1, o2).result();
		}
	}
}
