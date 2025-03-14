/*-
 * ===============LICENSE_START================================================
 * Acumos Apache-2.0
 * ============================================================================
 * Copyright (C) 2019 Nordix Foundation.
 * Modifications copyright (C)2020 Tech Mahindra.
 * ============================================================================
 * This Acumos software file is distributed by Nordix Foundation
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
 * ===============LICENSE_END==================================================
 */

package org.acumos.licensemanager.client;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.acumos.cds.client.CommonDataServiceRestClientMockImpl;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPCatalog;
import org.acumos.licensemanager.client.model.RegisterAssetRequest;
import org.acumos.licensemanager.client.model.RegisterAssetResponse;
import org.acumos.licensemanager.client.rtu.LicenseAsset;
import org.acumos.lum.model.GetAssetUsageAgreementResponse;
import org.acumos.lum.model.GetEntitledSwidTagsResponse;
import org.acumos.lum.model.PutAssetUsageAgreementRequest;
import org.acumos.lum.model.PutAssetUsageAgreementResponse;
import org.acumos.lum.model.PutAssetUsageAgreementRestrictionRequest;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.RepositoryLocation;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class LicenseAssetTest {

  private static final String LUM_SERVER = "http://lum-server.com";
  private static final String NEXUS_SERVER = "http://nexus-server.com";

  private static final String SWIDTAGID = UUID.randomUUID().toString();
  private static final String SWIDTAGID2 = UUID.randomUUID().toString();

  private class MockDatabaseClient extends CommonDataServiceRestClientMockImpl {}

  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(
          dsl(
              service(LUM_SERVER)
                  .put("/api/v1/swid-tag")
                  .queryParam("swTagId", SWIDTAGID2)
                  .anyBody()
                  .willReturn(serverError()),
              service(LUM_SERVER)
                  .put("/api/v1/swid-tag")
                  .queryParam("swTagId", SWIDTAGID)
                  .anyBody()
                  .willReturn(success("{}", "application/json")),
              service(LUM_SERVER)
                  .get("/api/v1/swid-tags/available-entitlement")
                  .queryParam("userId", "admin")
                  .queryParam("action", "aggregate")
                  .anyBody()
                  .willReturn(
                      success(
                          "{"
                              + "\"userId\": \"admin\","
                              + "\"requestId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\","
                              + "\"requested\": \"2020-03-30T15:13:25.670Z\","
                              + "\"action\": \"acumos:download\","
                              + "\"swidTagsWithAvailableEntitlement\": ["
                              + "{"
                              + "\"softwareLicensorId\": \"Company A\","
                              + "\"swTagId\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"swidTagRevision\": 5,"
                              + "\"swPersistentId\": \"c0de3e70-e815-461f-9734-a239c450bf77\","
                              + "\"licenseProfileId\": \"c75ddb24-0410-49f4-8550-98b06f9a0a51\","
                              + "\"availableEntitlement\": {"
                              + "\"rightToUseId\": \"acumos://software-licensor/Company A/permission/98378924-84ff-41f5-87ac-02fd2012c727\","
                              + "\"rightToUseRevision\": 3,"
                              + "\"assetUsageAgreementId\": \"acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106\","
                              + "\"assetUsageAgreementRevision\": 3"
                              + "}"
                              + "}"
                              + "]"
                              + "}",
                          "application/json")),
              service(LUM_SERVER)
                  .get("/api/v1/asset-usage-agreement")
                  .queryParam("softwareLicensorId", "Company A")
                  .queryParam(
                      "assetUsageAgreementId",
                      "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106")
                  .anyBody()
                  .willReturn(
                      success(
                          "{"
                              + "\"requestId\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"requested\": \"2020-03-30T15:13:25.670Z\","
                              + "\"assetUsageAgreement\": {"
                              + "\"softwareLicensorId\": \"Company A\","
                              + "\"assetUsageAgreementId\": \"acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106\","
                              + "\"agreement\": {"
                              + "\"uid\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"profile\": \"{}\" "
                              + "}"
                              + "}"
                              + "}",
                          "application/json")),
              service(LUM_SERVER)
                  .put("/api/v1/asset-usage-agreement")
                  .queryParam("softwareLicensorId", "Company A")
                  .queryParam(
                      "assetUsageAgreementId",
                      "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106")
                  .anyBody()
                  .willReturn(
                      success(
                          "{"
                              + "\"requestId\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"requested\": \"2020-03-30T15:13:25.670Z\","
                              + "\"assetUsageAgreement\": {"
                              + "\"softwareLicensorId\": \"Company A\","
                              + "\"assetUsageAgreementId\": \"acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106\","
                              + "\"agreement\": {"
                              + "\"uid\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"profile\": \"{}\" "
                              + "}"
                              + "}"
                              + "}",
                          "application/json")),
              service(LUM_SERVER)
                  .put("/api/v1/asset-usage-agreement-restriction")
                  .queryParam("softwareLicensorId", "Company A")
                  .queryParam(
                      "assetUsageAgreementId",
                      "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106")
                  .anyBody()
                  .willReturn(
                      success(
                          "{"
                              + "\"requestId\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"requested\": \"2020-03-30T15:13:25.670Z\","
                              + "\"assetUsageAgreement\": {"
                              + "\"softwareLicensorId\": \"Company A\","
                              + "\"assetUsageAgreementId\": \"acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106\","
                              + "\"agreement\": {"
                              + "\"uid\": \"c8606a77-c39d-4060-bd7d-1051f4c6d316\","
                              + "\"profile\": \"{}\" "
                              + "}"
                              + "}"
                              + "}",
                          "application/json")),
              service(NEXUS_SERVER)
                  .get("/license.json")
                  .anyBody()
                  .willReturn(
                      success(
                          "{\"rtuRequired\": true, \"companyName\": \"Company A\"}",
                          "application/json"))));

  @Before
  public void setUp() throws Exception {

    hoverflyRule.resetJournal();
  }

  @Test
  public void shouldRegisterAsset() {
    UUID solutionId = UUID.randomUUID();
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);
    mockArtifacts(mockCDSApi);
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    RegisterAssetRequest request = new RegisterAssetRequest();
    request.setSolutionId(solutionId);
    request.setRevisionId(SWIDTAGID);
    request.setLoggedIdUser("admin");
    try {
      CompletableFuture<RegisterAssetResponse> responseFuture = asset.register(request);
      RegisterAssetResponse response = responseFuture.get();
      assertEquals("softwareRegistered", response.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public NexusArtifactClient getNexusClient() {
    RepositoryLocation repositoryLocation = new RepositoryLocation();
    repositoryLocation.setUrl(NEXUS_SERVER);
    repositoryLocation.setId("1");
    repositoryLocation.setUsername("admin");
    repositoryLocation.setPassword("admin");
    // work around nexus client is not supporting jvm proxy host setup
    repositoryLocation.setProxy(
        "http://"
            + System.getProperty("http.proxyHost")
            + ":"
            + System.getProperty("http.proxyPort"));
    return new NexusArtifactClient(repositoryLocation);
  }

  @Test
  public void shouldRegisterAssetOverloaded() {
    UUID solutionId = UUID.randomUUID();

    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);
    mockArtifacts(mockCDSApi);
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    RegisterAssetRequest request = new RegisterAssetRequest();
    request.setSolutionId(solutionId);
    request.setRevisionId(SWIDTAGID);
    request.setLoggedIdUser("admin");
    try {
      CompletableFuture<RegisterAssetResponse> responseFuture = asset.register(request);
      RegisterAssetResponse response = responseFuture.get();
      assertEquals("softwareRegistered", response.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private void mockArtifacts(MockDatabaseClient mockCDSApi) {
    List<MLPArtifact> artifacts = new ArrayList<MLPArtifact>();
    MLPArtifact artifact = new MLPArtifact();
    artifact.setArtifactTypeCode("LI");
    artifact.setArtifactId(UUID.randomUUID().toString());
    artifact.setUri("/license.json");
    artifact.setName("license.json");
    artifacts.add(artifact);

    mockCDSApi.setSolutionRevisionArtifacts(artifacts);
  }

  @Test
  public void shouldRegisterAssetVariation1() {
    UUID solutionId = UUID.randomUUID();
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    RegisterAssetRequest request = new RegisterAssetRequest();
    request.setSolutionId(solutionId.toString());
    request.setRevisionId(UUID.fromString(SWIDTAGID));
    request.setLoggedIdUser("admin");
    mockArtifacts(mockCDSApi);

    try {
      CompletableFuture<RegisterAssetResponse> responseFuture = asset.register(request);
      RegisterAssetResponse response = responseFuture.get();
      assertEquals("softwareRegistered", response.getMessage());
      assertTrue(response.isSuccess());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void shouldFailWithException() {
    UUID solutionId = UUID.randomUUID();
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, NEXUS_SERVER);
    RegisterAssetRequest request = new RegisterAssetRequest();
    request.setSolutionId(solutionId);
    request.setRevisionId(SWIDTAGID2);
    request.setLoggedIdUser("admin");
    try {
      CompletableFuture<RegisterAssetResponse> responseFuture = asset.register(request);
      RegisterAssetResponse response = responseFuture.get();
      assertFalse(response.isSuccess());
      assertEquals(solutionId.toString(), response.getSolutionId());
      assertEquals(SWIDTAGID2, response.getRevisionId());

    } catch (InterruptedException e) {
      fail(e.getMessage());
    } catch (ExecutionException e) {
      // expect this exception
      fail(e.getMessage());
    }
  }

  @Test
  public void shouldGetswidtags() {
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    try {
      String userId = "admin";
      String action = "aggregate";
      CompletableFuture<GetEntitledSwidTagsResponse> responseFuture =
          asset.getEntitledSwidTagsByUser(userId, action);
      GetEntitledSwidTagsResponse response = responseFuture.get();
      assertNotNull(response.getSwidTagsWithAvailableEntitlement().get(0).getSwTagId());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void getAssetUsageAgreementRecord() {
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    try {
      String softwareLicensorId = "Company A";
      String assetUsageAgreementId =
          "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106";
      CompletableFuture<GetAssetUsageAgreementResponse> responseFuture =
          asset.getAssetUsageAgreement(softwareLicensorId, assetUsageAgreementId);
      GetAssetUsageAgreementResponse response = responseFuture.get();
      assertNotNull(response);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void putAssetUsageAgreementRecord() {
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    try {
      PutAssetUsageAgreementRequest request = new PutAssetUsageAgreementRequest();
      request.setUserId("admin");
      String softwareLicensorId = "Company A";
      String assetUsageAgreementId =
          "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106";
      CompletableFuture<PutAssetUsageAgreementResponse> responseFuture =
          asset.putAssetUsageAgreement(softwareLicensorId, assetUsageAgreementId, request);
      PutAssetUsageAgreementResponse response = responseFuture.get();
      assertNotNull(response);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void putAssetUsageAgreementRestriction() {
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    LicenseAsset asset = new LicenseAsset(mockCDSApi, LUM_SERVER, getNexusClient());
    try {
      PutAssetUsageAgreementRestrictionRequest request =
          new PutAssetUsageAgreementRestrictionRequest();
      request.setUserId("admin");
      String softwareLicensorId = "Company A";
      String assetUsageAgreementId =
          "acumos://software-licensor/Company A/agreement/3eb8c43a-bf19-46ab-8392-99c7efdf4106";
      CompletableFuture<PutAssetUsageAgreementResponse> responseFuture =
          asset.putAssetUsageAgreementRestriction(
              softwareLicensorId, assetUsageAgreementId, request);
      PutAssetUsageAgreementResponse response = responseFuture.get();
      assertNotNull(response);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
