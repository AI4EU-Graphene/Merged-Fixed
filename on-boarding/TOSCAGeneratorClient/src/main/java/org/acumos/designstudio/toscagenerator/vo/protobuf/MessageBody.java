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
import java.util.ArrayList;
import java.util.List;

public class MessageBody implements Serializable{
	
	private static final long serialVersionUID = -2474475011501700364L;
	private String messageName;
    private List<MessageargumentList> messageargumentList = new ArrayList<MessageargumentList>();
    public String getMessageName() {
		return messageName;
	}
	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}
	public List<MessageargumentList> getMessageargumentList() {
		return messageargumentList;
	}
	public void setMessageargumentList(List<MessageargumentList> messageargumentList) {
		this.messageargumentList = messageargumentList;
	}

}
