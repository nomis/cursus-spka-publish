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
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.google.common.io.ByteSource;

@SuppressWarnings("nls")
public class Uploader {
	private final FileCache existingFiles;

	public Uploader(FileCache existingFiles) {
		this.existingFiles = existingFiles;
	}

	public void to(FTPClient ftp, Map<String, ByteSource> newFiles) throws IOException {
		if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
			throw new IllegalStateException("Unable to set mode to binary");
		}

		for (Map.Entry<String, ByteSource> file : newFiles.entrySet()) {
			ByteSource other = null;

			if (existingFiles.containsKey(file.getKey())) {
				other = existingFiles.get(file.getKey());

				if (other == null) {
					ByteArrayOutputStream buf = new ByteArrayOutputStream();
					if (!ftp.retrieveFile(file.getKey(), buf)) {
						throw new IllegalStateException("Unable to retrieve " + file.getKey());
					}
					buf.close();
					other = ByteSource.wrap(buf.toByteArray());
					existingFiles.put(file.getKey(), other);
				}
			}

			if (other == null || !file.getValue().contentEquals(other)) {
				InputStream in = file.getValue().openStream();
				if (!ftp.storeFile(file.getKey(), in)) {
					throw new IllegalStateException("Unable to store " + file.getKey());
				}
				in.close();

				existingFiles.put(file.getKey(), file.getValue());
			}
		}
	}
}
