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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.domain.MLPUserNotifPref;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.portal.be.common.ConfigConstants;
import org.acumos.portal.be.common.NotificationTemplate;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.service.MailJet;
import org.acumos.portal.be.service.MailService;
import org.acumos.portal.be.service.NotificationService;
import org.acumos.portal.be.transport.MLNotification;
import org.acumos.portal.be.transport.MLUserNotifPref;
import org.acumos.portal.be.transport.MailData;
import org.acumos.portal.be.transport.NotificationRequestObject;
import org.acumos.portal.be.util.PortalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends AbstractServiceImpl implements NotificationService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
 
	@Autowired
	private Environment env;
	
	@Autowired
	private MailService mailservice;

	@Autowired
	private MailJet mailJet;


	/*
	 * No
	 */
	public NotificationServiceImpl() {

	}

	@Override
	public MLNotification createNotification(MLPNotification mlpNotification) {
		//log.debug("createNotification`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLNotification mlNotification = PortalUtils.convertToMLNotification(dataServiceRestClient.createNotification(mlpNotification));
		return mlNotification;
	}

	@Override
	public List<MLNotification> getNotifications() {
		//log.debug("getNotifications`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient(); 
		RestPageResponse<MLPNotification> mlpSolutionsPaged = null;
		RestPageRequest pageRequest = new RestPageRequest();
		pageRequest.setPage(0);
		pageRequest.setSize(0);
		mlpSolutionsPaged = dataServiceRestClient.getNotifications(pageRequest);
		List<MLPNotification> mlpNotificationList = mlpSolutionsPaged.getContent();
		List<MLNotification> mlNotificationList = new ArrayList<>();
		if (mlpNotificationList != null) {
			for (MLPNotification mlpNotification : mlpNotificationList) {
				MLNotification mlNotification = PortalUtils.convertToMLNotification(mlpNotification);
				mlNotificationList.add(mlNotification);
			}
		}
		return mlNotificationList;
	}

	@Override
	public List<MLPUserNotification> getUserNotifications(String userId, RestPageRequest restPageRequest) {
		//log.debug("getUserNotifications`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient(); 
		RestPageResponse<MLPUserNotification> mlpNotificationList = dataServiceRestClient.getUserNotifications(userId,restPageRequest);	
		return mlpNotificationList.getContent();
	}

	@Override
	public void addNotificationUser(String notificationId, String userId) {
		//log.debug("addNotificationUser`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		dataServiceRestClient.addUserToNotification(notificationId,userId);
	}

	@Override 
	public void dropNotificationUser(String notificationId, String userId) {
		//log.debug("dropNotificationUser`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		dataServiceRestClient.dropUserFromNotification(notificationId,userId);
		dataServiceRestClient.deleteNotification(notificationId);
	}

	@Override 
	public void setNotificationUserViewed(String notificationId, String userId) {
		//log.debug("dropNotificationUser`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		dataServiceRestClient.setUserViewedNotification(notificationId,userId);
	}
	
	@Override
	public void deleteNotification(String notificationId) {
		//log.debug("deleteNotification`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		dataServiceRestClient.deleteNotification(notificationId);
	}

	@Override
	public int getNotificationCount() {
		//log.debug("getNotificationCount");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		Long count = dataServiceRestClient.getNotificationCount();
		return count.intValue();
	}
	
	@Override
	public void generateNotification(MLPNotification notification, String userId) {
		//log.debug("generateNotification");
		try {
			if (notification != null) {
				Instant startDate = Instant.now();
				Instant endDate = startDate.plus(365, ChronoUnit.DAYS);

				notification.setStart(startDate);
				notification.setEnd(endDate);
				notification.setCreated(startDate);
				MLNotification mlNotification = createNotification(notification);
				if (mlNotification.getNotificationId() != null && userId != null) {
					addNotificationUser(mlNotification.getNotificationId(), userId);
				}
			} else {
				log.error(
						"Notification message can not be null: generateNotification()");
			}
		} catch (Exception e) {
			log.error( "Exception Occurred while generateNotification", e.getMessage());
			log.debug(e.toString());
		}
	}
	
	@Override
	public List<MLUserNotifPref> getUserNotifPrefByUserId(String userId) {
		//log.debug("getUserNotificationPreferences`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		List<MLPUserNotifPref> mlpNotificationList = dataServiceRestClient.getUserNotificationPreferences(userId);
		List<MLUserNotifPref> mlNotificationList = new ArrayList<>(mlpNotificationList.size());
		for (MLPUserNotifPref mlpUserNotifPref : mlpNotificationList) {
			mlNotificationList.add(PortalUtils.convertToMLUserNotifPref(mlpUserNotifPref));
		}
		return mlNotificationList;
	}

	@Override
	public MLUserNotifPref createUserNotificationPreference(MLUserNotifPref mlUserNotifPref) {
		//log.debug("createUserNotificationPreference`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLPUserNotifPref mlpUserNotifPref = PortalUtils.convertToMLPUserNotifPref(mlUserNotifPref);
		mlpUserNotifPref = dataServiceRestClient.createUserNotificationPreference(mlpUserNotifPref);
		mlUserNotifPref = PortalUtils.convertToMLUserNotifPref(mlpUserNotifPref);
		return mlUserNotifPref;
	}
	
	@Override
	public void updateUserNotificationPreference(MLUserNotifPref mlUserNotifPref) {
		//log.debug("updateUserNotificationPreference`");
		ICommonDataServiceRestClient dataServiceRestClient = getClient();
		MLPUserNotifPref mlpUserNotifPref = PortalUtils.convertToMLPUserNotifPref(mlUserNotifPref);
		dataServiceRestClient.updateUserNotificationPreference(mlpUserNotifPref);

	}


	@Override
	public void sendUserNotification(NotificationRequestObject notificationRequest) throws AcumosServiceException {
		
		//log.debug("Notify User ={}", notificationRequest);
		ICommonDataServiceRestClient dataServiceRestClient = getClient();

		if (PortalUtils.isEmptyOrNullString(notificationRequest.getUserId())) {
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_PARAMETER, "User Id required for sending the notification");
		}
		if (PortalUtils.isEmptyOrNullString(notificationRequest.getMessageType())) {
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_PARAMETER, "Message Type required for sending the notification");
		}
		if (PortalUtils.isEmptyOrNullString(notificationRequest.getSeverity())) {
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_PARAMETER, "Severity required for sending the notification");
		}
		String messgeType = notificationRequest.getMessageType();
		String template = getTemplate(messgeType);
		
		if (PortalUtils.isEmptyOrNullString(template)) {
			throw new AcumosServiceException(AcumosServiceException.ErrorCode.INVALID_PARAMETER, "NO template found for message type : " + messgeType);
		}
		
		//Check severity and send the notification accordingly
		String userId = notificationRequest.getUserId();
		MLPUser user = null;
		user = dataServiceRestClient.getUser(userId);
		List<MLPUserNotifPref> mlpNotificationList = dataServiceRestClient.getUserNotificationPreferences(userId);
		// TODO : Separate this logic to some factory method to decide if email or other kind of notifications need to e send
		for (MLPUserNotifPref mlpUserNotifPref : mlpNotificationList) {
			String userPrefSev = mlpUserNotifPref.getMsgSeverityCode();
			String msgSev = notificationRequest.getSeverity();
			// TODO Introduce enum for severity codes instead of this workaround
			if (userPrefSev.equals(msgSev) || (msgSev.equals("HI") && userPrefSev.matches("HI|ME|LO")) || (msgSev.equals("ME") && userPrefSev.matches("ME|LO"))) {
				
				if(mlpUserNotifPref.getNotfDelvMechCode().matches("Email|EM")) {
					MailData mailData = new MailData();
			        mailData.setSubject(notificationRequest.getSubject());
			        mailData.setFrom(env.getProperty(ConfigConstants.portal_feature_email_from));
			        mailData.setTemplate(template);
			        List<String> to = new ArrayList<String>();
			        to.add(user.getEmail());
			        mailData.setTo(to);
			        Map<String, Object> model = new HashMap<String, Object>();
			        model.put("user", user);
			        model.put("notificationData", notificationRequest.getNotificationData());
					model.put("signature", env.getProperty(ConfigConstants.portal_feature_email_signature));
			        mailData.setModel(model);

					try {
						if (!PortalUtils
								.isEmptyOrNullString(env.getProperty(ConfigConstants.portal_feature_email_service))
								&& env.getProperty(ConfigConstants.portal_feature_email_service)
										.equalsIgnoreCase("smtp")) {
							//log.debug("sendUserNotification: using SMTP service");
							mailservice.sendMail(mailData);
						} else if (!PortalUtils
								.isEmptyOrNullString(env.getProperty(ConfigConstants.portal_feature_email_service))
								&& env.getProperty(ConfigConstants.portal_feature_email_service)
										.equalsIgnoreCase("mailjet")) {
							//log.debug("sendUserNotification: using MailJet service");
							mailJet.sendMail(mailData);
						} else {
							log.debug(
									"sendUserNotification: no email service configured in key "
											+ ConfigConstants.portal_feature_email_service);
						}
					} catch (MailException ex) {
						log.error(
								"sendUserNotification: failed to send mail to user " + user.getEmail(), ex.getMessage());
					}					
				} else {
					//If notification Delivery mechanism is not found or if the mechanism is not present then log error 
					//and iterate to next Mechanism if present with same severity.
					log.error("Delivery Mechanism is not present. Cannot send notification for Code : " + mlpUserNotifPref.getNotfDelvMechCode());
				}
			}
		}
		
	}
	
	// TODO : Separate this method to factory class to get the template names 
	private String getTemplate(String messageType) {
		String template = null;
		
		if ("ONBD_FAIL".equals(messageType)) {
			return NotificationTemplate.ONBD_FAIL_TEMPLATE;
		}
		if ("ONBD_SUCCESS".equals(messageType)) {
			return NotificationTemplate.ONBD_SUCCESS_TEMPLATE;
		}
		if ("PBL_REQUEST".equals(messageType)) {
			return NotificationTemplate.PBL_REQUEST_TEMPLATE;
		}
		return template;
	}
	
	@Override
    public int getUserUnreadNotificationCount(String userId){
        ICommonDataServiceRestClient dataServiceRestClient = getClient();
        Long count=dataServiceRestClient.getUserUnreadNotificationCount(userId);
        return count.intValue();
    }

	@Override
	public RestPageResponse<MLPUserNotification> getUserNotificationsPagination(String userId, RestPageRequest restPageRequest) {
		ICommonDataServiceRestClient dataServiceRestClient = getClient(); 
		RestPageResponse<MLPUserNotification> mlpUserNotification = dataServiceRestClient.getUserNotifications(userId,restPageRequest);	
		return mlpUserNotification;
	}
	
}
