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

package org.acumos.licensemanager.profilevalidator.resource;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** LicenseJsonSchema class. */
public final class LicenseJsonSchema {

  private static Map<String, JsonSchema> schemaUrlToJsonSchemaMap =
      new HashMap<String, JsonSchema>();

  /** JsonSchema object for validation of license schema. */
  private static JsonSchema jsonSchema;

  /** Do not instantiate. */
  private LicenseJsonSchema() {}

  /**
   * Get the license json schema (as JsonSchema) based on given input url.
   *
   * @param schemaUrlPath schema URL to fetch the schema
   * @return a {@link com.networknt.schema.JsonSchema} object, null if not found.
   * @throws java.io.IOException if any.
   */
  public static JsonSchema getSchema(String schemaUrlPath) throws IOException {

    if (LicenseJsonSchema.schemaUrlToJsonSchemaMap.containsKey(schemaUrlPath)) {
      return LicenseJsonSchema.schemaUrlToJsonSchemaMap.get(schemaUrlPath);
    }

    JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
    URL schemaUrl = new URL(schemaUrlPath);
    InputStream is = schemaUrl.openStream();
    jsonSchema = factory.getSchema(is);
    return jsonSchema;
  }
}
