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

package org.acumos.licensemanager.client.main;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPLicenseProfileTemplate;
import org.acumos.licensemanager.client.LicenseProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * License Profile Main program to fetch available license templates.
 *
 * <p>Envirionment variables required to point to CCDS api - ACUMOS_CDS_HOST - ACUMOS_CDS_PORT -
 * ACUMOS_CDS_USER - ACUMOS_CDS_PASSWORD
 */
public class LicenseProfileMain {

  /** Logger for any exception handling. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** Common data service host name. Set as an environment variable ACUMOS_CDS_HOST. */
  private static final String HOSTNAME = System.getenv("ACUMOS_CDS_HOST");

  /** Common data context path. */
  private static final String CONTEXT_PATH = "/ccds";
  /**
   * Common data service port -- may require NodePort setup if using K8. Set as an environment
   * variable ACUMOS_CDS_PORT.
   */
  private static final int PORT = Integer.parseInt(System.getenv("ACUMOS_CDS_PORT"));

  /** Common data service user name. Set as an environment variable ACUMOS_CDS_USER. */
  private static final String USER_NAME = System.getenv("ACUMOS_CDS_USER");

  /** Common data service password. Set as an environment variable ACUMOS_CDS_PASSWORD. */
  private static final String PASSWORD = System.getenv("ACUMOS_CDS_PASSWORD");

  /** No not allow for utility class from being instantiated. */
  protected LicenseProfileMain() {
    // prevents calls from subclass
    throw new UnsupportedOperationException();
  }

  /**
   * Main program can be used with the following arguments requires position order.
   * LicenseProfileMain solutionId [userId] [siteWide]
   *
   * @param args an array of {@link java.lang.String} objects.
   * @throws java.lang.Exception if any.
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("Port:" + System.getenv("ACUMOS_CDS_PORT"));
    URL url = new URL("http", HOSTNAME, PORT, CONTEXT_PATH);
    LOGGER.info("CDS Client: URL is {}", url);
    ICommonDataServiceRestClient dataServiceClient =
        CommonDataServiceRestClientImpl.getInstance(url.toString(), USER_NAME, PASSWORD);

    if (args != null && args.length > 0) {
      System.out.println("cmd:" + args[0]);
      if (args[0].equals("createLicProfileTpl")) {
        LicenseProfileMain.createLicenceProfileTpl(dataServiceClient);
        return;
      }
    }

    try {
      LicenseProfile licProfile = new LicenseProfile(dataServiceClient);
      List<MLPLicenseProfileTemplate> templates = licProfile.getTemplates();

      Iterator<MLPLicenseProfileTemplate> licProIte = templates.iterator();
      Long licProTplId = null;
      while (licProIte.hasNext()) {
        MLPLicenseProfileTemplate licProTpl = licProIte.next();
        licProTplId = licProTpl.getTemplateId();
        LicenseProfileMain.printLicProTpl(licProTpl);
      }

      if (licProTplId != null) {
        System.out.println("\nFetching License Profile with ID: " + licProTplId);
        MLPLicenseProfileTemplate licProTpl = licProfile.getTemplate(licProTplId);
        LicenseProfileMain.printLicProTpl(licProTpl);
      } else {
        System.out.println("No License Profile found.");
      }

    } catch (HttpStatusCodeException ex) {
      LOGGER.error("basicSequenceDemo failed, server reports: {}", ex.getResponseBodyAsString());
      throw ex;
    }
  }

  public static void printLicProTpl(MLPLicenseProfileTemplate licProTpl) {
    Long licProTplId = licProTpl.getTemplateId();
    System.out.println("\n==== License Profile (ID: " + licProTplId + ")====");
    System.out.println("Name: " + licProTpl.getTemplateName());
    System.out.println("LicProfileText: " + licProTpl.getTemplate());
    System.out.println("Priority: " + licProTpl.getPriority());
  }

  public static void createLicenceProfileTpl(ICommonDataServiceRestClient dataServiceClient) {
    MLPLicenseProfileTemplate licProTpl = new MLPLicenseProfileTemplate();
    licProTpl.setTemplateName("License B");
    licProTpl.setTemplate(
        "{ \"keyword\": \"Commercial License\", \"licenseName\": \"Company B\", \"intro\": \"This Company B software file is distributed by Company B under the Commercial License, Version 1.0 (the \\\"License\\\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\", \"copyright\": { \"year\": 2019, \"company\": \"Company B\", \"suffix\": \"A Intellectual Property. All rights reserved.\" }, \"softwareType\": \"Acumos Ai/ML Model\", \"companyName\": \"Company B\", \"contact\": { \"name\": \"Company B HelpDesk\", \"URL\": \"https://companyB.example.org\", \"email\": \"contact@companyB.example.org\" }, \"additionalInfo\": \"This Company B software file\" }");
    licProTpl.setPriority(2);
    licProTpl.setUserId("12345678-abcd-90ab-cdef-1234567890ab");
    licProTpl.setCreated(Instant.now());
    licProTpl.setModified(Instant.now());

    dataServiceClient.createLicenseProfileTemplate(licProTpl);
  }
}
