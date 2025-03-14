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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END==================================================
 */

package org.acumos.licensemanager.client.model;

/**
 * Request object to verify a user's right to use for a specific action passed to the {@link
 * org.acumos.licensemanager.client.model.ILicenseRtuVerifier}.
 */
public class VerifyLicenseRequest extends BaseLicenseRequest implements IVerifyLicenseRequest {

  /** License Actions to be verified. */
  private LicenseAction licenseAction;

  /** Constructor for VerifyLicenseRequest. */
  public VerifyLicenseRequest() {}

  /**
   * @param action requested action
   * @param solutionId cds solution id for a ai model in acumos
   * @param revisionId cds revision id for a solution
   * @param userId logged in user id
   * @param usageRequestId software instance such as download id, deployment id, artifact id
   */
  public VerifyLicenseRequest(
      final LicenseAction action,
      final String solutionId,
      final String revisionId,
      final String userId,
      final String usageRequestId) {
    this.licenseAction = action;
    setSolutionId(solutionId);
    setRevisionId(revisionId);
    setUsageRequestId(usageRequestId);
    setLoggedInUserName(userId);
  }

  /**
   * Set the actions to be verified.
   *
   * @param action a {@link java.util.List} object.
   */
  public final void setAction(LicenseAction action) {
    this.licenseAction = action;
  }

  @Override
  public final LicenseAction getAction() {
    return licenseAction;
  }
}
