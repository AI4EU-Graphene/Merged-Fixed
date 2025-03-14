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
 **/

package org.acumos.licensemanager.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPLicenseProfileTemplate;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.licensemanager.profilevalidator.LicenseProfileValidator;
import org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException;
import org.acumos.licensemanager.profilevalidator.model.ILicenseProfileValidator;
import org.acumos.licensemanager.profilevalidator.model.LicenseProfileValidationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

/**
 * LicenseProfile api provides - list of available license profile templates and a specific template
 * - validation of given license profile
 *
 * <p>This API requires CDS 3.0.0 and above.
 */
public class LicenseProfile implements ILicenseProfileValidator {

  /** Logger for any exceptions that happen while creating a RTU with CDS. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  /** dataClient must be provided by consumer of this library. */
  private final ICommonDataServiceRestClient dataClient;

  private final LicenseProfileValidator LicenseProfileValidator;

  /**
   * The implementation of the CDS is required to enable this library.
   *
   * @param dataServiceClient a {@link org.acumos.cds.client.ICommonDataServiceRestClient} object.
   */
  public LicenseProfile(final ICommonDataServiceRestClient dataServiceClient) {
    this.dataClient = dataServiceClient;
    this.LicenseProfileValidator = new LicenseProfileValidator();
  }

  /**
   * Fetches and returns list of available license profile templates.
   *
   * <p>See {@link org.acumos.cds.client.ICommonDataServiceRestClient#getLicenseProfileTemplates}
   *
   * @return list of license profile templates, which may be empty
   * @throws LicenseProfileException if CDS fails to retrieve license profile templates
   */
  public final List<MLPLicenseProfileTemplate> getTemplates() throws LicenseProfileException {

    List<MLPLicenseProfileTemplate> templates;
    try {
      // sort by priority
      // show all records
      Map<String, String> fieldToDirectionMap = new HashMap<String, String>();
      fieldToDirectionMap.put("priority", "DESC");
      RestPageRequest req = new RestPageRequest(0, 0, fieldToDirectionMap);
      RestPageResponse<MLPLicenseProfileTemplate> templatesFromCDS =
          dataClient.getLicenseProfileTemplates(req);
      templates = templatesFromCDS.getContent();
    } catch (RestClientResponseException ex) {
      LOGGER.error("getTemplates failed, server reports: {}", ex.getResponseBodyAsString());
      throw new LicenseProfileException("getTemplates failed", ex);
    }
    return templates;
  }

  /**
   * Gets the license profile template contents based on the given template Id
   *
   * <p>See {@link org.acumos.cds.client.ICommonDataServiceRestClient#getLicenseProfileTemplate}
   *
   * @param templateId template ID to retrieve specific license profile template
   * @return the license profile template for the given template Id
   * @throws LicenseProfileException if CDS fails to retrieve license profile template
   */
  public final MLPLicenseProfileTemplate getTemplate(long templateId)
      throws LicenseProfileException {

    MLPLicenseProfileTemplate licenseTemplate;
    try {
      licenseTemplate = dataClient.getLicenseProfileTemplate(templateId);
    } catch (RestClientResponseException ex) {
      LOGGER.error("getTemplate failed, server reports: {}", ex.getResponseBodyAsString());
      throw new LicenseProfileException("getTemplate failed", ex);
    }
    return licenseTemplate;
  }

  @Override
  public LicenseProfileValidationResults validate(String jsonString)
      throws LicenseProfileException {
    return LicenseProfileValidator.validate(jsonString);
  }

  @Override
  public LicenseProfileValidationResults validate(JsonNode node) throws LicenseProfileException {
    return LicenseProfileValidator.validate(node);
  }

  @Override
  public LicenseProfileValidationResults validate(InputStream input)
      throws LicenseProfileException {
    return LicenseProfileValidator.validate(input);
  }
}
