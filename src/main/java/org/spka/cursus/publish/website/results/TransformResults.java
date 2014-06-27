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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.spka.cursus.publish.website.Constants;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import eu.lp0.cursus.xml.ImportException;
import eu.lp0.cursus.xml.data.entity.DataXMLEvent;
import eu.lp0.cursus.xml.data.entity.DataXMLPilot;
import eu.lp0.cursus.xml.scores.ScoresXMLFile;

@SuppressWarnings("nls")
public class TransformResults extends ScoresXMLFile {
	private static final Map<String, String> PILOT_NAMES = new HashMap<String, String>();
	static {
		try {
			for (String line : Files.readLines(new File(Constants.PILOT_MAP_FILE), Charsets.UTF_8)) {
				String[] mapping = line.split("=", 2);
				if (mapping.length == 2) {
					PILOT_NAMES.put(mapping[0], mapping[1]);
				}
			}
		} catch (IOException e) {
			Throwables.propagate(e);
		}
	}

	public TransformResults(ByteSource data) throws ImportException {
		super(data);

		for (DataXMLPilot pilot : getData().getSeries().getPilots()) {
			String replacementName = PILOT_NAMES.get(pilot.getName());
			if (replacementName != null) {
				pilot.setName(replacementName);
			}
		}

		for (DataXMLEvent event : getData().getSeries().getEvents()) {
			if (event.getName().startsWith("Race Event ")) {
				event.setName(event.getName().substring(5));
			}
		}
	}
}
