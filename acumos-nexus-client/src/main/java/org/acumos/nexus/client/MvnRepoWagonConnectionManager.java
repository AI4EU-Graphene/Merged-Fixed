/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.nexus.client;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.StreamingWagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.ftp.FtpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagonAuthenticator;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;

/**
 * The Class MvnRepoWagonConnectionManager.
 */
public class MvnRepoWagonConnectionManager {

	private static StreamingWagon testWagon;

	/**
	 * Should not be instantiated.
	 */
	private MvnRepoWagonConnectionManager() {
	}

	/**
	 * Creates the wagon.
	 * 
	 * @param repo
	 *            the repository location
	 * @return the stream wagon
	 * @throws ConnectionException
	 *             the connection exception
	 * @throws AuthenticationException
	 *             the authentication exception
	 */
	public static StreamingWagon createWagon(RepositoryLocation repo)
			throws ConnectionException, AuthenticationException {

		if (testWagon != null)
			return testWagon;

		Repository repository = new Repository(repo.getId(), repo.getUrl());
		StreamWagon streamWagon;
		if (repo.getUrl().startsWith("http:")) {
			LightweightHttpWagon lw = new LightweightHttpWagon();
			lw.setAuthenticator(new LightweightHttpWagonAuthenticator());
			// Always send credentials
			lw.setPreemptiveAuthentication(true);
			streamWagon = lw;
		} else if (repo.getUrl().startsWith("https:")) {
			LightweightHttpsWagon lw = new LightweightHttpsWagon();
			lw.setAuthenticator(new LightweightHttpWagonAuthenticator());
			// Always send credentials
			lw.setPreemptiveAuthentication(true);
			streamWagon = lw;
		} else if (repo.getUrl().startsWith("ftp:"))
			streamWagon = new FtpWagon();
		else if (repo.getUrl().startsWith("file:"))
			streamWagon = new FileWagon();
		else
			throw new IllegalStateException("Unknown protocol in repository url: " + repo.getUrl());

		if (repo.getProxy() != null && !repo.getProxy().isEmpty()) {
			ProxyInfo proxy = new ProxyInfo();
			String all[] = repo.getProxy().split(":");
			proxy.setType(all[0]);
			proxy.setHost(all[1].replaceFirst("//", ""));
			proxy.setPort(Integer.valueOf(all[2]));
			streamWagon.connect(repository, proxy);
		} else {
			AuthenticationInfo authenticationInfo = new AuthenticationInfo();
			authenticationInfo.setUserName(repo.getUsername());
			authenticationInfo.setPassword(repo.getPassword());
			streamWagon.connect(repository, authenticationInfo);
		}

		return streamWagon;
	}

	/**
	 * Creates maven path for a given artifact.
	 * 
	 * @param groupId
	 *            GroupId for the Artifact
	 * @param artifactId
	 *            Artifact Id for the artifact
	 * @param version
	 *            Version for the artifact
	 * @param packaging
	 *            packaging for artifact e.g jar, war etc
	 * @return Maven path for the specified artifact
	 */
	public static String createMvnPath(String groupId, String artifactId, String version, String packaging) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(groupId.replace(".", "/")).append("/");
		buffer.append(artifactId).append("/");
		buffer.append(version).append("/");
		buffer.append(artifactId).append("-");
		buffer.append(version).append(".").append(packaging);
		return buffer.toString();
	}

	/**
	 * @param testWagon
	 *            the testWagon to set
	 */
	public static void setTestWagon(StreamingWagon testWagon) {
		MvnRepoWagonConnectionManager.testWagon = testWagon;
	}
}
