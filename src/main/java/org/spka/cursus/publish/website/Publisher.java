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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.spka.cursus.publish.website.ftp.DownloadDataFiles;
import org.spka.cursus.publish.website.ftp.FileCache;
import org.spka.cursus.publish.website.ftp.ListFiles;
import org.spka.cursus.publish.website.ftp.Uploader;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import eu.lp0.cursus.xml.ExportException;
import eu.lp0.cursus.xml.ImportException;

@SuppressWarnings("nls")
public class Publisher implements FTPActivity {
	private Map<String, ByteSource> files = new LinkedHashMap<String, ByteSource>();

	public Publisher(Collection<File> files) throws ImportException, ExportException, IOException {
		this.files.put(".htaccess", Resources.asByteSource(Resources.getResource(Constants.RESOURCE_PATH + ".htaccess")));

		for (File file : files) {
			this.files.putAll(new ResultsPagesGenerator(file).getPages());
		}
	}

	@Override
	public boolean exec(FTPClient ftp) throws IOException {
		FileCache fileCache = new FileCache();

		ListFiles ls = new ListFiles(fileCache);
		ls.from(ftp);

		Uploader ul = new Uploader(fileCache);
		ul.to(ftp, files);

		DownloadDataFiles dl = new DownloadDataFiles(fileCache);
		dl.from(ftp);

		return false;
	}
}
