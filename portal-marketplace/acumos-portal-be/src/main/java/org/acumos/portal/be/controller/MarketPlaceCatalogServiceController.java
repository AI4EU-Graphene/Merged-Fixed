/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.acumos.portal.be.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acumos.cds.domain.MLPArtifact;
import org.acumos.cds.domain.MLPDocument;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionFavorite;
import org.acumos.cds.domain.MLPSolutionRating;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.domain.MLPTag;
import org.acumos.cds.domain.MLPUser;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.licensemanager.client.model.LicenseRtuVerification;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.portal.be.APINames;
import org.acumos.portal.be.common.CredentialsService;
import org.acumos.portal.be.common.JSONTags;
import org.acumos.portal.be.common.JsonRequest;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.common.RestPageRequestBE;
import org.acumos.portal.be.common.RestPageResponseBE;
import org.acumos.portal.be.common.VerifySolutionActionResponse;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.config.AsyncConfiguration;
import org.acumos.portal.be.service.CatalogService;
import org.acumos.portal.be.service.MarketPlaceCatalogService;
import org.acumos.portal.be.service.NotificationService;
import org.acumos.portal.be.service.PushAndPullSolutionService;
import org.acumos.portal.be.service.UserService;
import org.acumos.portal.be.service.impl.MarketPlaceCatalogServiceImpl;
import org.acumos.portal.be.transport.Author;
import org.acumos.portal.be.transport.CatalogSearchRequest;
import org.acumos.portal.be.transport.MLArtifact;
import org.acumos.portal.be.transport.MLCatalog;
import org.acumos.portal.be.transport.MLSolution;
import org.acumos.portal.be.transport.MLSolutionRating;
import org.acumos.portal.be.transport.MLSolutionWeb;
import org.acumos.portal.be.transport.RestPageRequestPortal;
import org.acumos.portal.be.transport.RevisionDescription;
import org.acumos.portal.be.transport.User;
import org.acumos.portal.be.util.PortalConstants;
import org.acumos.portal.be.util.PortalUtils;
import org.acumos.portal.be.util.SanitizeUtils;
import org.acumos.securityverification.domain.Workflow;
import org.acumos.securityverification.utils.SVConstants;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/")
public class MarketPlaceCatalogServiceController extends AbstractController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private MarketPlaceCatalogService marketPlaceService;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private UserService userService;

	@Autowired
	private PushAndPullSolutionService pushAndPullSolutionService;

	@Autowired
	private Environment env;

	@Autowired
	MarketPlaceCatalogServiceImpl impl;

	@Autowired
	CredentialsService credentialService;

	private static final String MSG_SEVERITY_ME = "ME";

	
	/**
	 * 
	 */
	public MarketPlaceCatalogServiceController() {
		// TODO Auto-generated constructor stub
	}

	@ApiOperation(
			value = "Gets a Solution Detail for the given SolutionId. Same API can be used for both Solution Owner view as well as General user. API will return isOwner as true if the user is owner of the solution",
			response = MLSolution.class)
	@RequestMapping(value = {APINames.SOLUTIONS_DETAILS}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> getSolutionsDetails(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @PathVariable("revisionId") String revisionId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		MLSolution solutionDetail = null;
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			solutionDetail = marketPlaceService.getSolution(solutionId, revisionId,
					(String) request.getAttribute("loginUserId"));
			if (solutionDetail != null) {
				data.setResponseBody(solutionDetail);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("getSolutionsDetails :  ", solutionDetail);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("Solutions Not fetched Successfully");
				log.debug("getSolutionsDetails :  ", solutionDetail);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}

		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			log.error("Exception Occurred Fetching Solutions Detail for solutionId :" + "solutionId", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get search solution according to queryparamters sent.",
			response = MLSolution.class, responseContainer = "List")
	@RequestMapping(value = {APINames.SEARCH_SOLUTION}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLSolution>> getSearchSolutions(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "search", required = true) String search) {
		List<MLSolution> mlSolutions = null;
		JsonResponse<List<MLSolution>> data = new JsonResponse<>();
		try {
			mlSolutions = marketPlaceService.getSearchSolution(search);
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("getSolutionsList: size is {} ", mlSolutions.size());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions for Market Place Catalog", e.getMessage());
		}
		return data;
	}

	/**
	 * @param mlSolution Solution
	 * @return List of Paginated ML Solutions in JSON format.
	 */
	@ApiOperation(value = "Gets a list of Paginated Solutions for Market Place Catalog.",
			response = MLSolution.class, responseContainer = "List")
	@RequestMapping(value = {APINames.PAGINATED_SOLUTIONS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponse<MLPSolution>> getPaginatedList(
			@RequestBody JsonRequest<MLSolution> mlSolution) {
		RestPageResponse<MLPSolution> paginatedSolution = null;
		JsonResponse<RestPageResponse<MLPSolution>> data = new JsonResponse<>();
		try {
			Integer page = mlSolution.getBody().getPageNo();
			Integer size = mlSolution.getBody().getSize();
			String sortingOrder = mlSolution.getBody().getSortingOrder();
			paginatedSolution = marketPlaceService.getAllPaginatedSolutions(page, size, sortingOrder);
			if (paginatedSolution != null) {
				data.setResponseBody(paginatedSolution);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.info("getSolutionsList: size is {} ", paginatedSolution.getSize());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions for Market Place Catalog", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Updates a given Solution for a provided SolutionId.",
			response = MLSolution.class)
	@RequestMapping(value = {APINames.SOLUTIONS_UPDATE}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> updateSolutionDetails(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("solutionId") String solutionId,
			@RequestBody JsonRequest<MLSolution> mlSolution) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		log.debug("updateSolutionDetails={}", solutionId);
		MLSolution solutionDetail = null;
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			if (mlSolution.getBody() != null) {

				// Check for the unique name in the market place before
				// publishing.
				if (!marketPlaceService.checkUniqueSolName(solutionId, mlSolution.getBody().getName())) {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE);
					data.setResponseDetail(
							"Model name is not unique. Please update model name before publishing");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return data;
				}

				marketPlaceService.updateSolution(mlSolution.getBody(), solutionId);
				data.setResponseBody(solutionDetail);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solution updated successfully.");
				response.setStatus(HttpServletResponse.SC_OK);
			} else
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred while updateSolutionDetails()", e.getMessage());
		}
		return data;
	}

	@ApiOperation(
			value = "Delete Artifacts of a given Solution for a provided SolutionId and RevisionId.",
			response = MLSolution.class)
	@RequestMapping(value = {APINames.ARTIFACT_DELETE}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> deleteSolutionArtifacts(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId,
			@RequestBody JsonRequest<MLSolution> mlSolution) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		log.debug("deleteSolutionArtifacts={}", solutionId, revisionId);
		MLSolution solutionDetail = null;
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			if (mlSolution.getBody() != null) {

				// Check for the unique name in the market place before
				// publishing.
				if (!marketPlaceService.checkUniqueSolName(solutionId, mlSolution.getBody().getName())) {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE);
					data.setResponseDetail(
							"Model name is not unique. Please update model name before publishing");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return data;
				}

				marketPlaceService.deleteSolutionArtifacts(mlSolution.getBody(), solutionId, revisionId);
				data.setResponseBody(solutionDetail);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solution updated successfully.");
				response.setStatus(HttpServletResponse.SC_OK);
			} else
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail("Unable to delete model image due to no image found on Nexus");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("AcumosServiceException Occurred while deleteSolutionArtifacts()", e.getMessage());
		} catch (URISyntaxException uriEx) {
			data.setErrorCode("401");
			data.setResponseDetail("Unable to delete  Artifact from Nexus");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("URISyntaxException Occurred while deleteSolutionArtifacts()", uriEx.getMessage());
		}
		return data;
	}

	/**
	 * @param request    HttpServletRequest
	 * @param response   HttpServletResponse
	 * @param solutionId solution ID
	 * @return List of Published ML Solutions in JSON format.
	 */
	@ApiOperation(
			value = "Gets a list of Solution Revision from the Catalog of the local Acumos Instance .",
			response = MLPSolutionRevision.class, responseContainer = "List")
	@RequestMapping(value = {APINames.SOLUTIONS_REVISIONS}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLPSolutionRevision>> getSolutionsRevisionList(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("solutionId") String solutionId) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		JsonResponse<List<MLPSolutionRevision>> data = new JsonResponse<List<MLPSolutionRevision>>();
		List<MLPSolutionRevision> peerCatalogSolutionRevisions = null;
		try {
			peerCatalogSolutionRevisions = marketPlaceService.getSolutionRevision(solutionId);
			if (peerCatalogSolutionRevisions != null) {
				data.setResponseBody(peerCatalogSolutionRevisions);
				data.setResponseCode(String.valueOf(HttpServletResponse.SC_OK));
				data.setResponseDetail(JSONTags.TAG_STATUS_SUCCESS);
				data.setStatus(true);
				response.setStatus(HttpServletResponse.SC_OK);
				log.debug("getSolutionsRevisionList: size is {} ", peerCatalogSolutionRevisions.size());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			data.setStatus(false);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred Fetching Solution Revisions for Market Place Catalog", e.getMessage());
		}
		return data;
	}

	/**
	 * @param request    HttpServletRequest
	 * @param response   HttpServletResponse
	 * @param solutionId solution ID
	 * @param revisionId revision ID
	 * @return List of Published ML Solutions in JSON format.
	 */

	@ApiOperation(
			value = "Gets a list of Solution Revision Artifacts from the Catalog of the local Acumos Instance .",
			response = MLPArtifact.class, responseContainer = "List")
	@RequestMapping(value = {APINames.SOLUTIONS_REVISIONS_ARTIFACTS}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLArtifact>> getSolutionsRevisionArtifactList(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		JsonResponse<List<MLArtifact>> data = new JsonResponse<List<MLArtifact>>();
		List<MLArtifact> filteredPeerSolutionArtifacts = new ArrayList<MLArtifact>();

		List<MLPArtifact> peerSolutionArtifacts = null;
		try {
			peerSolutionArtifacts = marketPlaceService.getSolutionArtifacts(solutionId, revisionId);
			if (peerSolutionArtifacts != null) {
				peerSolutionArtifacts.forEach((MLPArtifact mlpArtifact) -> {
					String str = mlpArtifact.getArtifactTypeCode();
					String content = null;
					try {
						String nexusUrl = env.getProperty("nexus.url");
						String nexusUserName = env.getProperty("nexus.username");
						String nexusPd = env.getProperty("nexus.password");
						// String str1 = "{\"probeIndicator\":\"false\",\"validSolution\":true}";
						NexusArtifactClient client = impl.nexusArtifactClient(nexusUrl, nexusUserName, nexusPd);
						ByteArrayOutputStream stream = client.getArtifact(str);
						String strCheck = stream.toString();
						ObjectMapper mapper = new ObjectMapper();
						JsonNode node = mapper.readTree(strCheck);
						content = node.get("validSolution").asText();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if ("DI".equals(mlpArtifact.getArtifactTypeCode())) {
						String[] st = mlpArtifact.getUri().split("/");
						String imagetag_prefix = st[0];
						if (env.getProperty("docker.registry.url") != null
								&& imagetag_prefix.equalsIgnoreCase(env.getProperty("docker.registry.url")
										.replaceAll("http://", "").replaceAll("https://", "").replaceAll("/", ""))) {
							filteredPeerSolutionArtifacts
									.add(PortalUtils.convertToMLArtifact(mlpArtifact, false, content));
						} else {
							filteredPeerSolutionArtifacts
									.add(PortalUtils.convertToMLArtifact(mlpArtifact, true, content));
						}
					} else {
						filteredPeerSolutionArtifacts
								.add(PortalUtils.convertToMLArtifact(mlpArtifact, false, content));
					}
				});
				data.setResponseBody(filteredPeerSolutionArtifacts);
				data.setResponseCode(String.valueOf(HttpServletResponse.SC_OK));
				data.setResponseDetail(JSONTags.TAG_STATUS_SUCCESS);
				data.setStatus(true);
				response.setStatus(HttpServletResponse.SC_OK);
				log.debug("getSolutionsRevisionArtifactList: size is {} ", peerSolutionArtifacts.size());
			}
		} catch (AcumosServiceException e) {
			data.setResponseCode(String.valueOf(HttpServletResponse.SC_BAD_REQUEST));
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			data.setStatus(false);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred Fetching Solution Revisions Artifacts for Market Place Catalog",
					e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Add tag for a provided SolutionId.", response = MLSolution.class)
	@RequestMapping(value = {APINames.ADD_TAG}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> addSolutionTag(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @PathVariable("tag") String tag,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		tag = SanitizeUtils.sanitize(tag);

		log.debug("addSolutionTag={}", solutionId);
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(solutionId) && !PortalUtils.isEmptyOrNullString(tag)) {
				marketPlaceService.addSolutionTag(solutionId, tag);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solution updated successfully.");
				response.setStatus(HttpServletResponse.SC_OK);
			} else
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred while addSolutionTag()", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Updates a given Solution for a provided SolutionId.",
			response = MLSolution.class)
	@RequestMapping(value = {APINames.DROP_TAG}, method = RequestMethod.DELETE,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> dropSolutionTag(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @PathVariable("tag") String tag,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		tag = SanitizeUtils.sanitize(tag);

		log.debug("addSolutionTag={}", solutionId);
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(solutionId) && !PortalUtils.isEmptyOrNullString(tag)) {
				marketPlaceService.dropSolutionTag(solutionId, tag);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solution updated successfully.");
				response.setStatus(HttpServletResponse.SC_OK);
			} else
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred while dropSolutionTag()", e.getMessage());
		}
		return data;
	}

	/**
	 * 
	 * @param restPageReq rest page request
	 * @return Rest page response
	 */
	@ApiOperation(value = "Gets a list of tags for Market Place Catalog.",
			response = RestPageResponseBE.class)
	@RequestMapping(value = {APINames.TAGS}, method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE> getTagsList(
			@RequestBody JsonRequest<RestPageRequest> restPageReq) {
		log.debug("getTagsList");
		List<String> mlTagsList = new ArrayList<>();
		JsonResponse<RestPageResponseBE> data = new JsonResponse<>();
		try {
			mlTagsList = marketPlaceService.getTags(restPageReq);
			if (mlTagsList != null) {
				List test = new ArrayList<>();
				RestPageResponseBE responseBody = new RestPageResponseBE<>(test);
				responseBody.setTags(mlTagsList);
				data.setResponseBody(responseBody);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Tags fetched Successfully");
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE);
				data.setResponseDetail("Exception Occurred Fetching tags for Market Place Catalog");
				log.error("Exception Occurred Fetching tags for Market Place Catalog");
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching tags for Market Place Catalog", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Gets a list of preferred tags for Market Place Catalog.",
			response = RestPageResponseBE.class)
	@RequestMapping(value = {APINames.PREFERRED_TAGS}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE> getPreferredTagsList(
			@RequestBody JsonRequest<RestPageRequest> restPageReq,
			@PathVariable("userId") String userId) {

		userId = SanitizeUtils.sanitize(userId);

		log.debug("getPreferredTagsList");
		List<String> mlTagsList = new ArrayList<>();
		JsonResponse<RestPageResponseBE> data = new JsonResponse<>();
		try {
			List<Map<String, String>> prefTagsList =
					marketPlaceService.getPreferredTagsList(restPageReq, userId);
			if (mlTagsList != null) {
				List content = new ArrayList<>();
				RestPageResponseBE responseBody = new RestPageResponseBE<>(content);
				responseBody.setPrefTags(prefTagsList);
				data.setResponseBody(responseBody);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Tags fetched Successfully");
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE);
				data.setResponseDetail(
						"Exception Occurred Fetching Preferred tags for Market Place Catalog");
				log.error("Exception Occurred Fetching Preferred tags for Market Place Catalog");
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Preferred tags for Market Place Catalog", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Create User Tag", response = MLPTag.class)
	@RequestMapping(value = {APINames.CREATE_USER_TAG}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE> createUserTag(@PathVariable("userId") String userId,
			@RequestBody JsonRequest<RestPageRequestBE> tagListReq) {

		userId = SanitizeUtils.sanitize(userId);

		JsonResponse<RestPageResponseBE> data = new JsonResponse<>();
		try {
			List<String> tagList = tagListReq.getBody().getTagList();
			List<String> dropTagList = tagListReq.getBody().getDropTagList();
			marketPlaceService.createUserTag(userId, tagList, dropTagList);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("User Tags created Successfully");
			log.debug("createUserTag :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception occured while createUserTag");
			log.error("Exception Occurred createUserTag :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Gets a All Solutions for the User for Manage Models Screen.",
			response = MLSolution.class, responseContainer = "List")
	@RequestMapping(value = {APINames.SEARCH_SOLUTION_TAGS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> getTagsSolutions(
			@PathVariable("tags") String tags, @RequestBody JsonRequest<RestPageRequestBE> restPageReq) {

		tags = SanitizeUtils.sanitize(tags);

		RestPageResponseBE<MLSolution> mlSolutions = null;
		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		try {
			mlSolutions = marketPlaceService.getTagBasedSolutions(tags, restPageReq);
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("getMySolutions: size is {} ", mlSolutions.getSize());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions for a User for Manage My Models", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Gets a user access Detail for the given SolutionId.",
			response = User.class)
	@RequestMapping(value = {APINames.SOLUTION_USER_ACCESS}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE> getSolutionUserAccess(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		List<User> userList = new ArrayList<>();
		JsonResponse<RestPageResponseBE> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(solutionId)) {
				userList = marketPlaceService.getSolutionUserAccess(solutionId);
				if (userList != null) {
					List test = new ArrayList<>();
					RestPageResponseBE responseBody = new RestPageResponseBE<>(test);
					responseBody.setUserList(userList);
					data.setResponseBody(responseBody);
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					data.setResponseDetail("Users for solution fetched Successfully");
					log.debug("getSolutionUserAccess :  ", userList);
				} else {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
					data.setResponseDetail("Error occured while fetching Users for solution");
					log.error("Error Occurred Fetching Users for solution :" + solutionId);
				}
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("solutionId not present");
			}

		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions Detail for solutionId :" + "solutionId", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Adds  user access Detail for the given SolutionId.")
	@RequestMapping(value = {APINames.SOLUTION_USER_ACCESS_ADD}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<User> addSolutionUserAccess(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @RequestBody JsonRequest<List<String>> userId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		JsonResponse<User> data = new JsonResponse<>();
		List<User> userList = new ArrayList<>();
		boolean exist = false;
		try {
			Workflow workflow = performSVScan(solutionId, SVConstants.SHARE);
			if (workflow.isWorkflowAllowed()) {
				List<String> userIdList = userId.getBody();
				if (!PortalUtils.isEmptyOrNullString(solutionId)) {
					userList = marketPlaceService.getSolutionUserAccess(solutionId);
					if (userList != null) {
						for (User user : userList) {
							if (user.getUserId().equals(userId)) {
								exist = true;
								break;
							}
						}
					}
				}

				if (!exist) {
					marketPlaceService.addSolutionUserAccess(solutionId, userIdList);

					// code to create notification
					for (String userID : userIdList) {
						MLPNotification notification = new MLPNotification();
						String notifMsg = null;
						MLSolution solutionDetail = marketPlaceService.getSolution(solutionId);
						MLPUser mlpUser = userService.findUserByUserId(userID);
						notifMsg = solutionDetail.getName() + " shared with " + mlpUser.getLoginName();
						notification.setMessage(notifMsg);
						notification.setTitle(notifMsg);
						notification.setMsgSeverityCode(MSG_SEVERITY_ME);
						notificationService.generateNotification(notification, mlpUser.getUserId());
					}
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					data.setResponseDetail("Users access for solution added Successfully");
					log.debug("addSolutionUserAccess :  ");
				} else {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
					data.setResponseDetail("User already assigned for solution");
					log.error("Error User already assigned for solution :" + solutionId);
				}
			} else {
				data.setErrorCode(
						(isReasonInfo(workflow.getReason())) ? JSONTags.TAG_INFO_SV : JSONTags.TAG_ERROR_SV);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				data.setResponseDetail(workflow.getReason());
				log.error("SV failure while addSolutionUserAccess() : " + workflow.getReason());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while addSolutionUserAccess() :" + "solutionId", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Adds  user access Detail for the given SolutionId.", response = User.class)
	@RequestMapping(value = {APINames.SOLUTION_USER_ACCESS_DELETE}, method = RequestMethod.DELETE,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<User> dropSolutionUserAccess(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @PathVariable("userId") String userId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		userId = SanitizeUtils.sanitize(userId);

		JsonResponse<User> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(solutionId)
					&& !PortalUtils.isEmptyOrNullString(userId)) {
				marketPlaceService.dropSolutionUserAccess(solutionId, userId);
				// code to create notification
				MLPNotification notification = new MLPNotification();
				String notificationMsg = null;
				MLSolution solutionDetail = marketPlaceService.getSolution(solutionId);
				MLPUser user = userService.findUserByUserId(userId);
				notificationMsg = solutionDetail.getName() + " unshared with " + user.getLoginName();
				notification.setMessage(notificationMsg);
				notification.setTitle(notificationMsg);
				notification.setMsgSeverityCode(MSG_SEVERITY_ME);
				notificationService.generateNotification(notification, userId);

				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Users access for solution droped Successfully");
				log.debug("dropSolutionUserAccess :  ");
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("Failure solutionId/userId not present");
				log.error("Exception Occurred Fetching Users for solution :" + solutionId);
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while dropSolutionUserAccess() :" + "solutionId", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Update solution view count", response = MLSolution.class)
	@RequestMapping(value = {APINames.UPDATE_VIEW_COUNT}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> incrementSolutionViewCount(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		MLSolution solutionDetail = null;
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			marketPlaceService.incrementSolutionViewCount(solutionId);
			// code to create notification
			MLSolution solution = marketPlaceService.getSolution(solutionId);
			int viewCount = solution.getViewCount();
			if (viewCount != 0 && viewCount % 10 == 0) {
				MLPNotification notification = new MLPNotification();
				String notificationMsg = null;
				notificationMsg = "View count for " + solution.getName() + " increased by 10";
				notification.setMessage(notificationMsg);
				notification.setTitle(notificationMsg);
				notification.setMsgSeverityCode(MSG_SEVERITY_ME);
				notificationService.generateNotification(notification, solution.getOwnerId());
			}
			data.setResponseBody(solution);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Solutions fetched Successfully");
			log.debug("incrementSolutionViewCount :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred incrementSolutionViewCount :" + "solutionId", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Create ratings for solution", response = MLSolution.class)
	@RequestMapping(value = {APINames.CREATE_RATING}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> createSolutionRating(HttpServletRequest request,
			@RequestBody JsonRequest<MLPSolutionRating> mlpSolutionRating, HttpServletResponse response) {
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			marketPlaceService.createSolutionrating(mlpSolutionRating.getBody());
			// code to create notification
			MLPNotification notification = new MLPNotification();
			String notificationMsg = null;
			MLSolution solution =
					marketPlaceService.getSolution(mlpSolutionRating.getBody().getSolutionId());
			notificationMsg = "Ratings updated for " + solution.getName();
			notification.setMessage(notificationMsg);
			notification.setTitle(notificationMsg);
			notification.setMsgSeverityCode(MSG_SEVERITY_ME);
			notificationService.generateNotification(notification, solution.getOwnerId());

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Successfully updated solution rating");
			log.debug("createSolutionRating :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred createSolutionRating :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Update solution ratings", response = MLSolution.class)
	@RequestMapping(value = {APINames.UPDATE_RATING}, method = RequestMethod.PUT,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> updateSolutionRating(HttpServletRequest request,
			@RequestBody JsonRequest<MLPSolutionRating> mlpSolutionRating, HttpServletResponse response) {
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			marketPlaceService.updateSolutionRating(mlpSolutionRating.getBody());
			// code to create notification
			MLPNotification notification = new MLPNotification();
			String notificationMsg = null;
			MLSolution solution =
					marketPlaceService.getSolution(mlpSolutionRating.getBody().getSolutionId());
			notificationMsg = "Ratings updated for " + solution.getName();
			notification.setMessage(notificationMsg);
			notification.setTitle(notificationMsg);
			notification.setMsgSeverityCode(MSG_SEVERITY_ME);
			notificationService.generateNotification(notification, solution.getOwnerId());

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Solutions fetched Successfully");
			log.debug("updateSolutionRating :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred updateSolutionRating :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Gets models shared for the given userId.", response = MLSolution.class)
	@RequestMapping(value = {APINames.SHARED_MODELS_FOR_USER}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLSolution>> getMySharedModels(HttpServletRequest request,
			@PathVariable("userId") String userId, HttpServletResponse response) {

		userId = SanitizeUtils.sanitize(userId);

		List<MLSolution> modelList = new ArrayList<>();
		JsonResponse<List<MLSolution>> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(userId)) {
				RestPageRequest restPageReq = new RestPageRequest();
				modelList = marketPlaceService.getMySharedModels(userId, restPageReq);
				if (modelList != null) {
					data.setResponseBody(modelList);
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					data.setResponseDetail("Models shared with user fetched Successfully");
					log.debug("getMySharedModels :  ", modelList);
				} else {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
					data.setResponseDetail("No any model shared for userId : " + userId);
					log.error("No any model shared for userId : " + userId);
				}
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
				data.setResponseDetail("userId not found");
			}

		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception occured while fetching models shared with userId :" + userId, e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Create favorite for solution", response = MLSolution.class)
	@RequestMapping(value = {APINames.CREATE_FAVORITE}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> createSolutionFavorite(HttpServletRequest request,
			@RequestBody JsonRequest<MLPSolutionFavorite> mlpSolutionFavorite,
			HttpServletResponse response) {
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			marketPlaceService.createSolutionFavorite(mlpSolutionFavorite.getBody());
			// code to create notification
			MLPNotification notification = new MLPNotification();
			String favorite = null;
			MLSolution solution =
					marketPlaceService.getSolution(mlpSolutionFavorite.getBody().getSolutionId());
			favorite = "Favorite created for " + solution.getName();
			notification.setMessage(favorite);
			notification.setTitle(favorite);
			notification.setMsgSeverityCode(MSG_SEVERITY_ME);
			notificationService.generateNotification(notification, solution.getOwnerId());

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Successfully created solution favorite");
			log.debug("createSolutionFavorite :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred createSolutionFavorite :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Delete favorite for solution", response = MLSolution.class)
	@RequestMapping(value = {APINames.DELETE_FAVORITE}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolution> deleteSolutionFavorite(HttpServletRequest request,
			@RequestBody JsonRequest<MLPSolutionFavorite> mlpSolutionFavorite,
			HttpServletResponse response) {
		JsonResponse<MLSolution> data = new JsonResponse<>();
		try {
			marketPlaceService.deleteSolutionFavorite(mlpSolutionFavorite.getBody());
			// code to create notification
			MLPNotification notification = new MLPNotification();
			String favorite = null;
			MLSolution solution =
					marketPlaceService.getSolution(mlpSolutionFavorite.getBody().getSolutionId());
			favorite = "Favorite deleted for " + solution.getName();
			notification.setMessage(favorite);
			notification.setTitle(favorite);
			notification.setMsgSeverityCode(MSG_SEVERITY_ME);
			notificationService.generateNotification(notification, solution.getOwnerId());

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Successfully deleted solution favorite");
			log.debug("deleteSolutionFavorite :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred deleteSolutionFavorite :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "get a list of favorite solutions for particuler userID",
			response = MLSolution.class)
	@RequestMapping(value = {APINames.USER_FAVORITE_SOLUTIONS}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLSolution>> getFavoriteSolutions(HttpServletRequest request,
			@PathVariable("userId") String userId, HttpServletResponse response) {

		userId = SanitizeUtils.sanitize(userId);

		JsonResponse<List<MLSolution>> data = new JsonResponse<>();
		try {
			RestPageRequest restPageReq = new RestPageRequest();
			List<MLSolution> mlSolutionList =
					marketPlaceService.getFavoriteSolutions(userId, restPageReq);
			if (mlSolutionList != null) {
				data.setResponseBody(mlSolutionList);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Favorite solutions  fetched Successfully");
				log.debug("getFavoriteSolutions: size is {} ", mlSolutionList.size());
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("No favorite solutions exist for user : " + userId);
				log.debug("No favorite solutions exist for user : " + userId);
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while getFavoriteSolutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get all related Solutions for the modelTypeId for Model Detail Screen.",
			response = MLSolution.class, responseContainer = "List")
	@RequestMapping(value = {APINames.RELATED_MY_SOLUTIONS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> getRelatedMySolutions(
			@RequestBody JsonRequest<RestPageRequestBE> restPageReq) {
		RestPageResponseBE<MLSolution> mlSolutions = null;
		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		try {
			mlSolutions = marketPlaceService.getRelatedMySolutions(restPageReq);
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("getRelatedMySolutions: size is {} ", mlSolutions.getSize());
			}
		} catch (AcumosServiceException e) {
			log.error("Exception Occurred while getRelatedMySolutions", e.getMessage());
			data.setErrorCode(e.getErrorCode());
			data.setResponseDetail(e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "API to read Image Artifact of the Machine Learning Solution",
			response = InputStream.class, responseContainer = "List", code = 200)
	@RequestMapping(value = {APINames.READ_SIGNATURE_TAB}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public String readArtifactSolutions(@PathVariable("artifactId") String artifactId,
			HttpServletRequest request, HttpServletResponse response) {

		artifactId = SanitizeUtils.sanitize(artifactId);

		InputStream resource = null;
		String outputString = "";
		try {

			String artifactFileName = pushAndPullSolutionService.getFileNameByArtifactId(artifactId);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("x-filename", artifactFileName);
			response.setHeader("Content-Disposition",
					"attachment; filename=\"" + artifactFileName + "\"");
			response.setStatus(HttpServletResponse.SC_OK);

			resource = pushAndPullSolutionService.downloadModelArtifact(artifactId);
			try {
				outputString = IOUtils.toString(resource, "UTF-8");
			} catch (IOException e) {
				throw new AcumosServiceException(AcumosServiceException.ErrorCode.IO_EXCEPTION,
						e.getMessage());
			}
		} catch (AcumosServiceException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred reading a artifact for a Solution in Market Place serive", e.getMessage());
		}
		return outputString;
	}

	@ApiOperation(value = "Get ratings for a solution Id", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.GET_SOLUTION_RATING}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponse<MLSolutionRating>> getSolutionRatings(
			@PathVariable String solutionId, @RequestBody JsonRequest<RestPageRequest> pageRequest) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		RestPageResponse<MLPSolutionRating> mlpSolutionRating = null;
		List<MLSolutionRating> mlSolutionRatingList = new ArrayList<MLSolutionRating>();
		JsonResponse<RestPageResponse<MLSolutionRating>> data = new JsonResponse<>();
		try {
			RestPageRequest restpageRequest = pageRequest.getBody();
			mlpSolutionRating = marketPlaceService.getSolutionRating(solutionId, restpageRequest);
			MLPUser mlpUser = null;
			for (MLPSolutionRating rating : mlpSolutionRating.getContent()) {
				mlpUser = userService.findUserByUserId(rating.getUserId());
				MLSolutionRating mlSolRating = PortalUtils.convertToMLSolutionRating(rating);
				mlSolRating.setUserName(mlpUser.getFirstName().concat(" ").concat(mlpUser.getLastName()));
				mlSolutionRatingList.add(mlSolRating);
			}

			RestPageResponse<MLSolutionRating> mlSolutionRating = PortalUtils
					.convertToMLSolutionRatingRestPageResponse(mlSolutionRatingList, mlpSolutionRating);

			if (mlSolutionRating != null) {
				data.setResponseBody(mlSolutionRating);
				data.setStatusCode(200);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("getSolutionRatings: size is {} ", mlSolutionRating.getSize());
			}
		} catch (AcumosServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception Occurred Fetching Ratings for Solutions");
			log.error("Exception Occurred Fetching Ratings for Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Create Tag", response = MLPTag.class)
	@RequestMapping(value = {APINames.CREATE_TAG}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPTag> createTag(HttpServletRequest request,
			@RequestBody JsonRequest<MLPTag> mlpTag, HttpServletResponse response) {
		JsonResponse<MLPTag> data = new JsonResponse<>();
		try {
			MLPTag tag = marketPlaceService.createTag(mlpTag.getBody());
			data.setResponseBody(tag);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Tags created Successfully");
			log.debug("createTag :  ");
		} catch (AcumosServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception occured while createTag");
			log.error("Exception Occurred createTag :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get ratings for a solution by user", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.GET_SOLUTION_RATING_USER}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPSolutionRating> getUserRatings(HttpServletRequest request,
			@PathVariable("solutionId") String solutionId, @PathVariable("userId") String userId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		userId = SanitizeUtils.sanitize(userId);

		JsonResponse<MLPSolutionRating> data = new JsonResponse<>();
		try {
			MLPSolutionRating mlSolutionRating = marketPlaceService.getUserRatings(solutionId, userId);
			if (mlSolutionRating != null) {
				data.setResponseBody(mlSolutionRating);
				data.setStatusCode(200);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Ratings fetched Successfully");
				log.debug("getUserRatings:  {} ", mlSolutionRating);
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception Occurred Fetching Ratings for Solutions");
			log.error("Exception Occurred Fetching Ratings for Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "findPortalSolutions", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.PORTAL_SOLUTIONS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> findPortalSolutions(
			HttpServletRequest request, @RequestBody JsonRequest<RestPageRequestPortal> restPageReqPortal,
			HttpServletResponse response) {

		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		String userId = (String) request.getAttribute("loginUserId");
		Set<MLPTag> prefTags = null;
		if (userId != null && !StringUtils.isEmpty(userId)) {
			MLPUser user = userService.findUserByUserId(userId);
			if (user != null) {
				prefTags = user.getTags();
			}
		}
		RestPageResponseBE<MLSolution> mlSolutions = null;
		try {
			mlSolutions = marketPlaceService.findPortalSolutions(restPageReqPortal.getBody(), prefTags);
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("findPortalSolutions: size is {} ", mlSolutions.getSize());
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "findPublicPortalSolutions", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.PORTAL_SOLUTIONS_PUBLIC}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> findPublicPortalSolutions(
			HttpServletRequest request, @RequestBody JsonRequest<RestPageRequestPortal> restPageReqPortal,
			HttpServletResponse response) {
		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		RestPageResponseBE<MLSolution> mlSolutions = null;

		try {
			if (restPageReqPortal.getBody().getCatalogIds() == null
					|| restPageReqPortal.getBody().getCatalogIds().length == 0) {
				String catalog_pagesize = env.getProperty("portal.feature.catalog.pagesize");
				Integer catalogPageSize = (catalog_pagesize != null) ? Integer.valueOf(catalog_pagesize)
						: PortalConstants.DEFAULT_CATALOG_PAGE_SIZE;
				CatalogSearchRequest catalogSearchRequest = new CatalogSearchRequest();
				catalogSearchRequest.setAccessTypeCode(PortalConstants.PUBLIC_CATALOG);
				catalogSearchRequest.setPageRequest(new RestPageRequest(0, catalogPageSize));
				RestPageResponse<MLCatalog> catalogs = catalogService.searchCatalogs(catalogSearchRequest);
				List<String> publicCatalogIds = catalogs.getContent().stream().filter(elt -> elt != null)
						.map(elt -> elt.getCatalogId()).collect(Collectors.toList());
				restPageReqPortal.getBody().setCatalogIds(publicCatalogIds.toArray(new String[0]));
			}
			mlSolutions = marketPlaceService.findPortalSolutions(restPageReqPortal.getBody(), null);
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("findPortalSolutions: size is {} ", mlSolutions.getSize());
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "searchSolutionBykeyword", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {"/searchSolutionBykeyword"}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> searchSolutionsByKeyword(
			HttpServletRequest request, @RequestBody JsonRequest<RestPageRequestPortal> restPageReqPortal,
			HttpServletResponse response) {

		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		RestPageResponseBE<MLSolution> mlSolutions = null;
		try {
			mlSolutions = marketPlaceService.searchSolutionsByKeyword(restPageReqPortal.getBody());
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("searchSolutionsByKeyword: size is {} ", mlSolutions.getSize());
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred Fetching Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "findUserSolutions", response = MLSolution.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.USER_SOLUTIONS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponseBE<MLSolution>> findUserSolutions(HttpServletRequest request,
			@RequestBody JsonRequest<RestPageRequestPortal> restPageReqPortal,
			HttpServletResponse response) {
		JsonResponse<RestPageResponseBE<MLSolution>> data = new JsonResponse<>();
		RestPageResponseBE<MLSolution> mlSolutions = null;
		try {
			mlSolutions = marketPlaceService.findUserSolutions(restPageReqPortal.getBody());
			if (mlSolutions != null) {
				data.setResponseBody(mlSolutions);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solutions fetched Successfully");
				log.debug("findUserSolutions: size is {} ", mlSolutions.getSize());
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred Fetching Solutions", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get solutions shared for userId", response = User.class)
	@RequestMapping(value = {APINames.USER_ACCESS_SOLUTIONS}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RestPageResponse<MLPSolution>> getUserAccessSolutions(
			@PathVariable("userId") String userId,
			@RequestBody JsonRequest<RestPageRequest> pageRequest) {

		userId = SanitizeUtils.sanitize(userId);

		RestPageResponse<MLPSolution> mlSolutions = null;
		JsonResponse<RestPageResponse<MLPSolution>> data = new JsonResponse<>();
		if (userId != null && pageRequest != null) {
			mlSolutions = marketPlaceService.getUserAccessSolutions(userId, pageRequest.getBody());
		}
		if (mlSolutions != null) {
			data.setResponseBody(mlSolutions);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("solution for user fetched Successfully");
			log.debug("getUserAccessSolutions :  ", mlSolutions);
		} else {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("Error occured while fetching solutions for user");
			log.error("Error Occurred Fetching solutions for user :" + userId);
		}
		return data;
	}

	@ApiOperation(value = "Get avg ratings for a solution Id", response = MLSolutionWeb.class,
			responseContainer = "List")
	@RequestMapping(value = {APINames.GET_AVG_SOLUTION_RATING}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLSolutionWeb> getAvgRatingsForSol(@PathVariable String solutionId) {
		JsonResponse<MLSolutionWeb> data = new JsonResponse<>();

		solutionId = SanitizeUtils.sanitize(solutionId);

		try {
			MLSolutionWeb mlSolutionWeb = marketPlaceService.getSolutionWebMetadata(solutionId);
			if (mlSolutionWeb != null) {
				data.setResponseBody(mlSolutionWeb);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Solution ratings fetched Successfully");
				log.debug("getAvgRatingsForSol: {} ");
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("No ratings found for solution :" + solutionId);
				log.error("Error Occurred Fetching ratings for model :" + solutionId);
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception Occurred while getAvgRatingsForSol");
			log.error("Exception Occurred while getAvgRatingsForSol", e.getMessage());
		}
		return data;
	}

	/**
	 * @param solutionId Solution ID
	 * @param version    Version
	 * @return Protobuf file details
	 * @throws AcumosServiceException
	 */
	@ApiOperation(value = "Get the profobuf file details for specified solutionID and version")
	@RequestMapping(value = {APINames.GET_PROTO_FILE}, method = RequestMethod.GET,
			produces = "text/plain")
	@ResponseBody
	public String fetchProtoFile(
			@RequestParam(value = "solutionId", required = true) String solutionId,
			@RequestParam(value = "version", required = true) String version)
			throws AcumosServiceException {
		log.debug(" fetchProtoFile() : Begin");

		solutionId = SanitizeUtils.sanitize(solutionId);

		String result = "";
		try {
			result = marketPlaceService.getProtoUrl(solutionId, version, "MI", "proto");

		} catch (Exception e) {
			log.error("Exception in fetchProtoFile() ", e.getMessage());
		}
		log.debug("fetchProtoFile() : End");

		return result;
	}

	/**
	 * @param solutionId Solution ID
	 * @param version    Version
	 * @return License file details
	 * @throws AcumosServiceException
	 */
	@ApiOperation(value = "Get the license file details for specified solutionID and version")
	@RequestMapping(value = {APINames.GET_LICENSE_FILE}, method = RequestMethod.GET,
			produces = "text/plain")
	@ResponseBody
	public String fetchLicenseFile(
			@RequestParam(value = "solutionId", required = true) String solutionId,
			@RequestParam(value = "version", required = true) String version)
			throws AcumosServiceException {
		log.debug(" fetchLicenseFile() : Begin");

		solutionId = SanitizeUtils.sanitize(solutionId);

		String result = "";
		try {
			result = marketPlaceService.getLicenseUrl(solutionId, version, "LI", "license");

		} catch (Exception e) {
			log.error("Exception in fetchLicenseFile() ", e.getMessage());
		}
		log.debug("fetchLicenseFile() : End");

		return result;
	}

	@ApiOperation(value = "Get Cloud Enables or not for that model", response = JsonResponse.class)
	@RequestMapping(value = {APINames.CLOUD_ENABLED_LIST}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<String> getCloudEnabledList(HttpServletRequest request,
			HttpServletResponse response) {

		JsonResponse<String> responseVO = new JsonResponse<String>();
		String cloudEnabled = env.getProperty("portal.feature.cloud_enabled");

		responseVO.setResponseBody(cloudEnabled);
		responseVO.setStatus(true);
		responseVO.setResponseDetail("Success");
		responseVO.setStatusCode(HttpServletResponse.SC_OK);
		return responseVO;
	}

	@ApiOperation(value = "Get Authors of Solution Revision", response = Author.class,
			responseContainer = "List")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/authors"},
			method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<Author>> getAuthors(@PathVariable String solutionId,
			@PathVariable String revisionId, HttpServletResponse response) {
		JsonResponse<List<Author>> data = new JsonResponse<>();

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		try {
			List<Author> authors = marketPlaceService.getSolutionRevisionAuthors(solutionId, revisionId);
			data.setResponseBody(authors);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Author fetched Successfully");
			log.debug("getAuthors: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception Occurred while fetching Authors");
			log.error("Exception Occurred while fetching Authors", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Add Authors of Solution Revision", response = Author.class,
			responseContainer = "List")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/authors"},
			method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<Author>> addAuthors(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@RequestBody JsonRequest<Author> authorReq, HttpServletResponse response) {
		JsonResponse<List<Author>> data = new JsonResponse<>();

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		try {
			List<Author> authors = marketPlaceService.addSolutionRevisionAuthors(solutionId, revisionId,
					authorReq.getBody());
			data.setResponseBody(authors);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Author added Successfully");
			log.debug("addAuthors: {} ");
		} catch (AcumosServiceException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while addAuthors", e.getMessage());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while addAuthors", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Remove Author from Solution Revision", response = Author.class,
			responseContainer = "List")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/removeAuthor"},
			method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<Author>> removeAuthor(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@RequestBody JsonRequest<Author> authorReq, HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		JsonResponse<List<Author>> data = new JsonResponse<>();
		try {
			List<Author> authors = marketPlaceService.removeSolutionRevisionAuthors(solutionId,
					revisionId, authorReq.getBody());
			data.setResponseBody(authors);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Author removed Successfully");
			log.debug("removeAuthor: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while removeAuthor", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get Publisher of Solution Revision", responseContainer = "String")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/publisher"},
			method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<String> getPublisher(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			HttpServletResponse response) {
		JsonResponse<String> data = new JsonResponse<>();

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		try {
			String publisher = marketPlaceService.getSolutionRevisionPublisher(solutionId, revisionId);
			data.setResponseBody(publisher);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Get Publisher Successfully");
			log.debug("addPublisher: {} ");
		} catch (AcumosServiceException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while getPublisher", e.getMessage());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while getPublisher", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Add Publisher of Solution Revision", responseContainer = "String")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/publisher"},
			method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<String> addPublisher(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@RequestBody String publisher, HttpServletResponse response) {
		JsonResponse<String> data = new JsonResponse<>();

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);

		try {
			marketPlaceService.addSolutionRevisionPublisher(solutionId, revisionId, publisher);
			// data.setResponseBody(authors);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Publisher added Successfully");
			log.debug("addPublisher: {} ");
		} catch (AcumosServiceException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while addPublisher", e.getMessage());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while addPublisher", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Add Solution Revision Document", response = MLPDocument.class)
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/{catalogId}/document"},
			method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	@Async(AsyncConfiguration.TASK_EXECUTOR_REPOSITORY)
	public  CompletableFuture<JsonResponse<MLPDocument>> addRevisionDocument(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@PathVariable String catalogId, @RequestParam("file") MultipartFile file,
			HttpServletResponse response) {
		String loggedInUserId = credentialService.getLoggedInUserId();
		final String  sanitizedSolutionId = SanitizeUtils.sanitize(solutionId);
		final String  sanitizedRevisionId = SanitizeUtils.sanitize(revisionId);
		final String  sanitizedCatalogId = SanitizeUtils.sanitize(catalogId);
		String userId = (String) request.getAttribute("loginUserId");
		return CompletableFuture.supplyAsync(() -> {

		JsonResponse<MLPDocument> data = new JsonResponse<>();
		try {
			Workflow workflow = performSVScan(sanitizedSolutionId, sanitizedRevisionId, SVConstants.UPDATED, loggedInUserId).get();
			if (workflow.isWorkflowAllowed()) {
				double maxFileSizeByKB = Double.valueOf(env.getProperty("document.size").toString());
				long fileSizeByKB = file.getBytes().length;
				if (fileSizeByKB <= maxFileSizeByKB) {
					MLPDocument document = marketPlaceService.addRevisionDocument(sanitizedSolutionId, sanitizedRevisionId,
						sanitizedCatalogId, userId, file);
					data.setResponseBody(document);
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					data.setResponseDetail("Document Added Successfully");
					log.debug("addDocument: {} ");
				} else {
					MLPDocument document = null;
					data.setResponseBody(document);
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
					data.setResponseDetail("Document size is greater than allowed size");
					log.debug("addDocument: {} ");
				}
			} else {
				data.setErrorCode(
						(isReasonInfo(workflow.getReason())) ? JSONTags.TAG_INFO_SV : JSONTags.TAG_ERROR_SV);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				data.setResponseDetail(workflow.getReason());
				log.error("SV failure while adding document : " + workflow.getReason());
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while adding Document", e.getMessage());
		}
		return data;
	});
	}

	@ApiOperation(value = "Remove Solution Revision Document", response = MLPDocument.class)
	@RequestMapping(
			value = {"/solution/{solutionId}/revision/{revisionId}/{catalogId}/document/{documentId}"},
			method = RequestMethod.DELETE, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPDocument> removeRevisionDocument(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@PathVariable String catalogId, @PathVariable String documentId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);
		catalogId = SanitizeUtils.sanitize(catalogId);
		documentId = SanitizeUtils.sanitize(documentId);

		JsonResponse<MLPDocument> data = new JsonResponse<>();
		String userId = (String) request.getAttribute("loginUserId");
		try {
			MLPDocument document = marketPlaceService.removeRevisionDocument(solutionId, revisionId,
					catalogId, userId, documentId);
			data.setResponseBody(document);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Document Removed Successfully");
			log.debug("removeDocument: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while removing Document", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get Solution Revision Documents", response = MLPDocument.class,
			responseContainer = "List")
	@RequestMapping(value = {"/solution/{solutionId}/revision/{revisionId}/{catalogId}/document"},
			method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLPDocument>> getRevisionDocument(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@PathVariable String catalogId, HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);
		catalogId = SanitizeUtils.sanitize(catalogId);

		JsonResponse<List<MLPDocument>> data = new JsonResponse<>();
		String userId = (String) request.getAttribute("loginUserId");
		try {
			List<MLPDocument> documents =
					marketPlaceService.getRevisionDocument(solutionId, revisionId, catalogId, userId);
			data.setResponseBody(documents);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Documents Fetched Successfully");
			log.debug("removeDocument: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while fetching Documents", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Copy Solution Revision Documents", response = MLPDocument.class,
			responseContainer = "List")
	@RequestMapping(value = {
			"/solution/{solutionId}/revision/{revisionId}/{catalogId}/copyDocuments/{fromRevisionId}"},
			method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLPDocument>> copyRevisionDocuments(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@PathVariable String catalogId, @PathVariable String fromRevisionId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);
		catalogId = SanitizeUtils.sanitize(catalogId);
		fromRevisionId = SanitizeUtils.sanitize(fromRevisionId);

		JsonResponse<List<MLPDocument>> data = new JsonResponse<>();
		String userId = (String) request.getAttribute("loginUserId");
		try {
			List<MLPDocument> documents = marketPlaceService.copyRevisionDocuments(solutionId, revisionId,
					catalogId, userId, fromRevisionId);
			data.setResponseBody(documents);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Documents Fetched Successfully");
			log.debug("removeDocument: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while fetching Documents", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get Solution Revision Description", response = RevisionDescription.class)
	@RequestMapping(value = {"/solution/revision/{revisionId}/{catalogId}/description"},
			method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RevisionDescription> getSolRevDescription(HttpServletRequest request,
			@PathVariable String revisionId, @PathVariable String catalogId,
			HttpServletResponse response) {

		revisionId = SanitizeUtils.sanitize(revisionId);
		catalogId = SanitizeUtils.sanitize(catalogId);

		JsonResponse<RevisionDescription> data = new JsonResponse<>();
		String userId = (String) request.getAttribute("loginUserId");
		try {
			RevisionDescription description =
					marketPlaceService.getRevisionDescription(revisionId, catalogId);
			data.setResponseBody(description);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Description Fetched Successfully");
			log.debug("removeDocument: {} ");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while fetching Description", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Add/Update Solution Revision Description",
			response = RevisionDescription.class)
	@RequestMapping(value = {"/solution/revision/{solutionId}/{revisionId}/{catalogId}/description"},
			method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<RevisionDescription> addSolRevDescription(HttpServletRequest request,
			@PathVariable String solutionId, @PathVariable String revisionId,
			@PathVariable String catalogId,
			@RequestBody JsonRequest<RevisionDescription> revisionDescription,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);
		revisionId = SanitizeUtils.sanitize(revisionId);
		catalogId = SanitizeUtils.sanitize(catalogId);
		String loggedInUserId = credentialService.getLoggedInUserId();

		JsonResponse<RevisionDescription> data = new JsonResponse<>();
		// String userId = (String) request.getAttribute("loginUserId");
		RevisionDescription description = revisionDescription.getBody();
		try {
			Workflow workflow = performSVScan(solutionId, revisionId, SVConstants.UPDATED, loggedInUserId).get();

			if (workflow.isWorkflowAllowed()) {

				description =
						marketPlaceService.addUpdateRevisionDescription(revisionId, catalogId, description);
				data.setResponseBody(description);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Description Fetched Successfully");
				log.debug("removeDocument: {} ");
			} else {
				data.setErrorCode(
						(isReasonInfo(workflow.getReason())) ? JSONTags.TAG_INFO_SV : JSONTags.TAG_ERROR_SV);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				data.setResponseDetail(workflow.getReason());
				log.error("SV failure while adding document : " + workflow.getReason());
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail(e.getMessage());
			log.error("Exception Occurred while fetching Description", e.getMessage());
		}
		return data;
	}


	@ApiOperation(value = "Fetches Solution Image. ")
	@RequestMapping(value = {APINames.SOLUTIONS_PICTURE}, method = RequestMethod.GET,
			produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> getSolutionImage(@PathVariable("solutionId") String solutionId,
			@RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		log.debug("getSolutionImage={}", solutionId);
		ResponseEntity<byte[]> responseVO = null;
		try {
			if (PortalUtils.isEmptyOrNullString(solutionId)) {
				String errMsg = "Bad request: solutionId empty";
				log.error(errMsg);
				throw new AcumosServiceException(errMsg);
			} else {
				Instant lastModified = marketPlaceService.getSolution(solutionId).getModified();
				if (!PortalUtils.isEmptyOrNullString(ifModifiedSince)
						&& Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince))
								.equals(lastModified)) {
					responseVO = ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
				} else {
					byte[] picture = marketPlaceService.getSolutionPicture(solutionId);
					if (picture != null) {
						responseVO = ResponseEntity.ok()
								.cacheControl(CacheControl.maxAge(8, TimeUnit.HOURS).cachePublic())
								.lastModified(lastModified.toEpochMilli()).body(picture);
					} else {
						responseVO = ResponseEntity.notFound().build();
					}
				}
			}
		} catch (Exception e) {
			responseVO = ResponseEntity.badRequest().build();
			log.error("Exception Occurred while getSolutionImage()", e.getMessage());
		}
		return responseVO;
	}

	@ApiOperation(value = "Updates Solution Image. ")
	@RequestMapping(value = {APINames.SOLUTIONS_PICTURE}, method = RequestMethod.POST,
			produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<Boolean> updateSolutionImage(HttpServletRequest request,
			@RequestParam("file") MultipartFile file, @PathVariable("solutionId") String solutionId,
			HttpServletResponse response) {

		solutionId = SanitizeUtils.sanitize(solutionId);

		log.debug("updateSolutionImage={}");
		JsonResponse<Boolean> responseVO = new JsonResponse<>();
		try {
			if (PortalUtils.isEmptyOrNullString(solutionId)) {
				log.error("Bad request: solutionId empty");
			}
			if (solutionId != null) {
				marketPlaceService.updateSolutionPicture(solutionId, file.getBytes());
			}
			responseVO.setStatus(true);
			responseVO.setResponseDetail("Success");
			responseVO.setResponseBody(true);
			responseVO.setStatusCode(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			responseVO.setStatus(false);
			responseVO.setResponseDetail("Failed");
			responseVO.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
			log.error("Exception Occurred while updateSolutionImage()", e.getMessage());
		}
		return responseVO;
	}

	@Async(AsyncConfiguration.TASK_EXECUTOR_REPOSITORY)
	@ApiOperation(
			value = "Perform SV Scan for given solution ID and revision ID, intended for pre-deployment use",
			response = Workflow.class)
	@RequestMapping(value = {APINames.SOLUTIONS_REVISIONS_VERIFY}, method = RequestMethod.GET,
			produces = APPLICATION_JSON)
	@ResponseBody
	public CompletableFuture<JsonResponse<VerifySolutionActionResponse>> verifySolutionRevision(
			HttpServletRequest request, @PathVariable("solutionId") String solutionId,
			@PathVariable("revisionId") String revisionId, @PathVariable("workflowId") String workflowId,
			HttpServletResponse response) {

		final String sanitizedSolutionId = SanitizeUtils.sanitize(solutionId);
		final String sanitizedRevisionId = SanitizeUtils.sanitize(revisionId);
		final String sanitizedWorkflowId = SanitizeUtils.sanitize(workflowId);
		String loggedInUserName  = credentialService.getLoggedInUserName();
		String loggedInUserId = credentialService.getLoggedInUserId();
		return CompletableFuture.supplyAsync(() -> {

			JsonResponse<VerifySolutionActionResponse> data = new JsonResponse<>();

			CompletableFuture<Workflow> workflowFuture =
					performSVScan(sanitizedSolutionId, sanitizedRevisionId, sanitizedWorkflowId, loggedInUserId);

			LicenseRtuVerification rtu = null;
			VerifySolutionActionResponse rtuVerifcation = handleRtuCheckResponse(rtu, data);
			// rtu is allowed check for sv scan now
			if (rtuVerifcation.isWorkflowAllowed()) {
				Workflow workflow = null;
				try {
					workflow = workflowFuture.get();
				} catch (InterruptedException | ExecutionException e) {
					log.error("system error during SV scan check", e.getMessage());
				}

				handleSvResponse(response, data, workflow);
			}
			return data;
		});

	}

	private void handleSvResponse(HttpServletResponse response,
			JsonResponse<VerifySolutionActionResponse> data, Workflow workflow) {
		if (workflow != null) {
			VerifySolutionActionResponse resp = new VerifySolutionActionResponse();
			resp.setReason(workflow.getReason());
			resp.setException(workflow.getSvException());
			resp.setWorkflowAllowed(workflow.isWorkflowAllowed());

			response.setStatus(HttpServletResponse.SC_OK);
			data.setResponseBody(resp);
			data.setErrorCode((workflow.isWorkflowAllowed()) ? JSONTags.TAG_ERROR_CODE_SUCCESS
					: (isReasonInfo(workflow.getReason())) ? JSONTags.TAG_INFO_SV : JSONTags.TAG_ERROR_SV);
			data.setResponseDetail("SV Scan completed");
			log.debug("SV Scan completed :  ", workflow);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("SV Scan failed");
			log.debug("SV Scan failed, workflow null");
		}
	}

	private VerifySolutionActionResponse handleRtuCheckResponse(LicenseRtuVerification rtu,
																JsonResponse<VerifySolutionActionResponse> data) {
		VerifySolutionActionResponse resp = new VerifySolutionActionResponse();
		resp.setWorkflowAllowed(true);
		data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
		data.setResponseBody(resp);
		return resp;
	}
}
