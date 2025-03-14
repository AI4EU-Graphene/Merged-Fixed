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

package org.acumos.nexus.client.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.RepositoryLocation;
import org.acumos.nexus.client.data.UploadArtifactInfo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Realistic tests require a functioning remote server. They are marked as
 * Ignore here.
 */
public class NexusArtifactClientTest {

	private static final Logger logger = LoggerFactory.getLogger(NexusArtifactClientTest.class);

	private final String REPO_URL = "http://central.maven.org/maven2";
	private final String artifactPath = "ch/qos/logback/logback-classic/1.1.11/logback-classic-1.1.11.jar";
	private final String REPO_USER = "test";
	private final String REPO_PASS = "test";
	private final String groupId = "g";
	private final String artifactId = "a";
	private final String version = "v";
	private final String packaging = "p";
	private final String artifactMvnPath = "m";

	@Test
	public void testUploadArtifactInfo() {
		logger.info("Testing uploadArtifactInfo");
		final long contentlength = 42L;
		UploadArtifactInfo uai = new UploadArtifactInfo(groupId, artifactId, version, packaging, artifactMvnPath,
				contentlength);
		Assert.assertEquals(groupId, uai.getGroupId());
		Assert.assertEquals(artifactId, uai.getArtifactId());
		Assert.assertEquals(version, uai.getVersion());
		Assert.assertEquals(packaging, uai.getPackaging());
		Assert.assertEquals(artifactMvnPath, uai.getArtifactMvnPath());
		Assert.assertEquals(contentlength, uai.getContentlength());
		uai.setGroupId(groupId);
		uai.setArtifactId(artifactId);
		uai.setVersion(version);
		uai.setPackaging(packaging);
		uai.setArtifactMvnPath(artifactMvnPath);
		uai.setContentlength(contentlength);
		Assert.assertEquals(groupId, uai.getGroupId());
		Assert.assertEquals(artifactId, uai.getArtifactId());
		Assert.assertEquals(version, uai.getVersion());
		Assert.assertEquals(packaging, uai.getPackaging());
		Assert.assertEquals(artifactMvnPath, uai.getArtifactMvnPath());
		Assert.assertEquals(contentlength, uai.getContentlength());
	}

	@Test
	public void testRepositoryLocation() {
		logger.info("Testing repositoryLocation");
		final String proxy = "proxy:proxy"; // IDK
		RepositoryLocation rl = new RepositoryLocation(groupId, REPO_URL, REPO_USER, REPO_PASS, proxy);
		Assert.assertEquals(groupId, rl.getId());
		Assert.assertEquals(REPO_URL, rl.getUrl());
		Assert.assertEquals(REPO_USER, rl.getUsername());
		Assert.assertEquals(REPO_PASS, rl.getPassword());
		Assert.assertEquals(proxy, rl.getProxy());
		rl = new RepositoryLocation();
		rl.setId(groupId);
		rl.setUrl(REPO_URL);
		rl.setUsername(REPO_USER);
		rl.setPassword(REPO_PASS);
		rl.setProxy(proxy);
		Assert.assertEquals(groupId, rl.getId());
		Assert.assertEquals(REPO_URL, rl.getUrl());
		Assert.assertEquals(REPO_USER, rl.getUsername());
		Assert.assertEquals(REPO_PASS, rl.getPassword());
		Assert.assertEquals(proxy, rl.getProxy());
	}

	@Test
	public void testGetArtifact() throws Exception {
		NexusArtifactClient artifactClient = new NexusArtifactClient(
				new RepositoryLocation("0", REPO_URL, REPO_USER, REPO_PASS, null));

		// Get in memory
		ByteArrayOutputStream outputStream = artifactClient.getArtifact(artifactPath);
		Assert.assertNotNull(outputStream);
		final int length = outputStream.size();
		logger.info("Path " + artifactPath + " yielded memory byte count " + length);

		// Get to stream
		outputStream = new ByteArrayOutputStream();
		artifactClient.getArtifact(artifactPath, outputStream);
		Assert.assertEquals(length, outputStream.size());
		logger.info("Path " + artifactPath + " yielded stream byte count " + outputStream.size());
	}

	@Ignore // must not commit valid username/password that would allow an upload!
	@Test
	public void testUploadArtifact() throws Exception {
		NexusArtifactClient artifactClient = new NexusArtifactClient(
				new RepositoryLocation("1", REPO_URL, REPO_USER, REPO_PASS, null));
		String content = "artifact content goes here";
		InputStream inputStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
		try {
			UploadArtifactInfo artifactInfo = artifactClient.uploadArtifact("org.acumos", "uploadArtifact",
					"1.0.0-SNAPSHOT", "txt", content.length(), inputStream);
			logger.debug("Uploaded Artifact: " + artifactInfo.getArtifactMvnPath());
		} catch (Exception ex) {
			logger.error("upload failed", ex);
		}
	}

	@Test
	public void testDeleteArtifact() throws URISyntaxException {
		NexusArtifactClient artifactClient = new NexusArtifactClient(
				new RepositoryLocation("1", REPO_URL, REPO_USER, REPO_PASS, null));
		try {
			artifactClient.deleteArtifact(artifactPath);
		} catch (HttpClientErrorException ex) {
			logger.info("Delete failed as expected");
		}
	}
}
