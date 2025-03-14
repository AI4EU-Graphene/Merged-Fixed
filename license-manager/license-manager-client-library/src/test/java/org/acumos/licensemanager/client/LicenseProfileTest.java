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

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.acumos.cds.client.CommonDataServiceRestClientMockImpl;
import org.acumos.cds.domain.MLPLicenseProfileTemplate;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException;
import org.junit.Test;

/**
 * License Manager Client > License Profile Unit tests - Test reading and verifying license profile
 * templates.
 */
public class LicenseProfileTest {

  private class MockDatabaseClient extends CommonDataServiceRestClientMockImpl {

    Map<Long, MLPLicenseProfileTemplate> licProfTempMap =
        new HashMap<Long, MLPLicenseProfileTemplate>();

    public MockDatabaseClient(String webapiString, String user, String pass) {
      super(webapiString, user, pass);

      // init license templates

      // template 1
      for (int tempId = 101; tempId <= 105; tempId++) {
        MLPLicenseProfileTemplate template = new MLPLicenseProfileTemplate();
        template.setTemplateId(Long.valueOf(tempId));
        template.setTemplateName("License " + tempId);
        template.setTemplate("This is description for License " + tempId);
        template.setPriority(1);
        template.setUserId("admin");
        template.setCreated(Instant.now());
        template.setModified(Instant.now());

        licProfTempMap.put(template.getTemplateId(), template);
      }
    }

    @Override
    public RestPageResponse<MLPLicenseProfileTemplate> getLicenseProfileTemplates(
        RestPageRequest pageRequest) {
      List<MLPLicenseProfileTemplate> templates =
          new ArrayList<MLPLicenseProfileTemplate>(licProfTempMap.values());
      RestPageResponse<MLPLicenseProfileTemplate> response =
          new RestPageResponse<MLPLicenseProfileTemplate>(templates);
      return response;
    }

    @Override
    public MLPLicenseProfileTemplate getLicenseProfileTemplate(long templateId) {
      return licProfTempMap.get(templateId);
    }
  }

  @Test
  public void testGetTemplates() throws LicenseProfileException {

    CommonDataServiceRestClientMockImpl dbClient = new MockDatabaseClient("url", "user", "pass");

    // get templates
    LicenseProfile licenseProfile = new LicenseProfile(dbClient);
    List<MLPLicenseProfileTemplate> templates = licenseProfile.getTemplates();

    assertEquals("Expected to receive list of templates", true, templates != null);
    assertEquals("Expected to receive 5 templates", 5, templates.size());

    MLPLicenseProfileTemplate licTemplate = templates.get(0);
    assertEquals(
        "Expected to receive first template with ID 101",
        Long.valueOf(101),
        licTemplate.getTemplateId());
    assertEquals(
        "Expected to receive first template with name 'License 101'",
        "License 101",
        licTemplate.getTemplateName());
  }

  @Test
  public void testGetTemplate() throws LicenseProfileException {

    CommonDataServiceRestClientMockImpl dbClient = new MockDatabaseClient("url", "user", "pass");

    // get templates
    LicenseProfile licenseProfile = new LicenseProfile(dbClient);
    MLPLicenseProfileTemplate licTemplate = licenseProfile.getTemplate(101);

    assertEquals("Expected to receive template with ID 101", true, licTemplate != null);
    assertEquals(
        "Expected to receive template with ID 101", Long.valueOf(101), licTemplate.getTemplateId());
    assertEquals(
        "Expected to receive template with name 'License 101'",
        "License 101",
        licTemplate.getTemplateName());
  }
}
