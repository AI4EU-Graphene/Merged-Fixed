/*-
 * ===============LICENSE_START================================================
 * Acumos Apache-2.0
 * ============================================================================
 * Copyright (C) 2019 Nordix Foundation.
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
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.acumos.cds.client.CommonDataServiceRestClientMockImpl;
import org.acumos.cds.domain.MLPCatalog;
import org.acumos.licensemanager.client.model.LicenseAction;
import org.acumos.licensemanager.client.model.LicenseRtuVerification;
import org.acumos.licensemanager.client.model.VerifyLicenseRequest;
import org.acumos.licensemanager.client.rtu.LicenseRtuVerifier;
import org.acumos.licensemanager.exceptions.RightToUseException;
import org.acumos.lum.model.AssetUsageDenialAssetUsageDenial;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * License Manager Client Unit tests - Test creation of the RTU and RTU references - Test reading
 * and verifying license/right to use exists.
 */
public class LicenseRtuVerifierTest {

  private static final String LUM_SERVER = "http://my-lum-server.com";
  private static final String SOLUTION_ID = UUID.randomUUID().toString();
  private static final String REVISION_ID = UUID.randomUUID().toString();
  private static final String ASSET_USAGE_ID = UUID.randomUUID().toString();
  private static final String NO_AGREEMENT_DENIAL_ASSET_USAGE_ID = UUID.randomUUID().toString();

  private class MockDatabaseClient extends CommonDataServiceRestClientMockImpl {}

  @ClassRule
  public static HoverflyRule hoverflyRule =
      HoverflyRule.inSimulationMode(
          dsl(
              service(LUM_SERVER)
                  .put("/api/v1/asset-usage")
                  .queryParam("assetUsageId", NO_AGREEMENT_DENIAL_ASSET_USAGE_ID)
                  .anyBody()
                  .willReturn(
                      response()
                          .status(402)
                          .body(
                              "{"
                                  + "\"userId\": \"consumer1\","
                                  + "\"swMgtSystemId\": \"Acumos\","
                                  + "\"requestId\": \"ce808319-fad0-4d6e-a9cd-92fda255ae6c\","
                                  + "\"requested\": \"2019-10-09T18:49:52.732Z\","
                                  + "\"usageEntitled\": false,"
                                  + "\"assetUsage\": {"
                                  + "\"swTagId\": \"8d3f8eb6-97bf-4e47-a236-79cc05cb87d8\","
                                  + "\"assetUsageId\": \"38935f5c-293b-4e00-91ad-14d5de66eee3\","
                                  + "\"action\": \"acumos:download\","
                                  + "\"isIncludedAsset\": false,"
                                  + "\"usageEntitled\": false,"
                                  + "\"isUsedBySwCreator\": false,"
                                  + "\"assetUsageSeq\": 1,"
                                  + "\"softwareLicensorId\": \"Company B\","
                                  + "\"licenseProfileId\": \"294fd35e-b6c3-4987-8caf-c1686597a332\","
                                  + "\"licenseProfileRevision\": 1,"
                                  + "\"isRtuRequired\": true,"
                                  + "\"swidTagRevision\": 1,"
                                  + "\"assetUsageDenial\": ["
                                  + "{"
                                  + "\"denialType\": \"agreementNotFound\","
                                  + "\"denialReason\": \"asset-usage-agreement not found for swTagId(8d3f8eb6-97bf-4e47-a236-79cc05cb87d8)\""
                                  + "}"
                                  + "]"
                                  + "}"
                                  + "}")
                          .header("Content-Type", "application/json"))
                  .put("/api/v1/asset-usage")
                  .queryParam("assetUsageId", ASSET_USAGE_ID)
                  .anyBody()
                  .willReturn(
                      response()
                          .status(200)
                          .body(
                              "{\"userId\":\"modelowner1\",\"swMgtSystemId\":\"Acumos\", "
                                  + "\"requestId\":\"02aad28e-7835-4095-982e-bd62c36692c9\",\"requested\":\"2019-10-09T18:39:06.327Z\","
                                  + "\"usageEntitled\":true,\"assetUsage\":{\"swTagId\":\"e55a613a-e3e7-4c15-9770-a85a9288e8f7\","
                                  + "\"assetUsageId\":\"f9cc4c86-78fc-430e-98be-83da38d04e5f\",\"action\":\"acumos:download\","
                                  + "\"isIncludedAsset\":false,\"usageEntitled\":true,\"isUsedBySwCreator\":true,\"assetUsageSeq\":1,"
                                  + "\"softwareLicensorId\":\"Company B\",\"licenseProfileId\":"
                                  + "\"af8bc957-1280-4739-a01c-66b656ca76aa\",\"licenseProfileRevision\":2,"
                                  + "\"isRtuRequired\":true,\"swidTagRevision\":2}}")
                          .header("Content-Type", "application/json"))));

  @Test
  public void licenseVerifyDownloadUsePrepublish()
      throws InterruptedException, ExecutionException, RightToUseException {

    boolean rtuFlag;
    LicenseAction licenseAction = LicenseAction.DOWNLOAD;
    String loggedInUserId = "test";

    // not published solution
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    mockCDSApi.setSolutionCatalogs(cats);

    VerifyLicenseRequest licenseDownloadRequest =
        new VerifyLicenseRequest(
            licenseAction, SOLUTION_ID, REVISION_ID, loggedInUserId, ASSET_USAGE_ID);
    licenseDownloadRequest.setAction(licenseAction);
    LicenseRtuVerifier licenseVerifier = new LicenseRtuVerifier(mockCDSApi, LUM_SERVER);
    CompletableFuture<LicenseRtuVerification> verifyUserRTU =
        licenseVerifier.verifyRtu(licenseDownloadRequest);

    LicenseRtuVerification verification = verifyUserRTU.get();
    // returns true or false if rtu exists
    rtuFlag = verification.isAllowed();

    assertTrue("download is allowed", rtuFlag);
    assertFalse("isPublished", verification.isPublished());
  }

  @Test
  public void licenseVerifyDownloadUse()
      throws InterruptedException, ExecutionException, RightToUseException {

    boolean rtuFlag;
    LicenseAction licenseAction = LicenseAction.DOWNLOAD;
    String loggedInUserId = "test";

    // published solution
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);

    VerifyLicenseRequest licenseDownloadRequest =
        new VerifyLicenseRequest(
            licenseAction, SOLUTION_ID, REVISION_ID, loggedInUserId, ASSET_USAGE_ID);
    licenseDownloadRequest.setAction(licenseAction);
    LicenseRtuVerifier licenseVerifier = new LicenseRtuVerifier(mockCDSApi, LUM_SERVER);
    CompletableFuture<LicenseRtuVerification> verifyUserRTU =
        licenseVerifier.verifyRtu(licenseDownloadRequest);

    LicenseRtuVerification verification = verifyUserRTU.get();
    // returns true or false if rtu exists
    rtuFlag = verification.isAllowed();
    assertTrue("download is allowed", rtuFlag);
    assertTrue("isPublished", verification.isPublished());
  }

  @Test
  public void licenseVerifyDownloadDeniedUse()
      throws InterruptedException, ExecutionException, RightToUseException {
    boolean rtuFlag;
    LicenseAction licenseAction = LicenseAction.DOWNLOAD;
    String loggedInUserId = "test";
    // published solution
    MockDatabaseClient mockCDSApi = new MockDatabaseClient();
    List<MLPCatalog> cats = new ArrayList<MLPCatalog>();
    cats.add(new MLPCatalog("PB", true, "Catalog", "url", "test"));
    mockCDSApi.setSolutionCatalogs(cats);
    VerifyLicenseRequest licenseDownloadRequest =
        new VerifyLicenseRequest(
            licenseAction,
            SOLUTION_ID,
            REVISION_ID,
            loggedInUserId,
            NO_AGREEMENT_DENIAL_ASSET_USAGE_ID);
    licenseDownloadRequest.setAction(licenseAction);
    LicenseRtuVerifier licenseVerifier = new LicenseRtuVerifier(mockCDSApi, LUM_SERVER);
    CompletableFuture<LicenseRtuVerification> verifyUserRTU =
        licenseVerifier.verifyRtu(licenseDownloadRequest);

    LicenseRtuVerification verification = verifyUserRTU.get();
    // returns true or false if rtu exists
    rtuFlag = verification.isAllowed();
    String rtuDeniedReason = verification.getApiException().getMessage();
    assertFalse("download is not allowed", rtuFlag);
    assertEquals("Payment Required", rtuDeniedReason);
    assertNotNull(verification.getLumDenialResponse());
    assertEquals(
        "asset-usage-agreement not found for swTagId(8d3f8eb6-97bf-4e47-a236-79cc05cb87d8)",
        verification.getDenialReason().getDetails());
    List<AssetUsageDenialAssetUsageDenial> denial =
        verification.getLumDenialResponse().getAssetUsage().getAssetUsageDenial();
    assertNotNull(denial);
    assertEquals(1, denial.size());
    assertEquals(
        AssetUsageDenialAssetUsageDenial.DenialTypeEnum.AGREEMENTNOTFOUND,
        denial.get(0).getDenialType());
    assertEquals(
        "asset-usage-agreement not found for swTagId(8d3f8eb6-97bf-4e47-a236-79cc05cb87d8)",
        denial.get(0).getDenialReason());
    assertTrue("isPublished", verification.isPublished());
  }
}
