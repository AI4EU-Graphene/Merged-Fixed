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

package org.acumos.licensemanager.profilevalidator.exceptions;

import java.io.Serializable;
import org.springframework.web.client.RestClientResponseException;

/** Exception to abort license profile processing and report failure. */
public class LicenseProfileException extends Exception implements Serializable {

  private static final long serialVersionUID = -7896327154019469541L;

  /**
   * Creates exception for any License Json procesing error.
   *
   * @param message provide text for message
   */
  public LicenseProfileException(final String message) {
    super(message);
  }

  /**
   * Creates exception for any License Json procesing error.
   *
   * @param message provide text for message
   * @param exception rest client response error
   */
  public LicenseProfileException(final String message, final Exception exception) {
    super(message, exception);
  }

  /**
   * Creates exception for any License Json procesing error.
   *
   * @param message provide text for message
   * @param exception rest client response error
   */
  public LicenseProfileException(
      final String message, final RestClientResponseException exception) {
    super(message, exception);
  }
}
