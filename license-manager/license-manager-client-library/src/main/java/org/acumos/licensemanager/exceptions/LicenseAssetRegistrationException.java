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
import okhttp3.Response;
import org.acumos.licensemanager.client.model.RegisterAssetRequest;

/** When getting, updating, or creating a right to use this exception captures the issue. */
public class LicenseAssetRegistrationException extends RuntimeException implements Serializable {

  /** Internal exception being wrapped by RTU exception. */
  private final Throwable exception;

  private String solutionId;

  private String revisionId;

  private Response response;

  /**
   * Creates exception for any RTU operation error.
   *
   * @param message provide text for message
   * @param exception rest client response error
   */
  public LicenseAssetRegistrationException(final String message, final Throwable exception) {
    super(message);
    this.exception = exception;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public LicenseAssetRegistrationException(
      final String message, final RegisterAssetRequest request) {
    super(message);
    this.exception = null;
    setSolutionId(request.getSolutionId().toString());
    setRevisionId(request.getRevisionId().toString());
  }

  public LicenseAssetRegistrationException(
      final String message, final RegisterAssetRequest request, final Throwable exception) {
    super(message);
    this.exception = exception;
    setSolutionId(request.getSolutionId().toString());
    setRevisionId(request.getRevisionId().toString());
  }

  /**
   * Creates exception for RTU creation errors
   *
   * @param message provide text for message
   * @param solutionId solution id that has an issue (persistentId in LUM)
   * @param revisionId revision id that has the issue (swidTag in LUM)
   */
  public LicenseAssetRegistrationException(
      final String message, final String solutionId, final String revisionId) {
    super(message);
    exception = null;
    this.setSolutionId(solutionId);
    this.setRevisionId(revisionId);
  }

  public LicenseAssetRegistrationException(
      String message, RegisterAssetRequest request, Response response) {
    super(message);
    exception = null;
    this.setSolutionId(solutionId);
    this.setRevisionId(revisionId);
    this.setResponse(response);
  }

  public String getSolutionId() {
    return solutionId;
  }

  /** @return the revisionId */
  public String getRevisionId() {
    return revisionId;
  }

  public LicenseAssetRegistrationException setSolutionId(String solutionId) {
    this.solutionId = solutionId;
    return this;
  }

  public LicenseAssetRegistrationException setRevisionId(String revisionId) {
    this.revisionId = revisionId;
    return this;
  }

  /**
   * *
   *
   * @return the Exception
   */
  public final Throwable getInternalException() {
    return exception;
  }

  private static final long serialVersionUID = -6924276046364606483L;
}
