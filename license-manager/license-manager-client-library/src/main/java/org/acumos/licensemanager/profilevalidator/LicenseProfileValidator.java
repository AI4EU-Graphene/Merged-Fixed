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

package org.acumos.licensemanager.profilevalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException;
import org.acumos.licensemanager.profilevalidator.model.ILicenseProfileValidator;
import org.acumos.licensemanager.profilevalidator.model.LicenseProfileValidationResults;
import org.acumos.licensemanager.profilevalidator.resource.LicenseJsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LicenseProfileValidator will verify the license.json to ensure there are no json errors or json
 * schema errors
 *
 * <p>Example json schema validation errors:
 *
 * <ul>
 *   <li>$.keyword: is missing but it is required
 *   <li>$.licenseName: is missing but it is required
 *   <li>$.copyright: is missing but it is required
 *   <li>$.keyword: integer found, string expected
 * </ul>
 *
 * <p>If content is not json then you will get an exception
 * org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException:
 * LicenseProfileJson: issue reading input
 */
public class LicenseProfileValidator implements ILicenseProfileValidator {

  /** Logger for any exception handling. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ObjectMapper objectMappper;
  private static String boreasSchemaUrl =
      "https://raw.githubusercontent.com/acumos/security-verification/boreas/license-manager-client-library/src/main/resources/license.schema.json";

  public LicenseProfileValidator() {}

  public LicenseProfileValidator(ObjectMapper mapper) {
    objectMappper = mapper;
  }

  private ObjectMapper getObjectMapper() {
    if (objectMappper == null) {
      objectMappper = new ObjectMapper();
    }
    return objectMappper;
  }

  @Override
  public LicenseProfileValidationResults validate(String jsonString)
      throws LicenseProfileException {

    LicenseProfileValidationResults results = null;
    try (InputStream targetStream = new ByteArrayInputStream(jsonString.getBytes()); ) {
      results = validate(targetStream);
    } catch (IOException e) {
      throw new LicenseProfileException("LicenseProfile: could not convert string to bytes", e);
    }

    return results;
  }

  @Override
  public final LicenseProfileValidationResults validate(InputStream inputStream)
      throws LicenseProfileException {
    // read in json schema
    JsonNode node;
    try {
      node = getObjectMapper().readTree(inputStream);
    } catch (Exception e) {
      throw new LicenseProfileException("LicenseProfile: issue reading input", e);
    }
    if (node == null) {
      throw new LicenseProfileException("LicenseProfile: could not load json");
    }
    return validate(node);
  }

  public LicenseProfileValidationResults validate(final JsonNode node)
      throws LicenseProfileException {

    if (node == null) {
      throw new LicenseProfileException("LicenseProfile: please provide json node input");
    }
    JsonSchema schema;
    try {
      schema = this.getLicenseJsonSchema(node);
    } catch (IOException e) {
      throw new LicenseProfileException("LicenseProfile: could not load schema", e);
    }
    Set<ValidationMessage> errors = schema.validate(node);
    LicenseProfileValidationResults results = new LicenseProfileValidationResults();
    results.setJsonSchemaErrors(errors);
    return results;
  }

  private JsonSchema getLicenseJsonSchema(final JsonNode node) throws IOException {
    // - $schema from the input data
    // - if $schema found then
    //   - use that as $schema URL
    // - else if input data as per boreas schema then
    //   - get/find schemaUrl as per boreas
    // - else
    //   - report an error

    // - find respective schema URL from the schemaVersion map
    JsonNode schemaNode = node.get("$schema");
    String schemaUrl = null;
    if (schemaNode != null) {
      schemaUrl = schemaNode.asText();
    } else if (node.get("modelLicenses") != null) {
      schemaUrl = boreasSchemaUrl;
    } else {
      LOGGER.error("The given document is missing $schema field.");
      throw new IOException("The given document is missing $schema field.");
    }

    return LicenseJsonSchema.getSchema(schemaUrl);
  }
}
