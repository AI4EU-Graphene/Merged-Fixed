/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
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
 * ===============LICENSE_END=========================================================
 */
package org.acumos.elk.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkServiceUtils {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static String executeScript(String action, String repoName) throws Exception {
		logger.debug("Inside executeScript action:  {}", action);
		String scriptFileName = "/maven/es_archive_script/archive_script.sh";
		ProcessBuilder processBuilder = null;
		Process process = null;
		BufferedReader reader = null;
		StringBuilder result = new StringBuilder();
		try {
			logger.debug("scriptFileName: {}", scriptFileName);
			logger.debug("action: {}", action);
			logger.debug("repoName: {}", repoName);
			String[] cmd = { "bash", scriptFileName, action, repoName };
			processBuilder = new ProcessBuilder(cmd);
			if (processBuilder != null) {
				process = processBuilder.start();
				int errCode = process.waitFor();
				logger.debug("cmd action:{} , Echo command executed, any errors? {}", action,
						(errCode == 0 ? "No" : "Yes"));
				String line = null;
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = reader.readLine()) != null) {
					result.append(line + System.getProperty("line.separator"));
				}
			}
			logger.debug("cmd>>{}", result.toString());

		} finally {
			if (null != process) {
				process.waitFor(10, TimeUnit.SECONDS);
				process.destroy();
			}
			if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("executeScript failed {}", e);
					throw e;
				}
			}
		}
		return String.valueOf(result);
	}

}