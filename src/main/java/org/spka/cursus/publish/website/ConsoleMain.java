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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.annotation.Nullable;

import jline.console.ConsoleReader;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import eu.lp0.cursus.xml.ExportException;
import eu.lp0.cursus.xml.ImportException;

@SuppressWarnings("nls")
public class ConsoleMain {
	private static final Preferences prefs = Preferences.userNodeForPackage(ConsoleMain.class);
	private ConsoleReader console;
	private String hostname;
	private String username;
	private String password;
	private FTPClient ftp = new FTPClient();

	public String getPref(String name, String prompt) throws IOException {
		return getPref(name, prompt, false);
	}

	public String getPref(String name, String prompt, boolean hidden) throws IOException {
		String value = prefs.get(name, null);
		if (value != null) {
			return value;
		}

		value = console.readLine(prompt + ": ", hidden ? '*' : null);
		if (value == null) {
			throw new IllegalArgumentException(prompt + " required");
		}

		prefs.put(name, value);
		return value;
	}

	public ConsoleMain() throws IOException {
		console = new ConsoleReader();
		console.setHandleUserInterrupt(true);
	}

	public boolean run(String[] files) throws IOException, ImportException, ExportException {
		return run(Lists.transform(Arrays.asList(files), new Function<String, File>() {
			@Override
			@Nullable
			public File apply(@Nullable String file) {
				return new File(file);
			}
		}));
	}

	public boolean run(List<File> files) throws IOException, ImportException, ExportException {
		return ftp(new Publisher(files));
	}

	private boolean ftp(FTPActivity activity) throws IOException {
		hostname = getPref("hostname", "FTP Hostname");
		username = getPref("username", "FTP Username");
		password = getPref("password", "FTP Password", true);

		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

		console.println("Connecting to " + hostname + "...");
		try {
			ftp.connect(hostname);
		} catch (IOException e) {
			prefs.remove("hostname");
			throw e;
		}
		// ftp.enterLocalPassiveMode();

		boolean okay = false;
		boolean ret;
		try {
			if (!ftp.login(username, password)) {
				prefs.remove("username");
				prefs.remove("password");
				okay = true;
				return false;
			}

			ret = activity.exec(ftp);
			okay = true;
		} finally {
			try {
				ftp.disconnect();
			} catch (IOException e) {
				if (okay) {
					throw e;
				}
			}
		}
		return ret;
	}

	public static void main(String[] args) throws IOException, ImportException, ExportException {
		System.exit(new ConsoleMain().run(args) ? 0 : 1);
	}
}
