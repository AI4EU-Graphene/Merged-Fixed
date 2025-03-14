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

package org.acumos.cds.migrate.domain;

import java.util.List;

/**
 * Transports a list of file names.
 * 
 * <pre>
 * {
 * "status":null,
 * "status_code":0,
 * "response_detail":"Solutions fetched Successfully",
 * "response_code":null,
 * "response_body":["NJMap.ppt"],
 * "error_code":"100"
 * }
 * </pre>
 */
public class CMSNameList {

	private String status;
	private Integer status_code;
	private String response_detail;
	// Actually a number?
	private String response_code;
	// Why is this number in quotes?
	private String error_code;
	public List<String> response_body;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getStatus_code() {
		return status_code;
	}

	public void setStatus_code(Integer status_code) {
		this.status_code = status_code;
	}

	public String getResponse_detail() {
		return response_detail;
	}

	public void setResponse_detail(String response_detail) {
		this.response_detail = response_detail;
	}

	public String getResponse_code() {
		return response_code;
	}

	public void setResponse_code(String response_code) {
		this.response_code = response_code;
	}

	public String getError_code() {
		return error_code;
	}

	public void setError_code(String error_code) {
		this.error_code = error_code;
	}

	public List<String> getResponse_body() {
		return response_body;
	}

	public void setResponse_body(List<String> response_body) {
		this.response_body = response_body;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[status=" + getStatus() + ", status_code=" + getStatus_code()
				+ ", response_code=" + getResponse_code() + ", response_detail=" + getResponse_detail()
				+ ", response_body=" + response_body + "]";
	}
}
