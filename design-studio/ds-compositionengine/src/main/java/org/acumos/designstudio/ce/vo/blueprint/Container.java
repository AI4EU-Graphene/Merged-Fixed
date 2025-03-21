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

package org.acumos.designstudio.ce.vo.blueprint;

import java.io.Serializable;

public class Container implements Serializable{

	private static final long serialVersionUID = 7033995176723370491L;

	private String container_name;
	private BaseOperationSignature operation_signature;
	
	/**
	 * @return the container_name
	 */
	public String getContainer_name() {
		return container_name;
	}
	/**
	 * @param container_name the container_name to set
	 */
	public void setContainer_name(String container_name) {
		this.container_name = container_name.toLowerCase();
	}
	/**
	 * @return the operation_signature
	 */
	public BaseOperationSignature getOperation_signature() {
		return operation_signature;
	}
	/**
	 * @param operation_signature the operation_signature to set
	 */
	public void setOperation_signature(BaseOperationSignature operation_signature) {
		this.operation_signature = operation_signature;
	}
	
}
