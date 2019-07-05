/*
	cursus - Race series management program
	Copyright 2019  Simon Arlott

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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

public class HostKeys implements HostKeyRepository {
	private List<HostKey> keys = new ArrayList<>();

	public HostKeys() throws JSchException {
		keys.add(new HostKey("kite.spka.uk", HostKey.SSHRSA, Base64.getDecoder().decode( //$NON-NLS-1$
				"AAAAB3NzaC1yc2EAAAADAQABAAACAQC94OMfT6fV9Ie0eQQSrYOJv/RxRfcBUJysKtWNgexIZO8alG1+JEVv8i3uaC9ypanye8nj73r6+kPSAeNWYDE1QcAckhpZRhjzdn64nlgaz3xwpbVPXNF489HeAScYWkEOAbkQLat26hNx1AMnXM+SwExsw2VhltwEKthyd1ofa+wudBmeM5MSfB4tai6z95csR+edafUpyIClhk723tXULDO4gx3yZwjJ68VfhsC6bfD3UyE4XwOKcJyTx2dTRTk99d7ifIgrAAFGf7GF5fqxggdN2GNi5kXQ8XdafCbjQEsvU57POp7BGQNjYZIDZOcDax7a76o52Tg9vynu80AMr622kQTSY94Y2hiMNWENEvvvT2YsXyzuwLCeKdNc7Dsw7B4ltSZnPv6ct5nLk2zZty/txsEapyirqXHZOOFoPP+mrkHwHShWDc7v6VgthwYjFi0ZgaphLXFVYbeJXS+lS9kYSfPxVQj3iPDRRVYAsykHOW/HgJJz+lolnBm0LFpfXe1HXDctToi89j+K4Xg7CPxKO1GdZ6EssA0YQkZWWpAtCk+Dh/TKQnzt4OVBwQDbmyLDOZz5nNyd3+jrKnWztYIQEtZo4LdFEqE4gXqT0/FqZF+Ka7fW3SQAPJdK/t3V3vlX12JEmIMHP4+RAMgthjL3bhfb4elwe+MnPGLKYQ=="))); //$NON-NLS-1$
	}

	@Override
	public int check(String host, byte[] key) {
		HostKey checkKey;
		try {
			checkKey = new HostKey(host, key);
		} catch (JSchException e) {
			return HostKeyRepository.NOT_INCLUDED;
		}
		return keys.stream().anyMatch(hostKey -> hostKey.getHost().equals(host) && hostKey.getKey().equals(checkKey.getKey())) ? HostKeyRepository.OK
				: HostKeyRepository.NOT_INCLUDED;
	}

	@Override
	public void add(HostKey hostkey, UserInfo ui) {
	}

	@Override
	public void remove(String host, String type) {
	}

	@Override
	public void remove(String host, String type, byte[] key) {
	}

	@Override
	public String getKnownHostsRepositoryID() {
		return getClass().getCanonicalName();
	}

	@Override
	public HostKey[] getHostKey() {
		return keys.toArray(new HostKey[0]);
	}

	@Override
	public HostKey[] getHostKey(String host, String type) {
		return keys.stream().filter(key -> key.getType().equals(type)).collect(Collectors.toList()).toArray(new HostKey[0]);
	}
}
