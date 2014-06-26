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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.spka.cursus.publish.website.Constants;

import com.google.common.io.ByteSource;

@SuppressWarnings("nls")
public class DownloadDataFiles {
	private FileCache files;

	public DownloadDataFiles(FileCache files) {
		this.files = files;
	}

	public void from(FTPClient ftp) throws IOException {
		if (!ftp.changeWorkingDirectory(Constants.RESULTS_DIR)) {
			throw new IllegalStateException("Unable to change to results dir");
		}

		if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
			throw new IllegalStateException("Unable to set mode to binary");
		}

		for (String fileName : files.keySet()) {
			if (fileName.startsWith("__") && fileName.endsWith(".xml")) {
				if (files.get(fileName) == null) {
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					if (!ftp.retrieveFile(fileName, buf)) {
						throw new IllegalStateException("Unable to retrieve " + fileName);
					}
					buf.close();
					files.put(fileName, ByteSource.wrap(buf.toByteArray()));
				}
			}
		}
	}
}
