/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
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

package org.acumos.designstudio.toscagenerator.vo.protobuf;

import java.io.Serializable;

public class InputMessage implements Serializable {
	private static final long serialVersionUID = 4667774275328976156L;
	
	private String inputMessageName = "";
	private boolean stream = false;

	public void setStream(boolean s) {
		this.stream = s;
	}
	public boolean getStream() {
		return stream;
	}

	public String getInputMessageName() {
		return inputMessageName;
	}

	public void setInputMessageName(String inputMessageName) {
		this.inputMessageName = inputMessageName;
	}
}
