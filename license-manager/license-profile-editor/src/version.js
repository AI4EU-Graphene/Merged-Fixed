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

const { getLastCommit } = require('git-last-commit');
const { version } = require('../package.json');
const { resolve, relative } = require('path');
const fs = require('fs');
const { writeFileSync } = require('fs-extra');

getLastCommit(function(err, commit) {
    // read commit object properties
    console.log(commit);

    if (!commit) {
        commit = {};
    }

    commit.version = version;
    commit.app_git_version = commit.version + (commit.shortHash ? '-' + commit.shortHash : '');

    const file = resolve(__dirname, 'environments', 'app.version.ts');
    if (fs.existsSync(file)) {
        fs.unlinkSync(file);
    }
    writeFileSync(file,
`// NOTICE: AUTO GENERATED FILE DURING "npm run prebuild"! DO NOT MANUALLY EDIT OR CHECKIN!
/* tslint:disable */
export const APP_VERSION = ${JSON.stringify(commit, null, 4)};
/* tslint:enable */
`, { encoding: 'utf-8' });

    console.log(`version info ${commit.app_git_version} available to ${relative(resolve(__dirname, '..'), file)}`);    
});
