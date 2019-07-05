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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.annotation.Nullable;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

import jline.console.ConsoleReader;
import uk.uuid.cursus.xml.ExportException;
import uk.uuid.cursus.xml.ImportException;

@SuppressWarnings("nls")
public class ConsoleMain {
	private static final Preferences prefs = Preferences.userNodeForPackage(ConsoleMain.class);
	private ConsoleReader console;
	private String hostname;
	private String username;
	private String password;

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

	public boolean run(String[] files) throws Exception {
		return run(Lists.transform(Arrays.asList(files), new Function<String, File>() {
			@Override
			@Nullable
			public File apply(@Nullable String file) {
				return new File(file);
			}
		}));
	}

	public boolean run(List<File> files) throws Exception {
		return sftp(files);
	}

	@SuppressWarnings("unused")
	private boolean ftp(List<File> files) throws IOException, ImportException, ExportException {
		FTPClient ftp = new FTPClient();

		hostname = getPref("hostname", "FTP Hostname");
		username = getPref("username", "FTP Username");
		password = getPref("password", "FTP Password", true);

		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

		System.out.println("Connecting to " + hostname + "...");
		try {
			ftp.connect(hostname);
		} catch (IOException e) {
			throw e;
		}
		ftp.enterLocalPassiveMode();

		boolean okay = false;
		boolean ret;
		try {
			if (!ftp.login(username, password)) {
				prefs.remove("password");
				okay = true;
				return false;
			}

			ret = new Publisher(files, ftp).exec();
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

	private boolean sftp(List<File> files) throws IOException, ImportException, ExportException, JSchException, AgentProxyException {
		JSch jsch = new JSch();
		JSch.setConfig("PreferredAuthentications", "publickey");
		jsch.setIdentityRepository(new RemoteIdentityRepository(new SSHAgentConnector(new JNAUSocketFactory())));

		hostname = getPref("sftp-hostname", "SFTP Hostname");
		username = getPref("sftp-username", "SFTP Username");

		Session session = jsch.getSession(username, hostname);
		session.setHostKeyRepository(new HostKeys());

		ChannelSftp sftp;
		System.out.println("Connecting to " + hostname + "...");
		try {
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp)channel;
		} catch (JSchException e) {
			throw e;
		}

		return new Publisher(files, sftp).exec();
	}

	public static void main(String[] args) {
		try {
			System.exit(new ConsoleMain().run(args) ? 0 : 1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
