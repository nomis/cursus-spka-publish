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

import java.util.Set;

import org.spka.cursus.scoring.CCConstants;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("nls")
public class Constants {
	public static final String RESOURCE_PATH = "org/spka/cursus/publish/website/";

	public static final String WWW_DIR = "/public_html";
	public static final String RESULTS_FILE = WWW_DIR + "/results.shtml";
	public static final String RESULTS_DIR = WWW_DIR + "/results";
	public static final String DATA_FILE_PREFIX = "__";

	public static final String PILOT_MAP_FILE = "pilots.map";

	public static final Set<String> TOP_COUNTRY_SCORERS = ImmutableSet.of(CCConstants.UUID_2008, CCConstants.UUID_2009, CCConstants.UUID_2013);
}
