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

import java.util.UUID;

/** When making a request for a license RTU consolidating common functionality. */
public abstract class BaseLicenseRequest implements ICommonLicenseRequest {

  private UUID solutionId;
  private UUID revisionId;
  private UUID usageRequestId;
  private String loggedInUserName;

  public UUID getUsageRequestId() {
    return usageRequestId;
  }

  public String getLoggedInUserName() {
    return loggedInUserName;
  }

  public void setLoggedInUserName(String loggedInUserName) {
    this.loggedInUserName = loggedInUserName;
  }

  public void setUsageRequestId(UUID usageRequestId) {
    this.usageRequestId = usageRequestId;
  }

  public void setUsageRequestId(String usageRequestId) {
    this.usageRequestId = UUID.fromString(usageRequestId);
  }

  public void setRevisionId(String revisionId) {
    this.revisionId = UUID.fromString(revisionId);
  }

  public void setRevisionId(UUID revisionId) {
    this.revisionId = revisionId;
  }

  public UUID getRevisionId() {
    return revisionId;
  }

  public UUID getSolutionId() {
    return solutionId;
  }

  public void setSolutionId(String solutionId) {
    this.solutionId = UUID.fromString(solutionId);
  }

  public void setSolutionId(UUID solutionId) {
    this.solutionId = solutionId;
  }
}
