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
package org.spka.cursus.publish.website.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.spka.cursus.publish.website.Constants;

@SuppressWarnings("nls")
public class PrepareDirectory {
	public PrepareDirectory() {
	}

	public void at(FTPClient ftp) throws IOException {
		if (!ftp.changeWorkingDirectory(Constants.RESULTS_DIR)) {
			if (!ftp.makeDirectory(Constants.RESULTS_DIR)) {
				throw new IllegalStateException("Unable to create results dir");
			}

			if (!ftp.changeWorkingDirectory(Constants.RESULTS_DIR)) {
				throw new IllegalStateException("Unable to change to results dir");
			}
		}
	}
}
