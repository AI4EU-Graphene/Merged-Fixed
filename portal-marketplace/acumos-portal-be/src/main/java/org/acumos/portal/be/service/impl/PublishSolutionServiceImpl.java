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

package org.acumos.portal.be.service.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPCatalog;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPPublishRequest;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.licensemanager.exceptions.LicenseAssetRegistrationException;
import org.acumos.portal.be.common.CommonConstants;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.common.exception.UserServiceException;
import org.acumos.portal.be.service.LicensingService;
import org.acumos.portal.be.service.NotificationService;
import org.acumos.portal.be.service.PublishSolutionService;
import org.acumos.portal.be.service.UserRoleService;
import org.acumos.portal.be.transport.MLRole;
import org.acumos.portal.be.transport.NotificationRequestObject;
import org.acumos.portal.be.util.PortalUtils;
import org.acumos.portal.be.util.URIBuildUtils;
import org.acumos.portal.be.security.RoleAuthorityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class PublishSolutionServiceImpl extends AbstractServiceImpl implements PublishSolutionService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	
	@Autowired
	private Environment env;
	
	@Autowired
	private LicensingService licensingService;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private NotificationService notificationService;

	private static final String MSG_SEVERITY_ME = "ME";

	
	public PublishSolutionServiceImpl() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String publishSolution(String solutionId, String visibility, String userId, String revisionId, String catalogId, UUID trackingId) {
		log.debug("publishModelBySolution ={}", solutionId);
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLPSolution mlpSolution2 = null;
		
		String publishStatus = ""; 
		try{
			mlpSolution2 = dataServiceRestClient.getSolution(solutionId);
			if(mlpSolution2 != null && mlpSolution2.getUserId().equalsIgnoreCase(userId)) {
				//Invoke the Validation API if the validation with Backend is enabled.
				if(!PortalUtils.isEmptyOrNullString(env.getProperty("portal.feature.enablePublication")) && env.getProperty("portal.feature.enablePublication").equalsIgnoreCase("true")) {
					MLPSolutionRevision mlpSolutionRevision = dataServiceRestClient.getSolutionRevision(solutionId, revisionId);
					if(mlpSolutionRevision != null) {
						//Check if validation is required
						MLPCatalog catalog = dataServiceRestClient.getCatalog(catalogId);
						//If the request is for public then only go for admin approval. Else publish the revision.
						if(!PortalUtils.isEmptyOrNullString(visibility) && visibility.equalsIgnoreCase(CommonConstants.PUBLIC) && (!catalog.isSelfPublish())) {
							publishStatus = publishApprovalRequest(solutionId, userId, revisionId, catalogId, dataServiceRestClient,
									mlpSolution2);
						
						}else if(!PortalUtils.isEmptyOrNullString(visibility) && visibility.equalsIgnoreCase(CommonConstants.RESTRICTED) && (!catalog.isSelfPublish())) {
							publishStatus = publishApprovalRequest(solutionId, userId, revisionId, catalogId, dataServiceRestClient,
									mlpSolution2);
							
						} else {
							dataServiceRestClient.addSolutionToCatalog(solutionId, catalogId);
							boolean isLicenseAssetRegisterd = false;
							isLicenseAssetRegisterd = licensingService.licenseAssetRegister(solutionId, revisionId, userId);
							if(isLicenseAssetRegisterd) {
								publishStatus = "Solution "+mlpSolution2.getName()+" Published Successfully";
							}
							else {
								dataServiceRestClient.dropSolutionFromCatalog(solutionId, revisionId);
								publishStatus = "Failed to publish the solution, please try again later";
							}
						}
					}
				} else {
					dataServiceRestClient.addSolutionToCatalog(solutionId, catalogId);
					boolean isLicenseAssetRegisterd = false;
					isLicenseAssetRegisterd = licensingService.licenseAssetRegister(solutionId, revisionId, userId);
					if(isLicenseAssetRegisterd) {
						publishStatus = "Solution "+mlpSolution2.getName()+" Published Successfully";
					}
					else {
						dataServiceRestClient.dropSolutionFromCatalog(solutionId, revisionId);
						publishStatus = "Failed to publish the solution, please try again later";
					}
				}
			}
		}		
		catch (Exception e) {
			publishStatus = "Failed to publish the solution, please try again later";
			log.error("Exception Occurred while Publishing Solution ={}", e.getMessage());
		}
		return publishStatus;
	}

	private String publishApprovalRequest(String solutionId, String userId, String revisionId, String catalogId,
			ICommonDataServiceRestClient dataServiceRestClient, MLPSolution mlpSolution2) {
		String publishStatus = "";
		MLPPublishRequest publishRequest = new MLPPublishRequest();
		publishRequest.setSolutionId(solutionId);
		publishRequest.setRevisionId(revisionId);
		publishRequest.setCatalogId(catalogId);
		publishRequest.setRequestUserId(userId);
		//Get Status Code from CDS and then populate 
		publishRequest.setStatusCode(CommonConstants.PUBLISH_REQUEST_PENDING);
		
		//Create separate service for creating request and use single service all over the code
		publishRequest = dataServiceRestClient.createPublishRequest(publishRequest);

		try {
			if (publishRequest.getRequestId() != null) {
				generateNotificationsForPublishRequest(publishRequest, mlpSolution2.getName());
			}
		} catch (UserServiceException | AcumosServiceException ex) {
			log.error("generateNotificationsForPublishRequest failed ={}", ex.getMessage());
		}
		
		log.info("publish request has been created for solution {} with request Id as {}  ", solutionId, publishRequest.getRequestId());
		// Change the return type to send the message that request has been created 
		publishStatus = "Solution "+mlpSolution2.getName()+" Pending for Publisher Approval";
		return  publishStatus;
	}

	private void generateNotificationsForPublishRequest(MLPPublishRequest publishRequest, String solutionName) throws UserServiceException, AcumosServiceException {
		List<MLRole> allRoles = userRoleService.getAllRoles();
		String publisherRoleId = null;

		for (MLRole r : allRoles) {
			if (RoleAuthorityConstants.PUBLISHER.equalsIgnoreCase(r.getName())) {
				publisherRoleId = r.getRoleId();
				break;
			}
		}

		if (publisherRoleId == null) {
			log.error("Publisher role id not found");
			return;
		}

		List<MLPUser> publishers = userRoleService.getRoleUsers(publisherRoleId);
		String portalAddress = env.getProperty("portal.ui.server.address");
		String publishRequestUrl = portalAddress + "/#/publishRequest";
		String message = "A publish request has been created for solution: " + solutionName + "\nPlease review it at: " + publishRequestUrl;
		String subject = env.getProperty("portal.feature.mail.subject.publishrequest", "Pending publish request");
		String severity = MSG_SEVERITY_ME;

		for (MLPUser pubUser : publishers) {	
			log.info("generateNotification for user " + pubUser.getUserId() + " (" + message + ")");
			// Notification in the web interface	
			MLPNotification notificationObj = new MLPNotification();
			notificationObj.setMsgSeverityCode(severity);
			notificationObj.setMessage(message);
			notificationObj.setTitle(subject);
			notificationService.generateNotification(notificationObj, pubUser.getUserId());

			// E-Mail notification
			NotificationRequestObject mailRequest = new NotificationRequestObject();
			mailRequest.setMessageType("PBL_REQUEST");
			mailRequest.setSeverity(severity);
			mailRequest.setSubject(subject);
			mailRequest.setUserId(pubUser.getUserId());
			Map<String, String> notifyBody = new HashMap<String, String>();
			notifyBody.put("solutionName", solutionName);
			notifyBody.put("publishRequestUrl", publishRequestUrl);
			mailRequest.setNotificationData(notifyBody);
			notificationService.sendUserNotification(mailRequest);
		}
	}	
	
	@Override
	public String unpublishSolution(String solutionId, String catalogId, String userId,long publishRequestId) {
		//TODO: Need to revisit the un-publish the solution revision. Currently this service is not being used in portal.
		log.debug("unpublishModelBySolutionId ={}", solutionId);
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLPSolution mlpSolution = new MLPSolution();
		/*mlpSolution.setAccessTypeCode(accessType);*/
		mlpSolution.setSolutionId(solutionId);
		mlpSolution.setUserId(userId);
		MLPSolution mlpSolution2 = null;
		
		//TODO version needs to be noted as we need to only publish specific version		
		String unpublishedStatus = ""; 
		try{
			//Unpublish the Solution
			mlpSolution2 = dataServiceRestClient.getSolution(solutionId);
			if(mlpSolution2 != null && mlpSolution2.getUserId().equalsIgnoreCase(userId)) {
				dataServiceRestClient.dropSolutionFromCatalog(solutionId, catalogId);
				if(publishRequestId != 0 ){
					dataServiceRestClient.deletePublishRequest(publishRequestId);
				}
				unpublishedStatus = "Solution "+mlpSolution2.getName()+" Unpublished Successfully";
			}
			
		} catch (Exception e) {
			unpublishedStatus = "Failed to Unpublish the solution";
			log.error("Exception Occurred while UnPublishing Solution ={}", e.getMessage());
		}
		
		return unpublishedStatus;
	}

	@Override
	public boolean checkUniqueSolName(String solutionId) {
		log.debug("checkUniqueSolName ={}", solutionId);
		ICommonDataServiceRestClient dataServiceRestClient = getClient();

		MLPSolution solution = dataServiceRestClient.getSolution(solutionId);
		String[] name = { solution.getName() };

		Map<String, String> queryParameters = new HashMap<>();
		//Fetch the maximum possible records. Need an api that could return the exact match of names along with other nested filter criteria
		RestPageResponse<MLPSolution> searchSolResp = dataServiceRestClient.findPortalSolutions(name, null, true, null,
				null, null, null, null, new RestPageRequest(0, 10000, queryParameters));
		List<MLPSolution> searchSolList = searchSolResp.getContent();

		//removing the same solutionId from the list
		List<MLPSolution> filteredSolList1 = searchSolList.stream()
				.filter(searchSol -> !searchSol.getSolutionId().equalsIgnoreCase(solution.getSolutionId()))
				.collect(Collectors.toList());
		
		//Consider only those records that have exact match with the solution name
		List<MLPSolution> filteredSolList = filteredSolList1.stream()
				.filter(searchSol -> searchSol.getName().equalsIgnoreCase(solution.getName()))
				.collect(Collectors.toList());

		if (!filteredSolList.isEmpty()) {
			return false;
		}

		return true;
	}
	

}
