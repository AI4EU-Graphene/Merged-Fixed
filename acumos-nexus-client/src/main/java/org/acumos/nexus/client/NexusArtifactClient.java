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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.acumos.nexus.client.data.UploadArtifactInfo;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamingWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class NexusArtifactClient {

	private final RepositoryLocation repositoryLocation;
	private final RestTemplate restTemplate;
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * Builds an instance of the client with the supplied repository location
	 * 
	 * @param repoLoc
	 *            Repository location including URL and credentials.
	 */
	public NexusArtifactClient(RepositoryLocation repoLoc) {
		this.repositoryLocation = repoLoc;
		
		// Build a restTemplate for use by the delete method
		URL url = null;
		try {
			url = new URL(repoLoc.getUrl());
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Failed to parse URL", ex);
		}
		final HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());

		// Build a client with a credentials provider
		CloseableHttpClient httpClient = null;
		if (repoLoc.getUsername() != null && repoLoc.getPassword() != null) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(httpHost),
					new UsernamePasswordCredentials(repoLoc.getUsername(), repoLoc.getPassword()));
			httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		} else {
			httpClient = HttpClientBuilder.create().build();
		}
		// Create request factory
		HttpComponentsClientHttpRequestFactoryBasicAuth requestFactory = new HttpComponentsClientHttpRequestFactoryBasicAuth(
				httpHost);
		requestFactory.setHttpClient(httpClient);

		// Put the factory in the template
		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);
	}

	/**
	 * Uploads an artifact from a stream.
	 * 
	 * @param groupId
	 *            GroupId where the Artifacts needs to stored.
	 * @param artifactId
	 *            ArtifactId is the name of the artifact
	 * @param version
	 *            Version of the artifact
	 * @param packaging
	 *            Packaging of the Artifact
	 * @param contentLength
	 *            ContentLength of the Artifact
	 * @param inputStream
	 *            InputStream containing artifact
	 * @return UploadArtifactInfo
	 * @throws AuthenticationException
	 *             On failure to authenticate
	 * @throws AuthorizationException
	 *             On failure to authorize
	 * @throws ConnectionException
	 *             On failure to connect
	 * @throws ResourceDoesNotExistException
	 *             On failure to find resource
	 * @throws TransferFailedException
	 *             On failure to transfer
	 */
	public UploadArtifactInfo uploadArtifact(String groupId, String artifactId, String version, String packaging,
			long contentLength, InputStream inputStream) throws AuthenticationException, AuthorizationException,
			ConnectionException, TransferFailedException, ResourceDoesNotExistException {
		StreamingWagon streamWagon = null;
		UploadArtifactInfo artifactInfo = null;
		try {
			String mvnPath = MvnRepoWagonConnectionManager.createMvnPath(groupId, artifactId, version, packaging);
			artifactInfo = new UploadArtifactInfo(groupId, artifactId, version, packaging, mvnPath, contentLength);
			streamWagon = MvnRepoWagonConnectionManager.createWagon(repositoryLocation);
			streamWagon.putFromStream(inputStream, mvnPath, contentLength, -1);
		} finally {
			IOUtil.close(inputStream);
			if (streamWagon != null)
				streamWagon.disconnect();
		}
		return artifactInfo;
	}

	/**
	 * Gets the artifact by path and stores in memory.
	 * 
	 * @param artifactReference
	 *            artifactPath
	 * @return Stream of bytes
	 * @throws AuthenticationException
	 *             On failure to authenticate
	 * @throws AuthorizationException
	 *             On failure to authorize
	 * @throws ConnectionException
	 *             On failure to connect
	 * @throws ResourceDoesNotExistException
	 *             On failure to find resource
	 * @throws TransferFailedException
	 *             On failure to transfer
	 */
	public ByteArrayOutputStream getArtifact(String artifactReference) throws AuthenticationException,
			ConnectionException, ResourceDoesNotExistException, TransferFailedException, AuthorizationException {
		StreamingWagon streamWagon = null;
		ByteArrayOutputStream outputStream = null;
		try {
			streamWagon = MvnRepoWagonConnectionManager.createWagon(repositoryLocation);
			outputStream = new ByteArrayOutputStream();
			streamWagon.getToStream(artifactReference, outputStream);
		} finally {
			IOUtil.close(outputStream);
			if (streamWagon != null)
				streamWagon.disconnect();
		}
		return outputStream;
	}

	/**
	 * Gets the artifact at the specified path and transfers to the specified output
	 * stream.
	 * 
	 * @param artifactReference
	 *            artifactPath
	 * @param outputStream
	 *            Output stream. Stream is closed when the transfer finishes.
	 * @throws AuthenticationException
	 *             On failure to authenticate
	 * @throws AuthorizationException
	 *             On failure to authorize
	 * @throws ConnectionException
	 *             On failure to connect
	 * @throws ResourceDoesNotExistException
	 *             On failure to find resource
	 * @throws TransferFailedException
	 *             On failure to transfer
	 */
	public void getArtifact(String artifactReference, OutputStream outputStream) throws AuthenticationException,
			ConnectionException, ResourceDoesNotExistException, TransferFailedException, AuthorizationException {
		StreamingWagon streamWagon = null;
		try {
			streamWagon = MvnRepoWagonConnectionManager.createWagon(repositoryLocation);
			streamWagon.getToStream(artifactReference, outputStream);
		} finally {
			IOUtil.close(outputStream);
			if (streamWagon != null)
				streamWagon.disconnect();
		}
	}

	/**
	 * Deletes Artifacts from Nexus Repository
	 * 
	 * @param artifactReference
	 *            Artifact path to be deleted
	 * @throws URISyntaxException
	 *             If full path cannot be parsed as URI
	 */
	public void deleteArtifact(String artifactReference) throws URISyntaxException {
		log.debug("deleteArtifact begin");
		if (artifactReference == null)
			throw new IllegalArgumentException("artifactReference cannot be null");
		if (restTemplate != null && artifactReference != null) {
			log.debug("deleteArtifact: artifactReference {}", artifactReference);
			URI url = new URI(repositoryLocation.getUrl() + (repositoryLocation.getUrl().endsWith("/") ? "" : "/")
					+ artifactReference);
			log.debug(" url : url {}", url);
			restTemplate.delete(url);
		}
	}

}
