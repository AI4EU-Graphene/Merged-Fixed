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

package org.acumos.licensemanager.profilevalidator.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException;

/**
 * License Profile JSON validator Input json in different formats string and will return a report if
 * the json is validated 1. valid json syntax 2. valid json against schema
 */
public interface ILicenseProfileValidator {

  /**
   * Validates license profile json string against license profile schema.
   *
   * @param jsonString a {@link java.lang.String} object.
   * @return ILicenseJsonVerification results of parsing jsonstring and any errors against json
   *     schema
   * @throws LicenseProfileException if any.
   */
  LicenseProfileValidationResults validate(String jsonString) throws LicenseProfileException;

  /**
   * Validates license profile json node against license profile schema.
   *
   * @param node a {@link com.fasterxml.jackson.databind.JsonNode} object.
   * @return a {@link
   *     org.acumos.licensemanager.profilevalidator.model.LicenseProfileValidationResults} object.
   * @throws LicenseProfileException if any.
   */
  LicenseProfileValidationResults validate(JsonNode node) throws LicenseProfileException;

  /**
   * Validates license profile json node against license schema using input stream.
   *
   * @param input a {@link java.io.InputStream} object.
   * @return a {@link
   *     org.acumos.licensemanager.profilevalidator.model.LicenseProfileValidationResults} object.
   * @throws LicenseProfileException if any.
   */
  LicenseProfileValidationResults validate(InputStream input) throws LicenseProfileException;
}
