/*
	cursus - Race series management program
	Copyright 2014,2019  Simon Arlott

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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.spka.cursus.publish.website.ftp.Activity;
import org.spka.cursus.publish.website.ftp.DownloadDataFiles;
import org.spka.cursus.publish.website.ftp.FileCache;
import org.spka.cursus.publish.website.ftp.ListFiles;
import org.spka.cursus.publish.website.ftp.PrepareDirectory;
import org.spka.cursus.publish.website.ftp.Uploader;
import org.spka.cursus.publish.website.results.ResultsLinksGenerator;
import org.spka.cursus.publish.website.results.ResultsPagesGenerator;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

import uk.uuid.cursus.xml.ExportException;
import uk.uuid.cursus.xml.ImportException;

@SuppressWarnings("nls")
public class Publisher implements Activity {
	private Map<String, ByteSource> files = new LinkedHashMap<String, ByteSource>();
	private FTPClient ftp;
	private ChannelSftp sftp;

	private static final FTPFileFilter FILES_ONLY = new FTPFileFilter() {
		@Override
		public boolean accept(FTPFile file) {
			return file.isFile();
		}
	};

	private Publisher(Collection<File> files) throws ImportException, ExportException, IOException {
		this.files.put(Constants.RESULTS_DIR + "/.htaccess", Resources.asByteSource(Resources.getResource(Constants.RESOURCE_PATH + ".htaccess")));

		for (File file : files) {
			this.files.putAll(new ResultsPagesGenerator(file).getPages());
		}
	}

	public Publisher(Collection<File> files, FTPClient ftp) throws ImportException, ExportException, IOException {
		this(files);
		this.ftp = ftp;
	}

	public Publisher(Collection<File> files, ChannelSftp sftp) throws ImportException, ExportException, IOException {
		this(files);
		this.sftp = sftp;
	}

	public boolean exec() throws IOException, ImportException {
		FileCache fileCache = new FileCache();

		new PrepareDirectory().on(this);

		ListFiles ls = new ListFiles(fileCache);
		ls.from(this);

		Uploader ul = new Uploader(fileCache);
		ul.to(this, files);

		DownloadDataFiles dl = new DownloadDataFiles(fileCache);
		dl.from(this);

		files.clear();
		files.putAll(new ResultsLinksGenerator(fileCache).getPages());
		ul.to(this, files);

		return false;
	}

	@Override
	public boolean setFileType(int fileType) throws IOException {
		if (ftp != null) {
			return ftp.setFileType(fileType);
		} else if (sftp != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean retrieveFile(String fileName, OutputStream out) throws IOException {
		if (ftp != null) {
			return ftp.retrieveFile(Constants.FTP_WWW_DIR + fileName, out);
		} else if (sftp != null) {
			try {
				fileName = Constants.SFTP_WWW_DIR + fileName;
				System.out.println("get " + fileName);
				sftp.get(fileName, out);
				return true;
			} catch (SftpException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listFiles(String dir) throws IOException {
		if (ftp != null) {
			return Arrays.stream(ftp.listFiles(Constants.FTP_WWW_DIR + dir, FILES_ONLY)).map(ftpFile -> ftpFile.getName()).collect(Collectors.toList());
		} else if (sftp != null) {
			try {
				dir = Constants.SFTP_WWW_DIR + dir;
				System.out.println("ls " + dir);
				return ((Vector<LsEntry>)sftp.ls(dir)).stream().filter(file -> file.getAttrs().isReg()).map(file -> file.getFilename())
						.collect(Collectors.toList());
			} catch (SftpException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean changeWorkingDirectory(String dir) throws IOException {
		if (ftp != null) {
			return ftp.changeWorkingDirectory(Constants.FTP_WWW_DIR + dir);
		} else if (sftp != null) {
			try {
				dir = Constants.SFTP_WWW_DIR + dir;
				System.out.println("cd " + dir);
				sftp.cd(dir);
			} catch (SftpException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean makeDirectory(String dir) throws IOException {
		if (ftp != null) {
			return ftp.makeDirectory(Constants.FTP_WWW_DIR + dir);
		} else if (sftp != null) {
			try {
				dir = Constants.SFTP_WWW_DIR + dir;
				System.out.println("mkdir " + dir);
				sftp.mkdir(dir);
			} catch (SftpException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean storeFile(String fileName, InputStream in) throws IOException {
		if (ftp != null) {
			return ftp.storeFile(Constants.FTP_WWW_DIR + fileName, in);
		} else if (sftp != null) {
			try {
				fileName = Constants.SFTP_WWW_DIR + fileName;
				System.out.println("put " + fileName);
				sftp.put(in, fileName);
				return true;
			} catch (SftpException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}
