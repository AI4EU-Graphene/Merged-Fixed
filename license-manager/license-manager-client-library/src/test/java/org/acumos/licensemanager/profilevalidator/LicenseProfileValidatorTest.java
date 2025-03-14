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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import org.acumos.licensemanager.profilevalidator.exceptions.LicenseProfileException;
import org.acumos.licensemanager.profilevalidator.model.LicenseProfileValidationResults;
import org.junit.Test;
import org.mockito.Mockito;

/** License Profile Json Validator Test. */
public class LicenseProfileValidatorTest {

  private void updateSchemaLocalPath(JsonNode jsonNode) {
    JsonNode schemaNode = jsonNode.get("$schema");
    String jsonSchemaPath = schemaNode.asText();
    URL newPath = LicenseProfileValidator.class.getResource(jsonSchemaPath);
    ((ObjectNode) jsonNode).put("$schema", newPath.toString());
  }

  @Test
  public void validLicenseJson() throws Exception {
    JsonNode goodJson = getJsonNodeFromClasspath("/good-license.json");
    this.updateSchemaLocalPath(goodJson);
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(goodJson);
    assertEquals(true, results.getJsonSchemaErrors().isEmpty());
  }

  @Test
  public void validBoreasLicenseJson() throws Exception {
    JsonNode goodJson = getJsonNodeFromClasspath("/boreas-good-license.json");
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(goodJson);
    assertEquals(true, results.getJsonSchemaErrors().isEmpty());
  }

  @Test
  public void validateErrorHandling() throws Exception {
    ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapper.readTree(Mockito.any(JsonParser.class))).thenThrow(IOException.class);

    LicenseProfileValidator validator = new LicenseProfileValidator(objectMapper);

    try {
      validator.validate("{}");
    } catch (LicenseProfileException e) {
      assertEquals("LicenseProfile: could not load json", e.getMessage());
    }
  }

  @Test
  public void minLicenseJson() throws Exception {
    JsonNode minLicense = getJsonNodeFromClasspath("/min-license.json");
    this.updateSchemaLocalPath(minLicense);
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(minLicense);
    System.out.println(results.getJsonSchemaErrors());
    assertEquals(0, results.getJsonSchemaErrors().size());
  }

  @Test
  public void minMissingLicenseJson() throws Exception {
    // tests minimum/required field missing (as per schema)
    JsonNode minMissingLicense = getJsonNodeFromClasspath("/min-missing-license.json");
    this.updateSchemaLocalPath(minMissingLicense);
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(minMissingLicense);
    System.out.println(results.getJsonSchemaErrors());
    assertEquals(false, results.getJsonSchemaErrors().isEmpty());
  }

  @Test
  public void partialLicenseJson() throws Exception {
    JsonNode partialLicense = getJsonNodeFromClasspath("/partial-license.json");
    this.updateSchemaLocalPath(partialLicense);
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(partialLicense);
    System.out.println(results.getJsonSchemaErrors());
    assertEquals(0, results.getJsonSchemaErrors().size());
  }

  @Test
  public void badInput() throws Exception {
    LicenseProfileValidator validator = new LicenseProfileValidator();
    InputStream in = Mockito.mock(InputStream.class);
    Mockito.when(in.read()).thenThrow(new IOException());
    InputStream inputStreamSpy = Mockito.spy(in);
    try {
      validator.validate(inputStreamSpy);
      fail("should have thrown error");
    } catch (LicenseProfileException e) {
      assertNotNull(e);
    }
    Mockito.verify(inputStreamSpy).close();
  }

  @Test
  public void partialLicenseJsonAsString() throws Exception {
    String partialLicense = convertStreamToString("/partial-license.json");

    // update schema path relative to local classpath
    ObjectMapper mapper = new ObjectMapper();
    JsonNode partialLicenseNode = mapper.readTree(partialLicense);
    this.updateSchemaLocalPath(partialLicenseNode);
    partialLicense = partialLicenseNode.toString();

    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(partialLicense);
    System.out.println(results.getJsonSchemaErrors());
    assertEquals(0, results.getJsonSchemaErrors().size());
  }

  @Test
  public void invalidLicenseJson() throws Exception {
    JsonNode badJson = getJsonNodeFromClasspath("/bad-license.json");
    this.updateSchemaLocalPath(badJson);
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(badJson);
    assertEquals(false, results.getJsonSchemaErrors().isEmpty());

    JsonNode badJson2 = getJsonNodeFromClasspath("/invalid-types-license.json");
    this.updateSchemaLocalPath(badJson2);
    results = validator.validate(badJson2);
    assertEquals(false, results.getJsonSchemaErrors().isEmpty());
  }

  @Test
  public void invalidBoreasLicenseJson() throws Exception {
    JsonNode badJson = getJsonNodeFromClasspath("/boreas-bad-license.json");
    LicenseProfileValidator validator = new LicenseProfileValidator();
    LicenseProfileValidationResults results = validator.validate(badJson);
    assertEquals(false, results.getJsonSchemaErrors().isEmpty());

    JsonNode badJson2 = getJsonNodeFromClasspath("/boreas-invalid-types-license.json");
    results = validator.validate(badJson2);
    assertEquals(false, results.getJsonSchemaErrors().isEmpty());
  }

  private JsonNode getJsonNodeFromClasspath(String name) throws Exception {
    InputStream is1 = LicenseProfileValidatorTest.class.getResourceAsStream(name);
    InputStream inputStreamSpy = Mockito.spy(is1);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(inputStreamSpy);
    Mockito.verify(inputStreamSpy).close();
    return node;
  }

  private String convertStreamToString(String name) throws IOException {
    InputStream is1 = LicenseProfileValidatorTest.class.getResourceAsStream(name);
    try {
      Scanner s = new Scanner(is1);
      s.useDelimiter("\\A");
      String content = s.hasNext() ? s.next() : "";
      is1.close();
      s.close();
      return content;
    } finally {
      if (is1 != null) {
        is1.close();
      }
    }
  }
}
