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

const fs = require('fs-extra');
const concat = require('concat');

(async function build() {
  const filesEs5 = [
    './dist/license-profile-editor/main-es5.js',
    './dist/license-profile-editor/polyfills-es5.js',
    './dist/license-profile-editor/runtime-es5.js',
    './dist/license-profile-editor/scripts.js',
  ];

  // cancat file order is important here
  const filesEs2015 = [
    './dist/license-profile-editor/runtime-es2015.js',
    './dist/license-profile-editor/polyfills-es2015.js',
    './dist/license-profile-editor/scripts.js',
    './dist/license-profile-editor/main-es2015.js',
  ];

  await concat(filesEs5, './dist/license-profile-editor/license-profile-editor-es5.js');
  await concat(filesEs2015, './dist/license-profile-editor/license-profile-editor-es2015.js');
})();
