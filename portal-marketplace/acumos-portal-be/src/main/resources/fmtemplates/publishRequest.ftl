<#-- 
 ===============LICENSE_START=======================================================
 Acumos
 ===================================================================================
 Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 ===================================================================================
 This Acumos software file is distributed by AT&T and Tech Mahindra
 under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
 This file is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ===============LICENSE_END=========================================================
-->
<html>		
    <body>
        <p>Dear <#if user.firstName??>${user.firstName},<#else>User,</#if></p>
        <p>
            A publish request has been created<#if notificationData.solutionName??> for solution: ${notificationData.solutionName}</#if>.<br/>
            <#if notificationData.publishRequestUrl??>Please review it at: <a href="${notificationData.publishRequestUrl}">${notificationData.publishRequestUrl}</a></#if>
        </p>

        <p>Thanks</p>
        <p>${signature?if_exists}</p>
    </body>
</html>