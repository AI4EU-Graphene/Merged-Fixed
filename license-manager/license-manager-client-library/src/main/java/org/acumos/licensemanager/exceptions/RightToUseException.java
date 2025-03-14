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

package org.acumos.licensemanager.exceptions;

import java.io.Serializable;
import java.util.UUID;
import org.springframework.web.client.RestClientResponseException;

/** When getting, updating, or creating a right to use this exception captures the issue. */
public class RightToUseException extends Exception implements Serializable {

  private UUID solutionId;
  private UUID revisionId;

  /**
   * Creates exception for any RTU operation error.
   *
   * @param message provide text for message
   * @param restException rest client response error
   */
  public RightToUseException(
      final String message, final RestClientResponseException restException) {
    super(message);
  }

  public UUID getSolutionId() {
    return solutionId;
  }

  public RightToUseException setSolutionId(UUID solutionId) {
    this.solutionId = solutionId;
    return this;
  }

  public UUID getRevisionId() {
    return revisionId;
  }

  public RightToUseException setRevisionId(UUID revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  /**
   * /** Creates exception for RTU creation errors
   *
   * @param message about the rtu exception
   * @param solutionId for the model
   * @param revisionId version of the model
   */
  public RightToUseException(final String message, final UUID solutionId, final UUID revisionId) {
    super(message);
    this.setSolutionId(solutionId);
    this.setRevisionId(revisionId);
  }

  private static final long serialVersionUID = 3714073159231864295L;
}
