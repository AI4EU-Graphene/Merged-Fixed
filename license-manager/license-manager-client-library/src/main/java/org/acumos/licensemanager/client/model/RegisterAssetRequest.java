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

package org.acumos.licensemanager.client.model;

import java.util.UUID;

/** solutionId maps to @see {@link org.acumos.lum.model.PutSwidTagRequest} */
public class RegisterAssetRequest {

  private UUID solutionId;
  private UUID revisionId;
  private String loggedIdUser;

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

  public String getLoggedIdUser() {
    return loggedIdUser;
  }

  public void setLoggedIdUser(String loggedIdUser) {
    this.loggedIdUser = loggedIdUser;
  }

  public void setRevisionId(String revisionId) {
    this.revisionId = UUID.fromString(revisionId);
  }

  public void setRevisionId(UUID revisionId) {
    this.revisionId = revisionId;
  }
}
