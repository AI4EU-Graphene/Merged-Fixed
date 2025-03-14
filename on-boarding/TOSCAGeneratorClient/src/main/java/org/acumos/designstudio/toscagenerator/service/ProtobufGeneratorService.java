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

package org.acumos.designstudio.toscagenerator.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.acumos.designstudio.toscagenerator.exceptionhandler.ServiceException;
import org.acumos.designstudio.toscagenerator.util.Properties;
import org.acumos.designstudio.toscagenerator.vo.protobuf.ComplexType;
import org.acumos.designstudio.toscagenerator.vo.protobuf.InputMessage;
import org.acumos.designstudio.toscagenerator.vo.protobuf.MessageBody;
import org.acumos.designstudio.toscagenerator.vo.protobuf.MessageargumentList;
import org.acumos.designstudio.toscagenerator.vo.protobuf.Operation;
import org.acumos.designstudio.toscagenerator.vo.protobuf.Option;
import org.acumos.designstudio.toscagenerator.vo.protobuf.OutputMessage;
import org.acumos.designstudio.toscagenerator.vo.protobuf.ProtoBufClass;
import org.acumos.designstudio.toscagenerator.vo.protobuf.Service;
import org.acumos.designstudio.toscagenerator.vo.protobuf.SortComparator;
import org.acumos.designstudio.toscagenerator.vo.protobuf.SortFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

public class ProtobufGeneratorService {

	boolean isMessage = false;
	boolean isItservice = false;
	boolean isItMessage = false;
	int servicesLineCount = 0;

	private static final Logger logger = LoggerFactory.getLogger(ProtobufGeneratorService.class);
	final Pattern pattern = Pattern.compile("\\((.*?)\\)");
	private List<MessageargumentList> messageargumentList = null;
	private List<InputMessage> listOfInputMessages = null;
	private List<OutputMessage> listOfOputPutMessages = null;
	private List<Operation> listOfOperation = null;

	private ProtoBufClass protoBufClass = null;

	private MessageBody messageBody = null;
	private List<MessageBody> messageBodyList = null;
	private org.acumos.designstudio.toscagenerator.vo.protobuf.Service service = null;
	private Operation operation = null;
	private List<String> listOfInputAndOutputMessage = null;
	private List<Option> listOfOption = null;


	/**
	 * This method create the json format of .proto file.
	 * @param solutionId
	 *            solution ID
	 * @param version
	 *            version
	 * @param localMetadataFile
	 *            metadata file
	 * @return Protobuf JSON
	 *             string of protobuf json
	 * @throws ServiceException
	 *             On failure
	 */
	public String createProtoJson(String solutionId, String version, File localMetadataFile) throws ServiceException {
		logger.debug("CreateProtoJson() started");
		protoBufClass = new ProtoBufClass();
		messageBodyList = new ArrayList<MessageBody>();
		listOfInputAndOutputMessage = new ArrayList<String>();
		listOfOption = new ArrayList<Option>();
		String protoBufToJsonString = "";
		try {
			parseFile(localMetadataFile);
			// do not add utility interfaces to utility nodes
			if(!service.getName().equalsIgnoreCase("SharedFolderProvider")) {
				logger.info("add shared-folder.proto");
				parseResource("shared-folder.proto");
			}
			Gson gson = new Gson();
			protoBufToJsonString = gson.toJson(protoBufClass);
			try {
				List<MessageBody> expendedmessageBodyList = constructListOfMessages(protoBufToJsonString);
				protoBufClass.setListOfMessages(expendedmessageBodyList);
				Gson gson1 = new Gson();
				protoBufToJsonString = gson1.toJson(protoBufClass);
				logger.info("protoBufToJsonString: "+protoBufToJsonString);
			} catch (Exception ex) {
				logger.error("Exception Occured  constructListOfMessages()", ex);
				ex.printStackTrace();
				throw ex;
			}
			isMessage = false;
			isItservice = false;
			isItMessage = false;
			servicesLineCount = 0;
			logger.debug("CreateProtoJson() end");
		} catch (ServiceException ex) {
			logger.error(
					"Exception Occured  CreateProtoJson() when Reading the protobuf file and generating protobuf json",
					ex);
			throw ex;
		} catch (Exception ex) {
			logger.error(
					"Exception Occured  CreateProtoJson() when Reading the protobuf file and generating protobuf json",
					ex);
			throw new ServiceException(ex.toString(),
				ServiceException.TOSCA_FILE_GENERATION_ERROR_CODE, ServiceException.TOSCA_FILE_GENERATION_ERROR_DESC, ex);
		}
		return protoBufToJsonString;
	}

	private void parseFile(File protoFile) throws Exception {
		try(BufferedReader br=new BufferedReader(new FileReader(protoFile))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				parseLine(sCurrentLine.replace("\t", ""));
			}
		}
	}

	private void parseResource(String resource) throws Exception {
		try(BufferedReader br=new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource)))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				parseLine(sCurrentLine.replace("\t", ""));
			}
		}
	}

	private void parseLine(String line) throws ServiceException {
		try {
			// construct syntax
			if (line.contains("syntax")) {
				protoBufClass.setSyntax(constructSyntax(line));
			}
			// check for package and imports
			if (line.trim().startsWith("package") || line.trim().startsWith("import") ) {
				String message="package or import declaration not allowed in proto-files: "+line;
				logger.error(message);
				throw new ServiceException(message, Properties.getDecryptionErrorCode(), message);
			}
			if (Properties.isOptionKeywordRequirede().equals("true")) {
				if (line.startsWith("option")) {
					constructOption(line);
					protoBufClass.setListOfOption(listOfOption);
				}
			}
			// start package

			if (line.startsWith("message")) {
				logger.debug("costructMessage() strated");

				if (isMessage == true) {
					String message = "encountered message line while in message: "+line;
					logger.error(message);
					throw new ServiceException(message, Properties.getDecryptionErrorCode(), message);
				}

				messageBody = new MessageBody();
				messageBody = costructMessage(line, messageBody);

			} else if (isMessage && !line.contains("}")) {
				messageBody = costructMessage(line, messageBody);
			} else if (isMessage && line.contains("}")) {
				messageBodyList.add(messageBody);
				protoBufClass.setListOfMessages(messageBodyList);
				isMessage = false;
				logger.debug("costructMessage() end");
			}

			if (line.startsWith("service")) {
				// allow multiple service blocks
				if(service==null) {
					service = new org.acumos.designstudio.toscagenerator.vo.protobuf.Service();
				}
				service = constructService(line, service);

			} else if (isItservice && !line.contains("}") && !line.isEmpty() && !line.trim().startsWith("//")) {
				operation = new Operation();
				listOfOperation = new ArrayList<Operation>();
				// e.g. rpc evaluateSudokuDesign(SudokuDesignEvaluationJob) returns (SudokuDesignEvaluationResult);
				line = line.replace(";", "").replace("\t", "").trim();
				String operationType = "";
				String operationName = "";
				String inputParameterString = "";
				String outPutParameterString = "";

				// line1 is everything before returns
				// e.g. rpc evaluateSudokuDesign(SudokuDesignEvaluationJob)
				String line1 = line.split("returns")[0].trim();
				operationType = line1.split(" ", 2)[0].trim();
				// line2 is operation name and input parameter without closing bracket
				// e.g. evaluateSudokuDesign(SudokuDesignEvaluationJob
				String line2 = line1.split(" ", 2)[1].replace(")", "").trim();
				// e.g. evaluateSudokuDesign
				operationName = line2.split("\\(")[0].trim();
				// e.g. SudokuDesignEvaluationJob
				inputParameterString = line2.split("\\(")[1].trim();

				outPutParameterString = line.split("returns")[1].replace("(", "").replace(")", "").trim();
				operation.setOperationType(operationType);
				operation.setOperationName(operationName);
				listOfInputMessages = constructInputMessage(inputParameterString);
				listOfOputPutMessages = constructOutputMessage(outPutParameterString);

				operation.setListOfInputMessages(listOfInputMessages);
				operation.setListOfOutputMessages(listOfOputPutMessages);
				if (service.getListOfOperations() != null && !service.getListOfOperations().isEmpty()) {
					service.getListOfOperations().add(operation);
				} else {
					listOfOperation.add(operation);
					service.setListOfOperations(listOfOperation);
					protoBufClass.setService(service);
				}
			} else if (isItservice && line.contains("}") && !line.isEmpty()) {
				isItservice = false;
				logger.debug("constructService() end");
			}
		} catch (Exception ex) {
			logger.error("Exception Occured  parseLine()", ex);
			throw new ServiceException("Exception Occured while parsing protobuf: "+ex.getMessage(),
					Properties.getDecryptionErrorCode(), ex.getMessage(), ex.getCause());
		}

	}

	private String constructSyntax(String line) {
		logger.debug("constructSyntax() strated");
		String removequotes = "";
		try {
			if (line.startsWith("syntax")) {
				String[] fields = line.split("=");
				String removeSemicolon = fields[1].replace(";", "");
				String trimString = removeSemicolon.trim();
				removequotes = trimString.replaceAll("^\"|\"$", "");
			}
		} catch (Exception ex) {
			logger.error("Exception Occured  constructSyntax()", ex);
			throw ex;
		}
		logger.debug("constructSyntax() end");
		return removequotes;

	}

	private String constructPackage(String line) {
		logger.debug("constructPackage() strated");
		String[] fields = line.split(" ");
		logger.debug("constructPackage() end");
		return fields[1].replace(";", "");
	}

	private String constructOption(String line) {
		Option optionInstance = new Option();
		StringTokenizer st = new StringTokenizer(line, " ");
		String removequotes = "";
		while (st.hasMoreElements()) {
			st.nextElement();
			optionInstance.setKey(st.nextElement().toString());
			st.nextElement();
			String removeSemicolon = st.nextElement().toString().replace(";", "");
			String trimString = removeSemicolon.trim();
			removequotes = trimString.replaceAll("^\"|\"$", "");
			optionInstance.setValue(removequotes);
		}
		listOfOption.add(optionInstance);
		return "";
	}

	private MessageBody costructMessage(String line, MessageBody messageBody) throws ServiceException {
		try {
			if (line.startsWith("message")) {
				if (!line.trim().endsWith("{")) {
					String message="this protobuf parser can only start messages with 'message <name> {', please continue the message in the next line. problematic input line: '"+line+"'";
					logger.error(message);
					throw new ServiceException(message, Properties.getDecryptionErrorCode(), message);
				}
				String[] fields = line.split(" ");

				// if { comes directly after message name, remove {
				if (fields[1].endsWith("{")) {
					fields[1] = fields[1].substring(0, fields[1].length()-1);
				}

				messageBody.setMessageName(fields[1]);
			}
			if (isMessage) {

				if (line.trim().endsWith(";") || line.contains(";")) {
					String nonDetLine = line.trim().split(";")[0].replaceAll("(\\s+)", " ");
					StringTokenizer st = new StringTokenizer(nonDetLine, " ");
					String[] fields = nonDetLine.split(" ");
					MessageargumentList messageargumentListInstance = new MessageargumentList();

					for (int i = 0; i < fields.length; i++) {
						if (!fields[i].isEmpty()) {
							if (st.countTokens() == 5) {// count total number of token in line.
								messageargumentListInstance.setRole(fields[i]);
								messageargumentListInstance.setType(fields[i + 1]);
								messageargumentListInstance.setName(fields[i + 2]);
								messageargumentListInstance.setTag(fields[i + 4].replace(";", ""));
							} else {
								messageargumentListInstance.setRole("");
								messageargumentListInstance.setType(fields[i]);
								messageargumentListInstance.setName(fields[i + 1]);
								messageargumentListInstance.setTag(fields[i + 3].replace(";", ""));
							}
							break;
						}
					}
					SortComparator sortComparator = SortFactory.getComparator();
					messageBody.getMessageargumentList().add(messageargumentListInstance);
					Collections.sort(messageBody.getMessageargumentList(), sortComparator);
				}
			}
			if (line.contains("}")) {
				isMessage = false;
			} else {
				isMessage = true;
			}
		} catch (Exception ex) {
			logger.error("Exception Occured  costructMessage()", ex);
			throw ex;
		}
		return messageBody;
	}

	private org.acumos.designstudio.toscagenerator.vo.protobuf.Service constructService(String line,
			org.acumos.designstudio.toscagenerator.vo.protobuf.Service service) {
		try {
			if(service.getName().isEmpty()) {
				// we use only the name of the first service block
				String[] fields = line.split(" ");
				service.setName(fields[1]);
			}
			if (line.contains("}")) {
				isItservice = false;
			} else {
				isItservice = true;
			}
		} catch (Exception ex) {
			logger.error("Exception Occured  constructService()", ex);
			throw ex;
		}
		return service;

	}

	private List<InputMessage> constructInputMessage(String inputParameterString) throws ServiceException {
		logger.debug("constructInputMessage() strated");
		listOfInputMessages = new ArrayList<InputMessage>();

		String[] inPutParameterArray = inputParameterString.split(",");
		if (inPutParameterArray.length != 1) {
			throw new ServiceException("operation needs exactly one input parameter, found '"+inputParameterString+"'", ServiceException.TOSCA_FILE_GENERATION_ERROR_CODE, ServiceException.TOSCA_FILE_GENERATION_ERROR_DESC);
		}
		for (int i = 0; i < inPutParameterArray.length; i++) {
			InputMessage inputMessage = new InputMessage();

			// handle stream input
			String[] parts = inPutParameterArray[i].split("[  \\t]+");
			if (parts.length == 1) {
				inputMessage.setStream(false);
				inputMessage.setInputMessageName(parts[0]);
			} else {
				if (parts.length == 2 && parts[0].equals("stream")) {
					inputMessage.setStream(true);
					inputMessage.setInputMessageName(parts[1]);
				} else {
					throw new ServiceException("service operation has invalid input parameter '"+inPutParameterArray[i]+"' parts="+parts.length+" parts[0]="+parts[0], ServiceException.TOSCA_FILE_GENERATION_ERROR_CODE, ServiceException.TOSCA_FILE_GENERATION_ERROR_DESC);
				}
			}

			listOfInputMessages.add(inputMessage);
			listOfInputAndOutputMessage.add(inputMessage.getInputMessageName());
		}

		logger.debug("constructInputMessage() end");
		return listOfInputMessages;

	}

	private List<OutputMessage> constructOutputMessage(String outPutParameterString) throws ServiceException {
		logger.debug("constructOutputMessage() strated");
		listOfOputPutMessages = new ArrayList<OutputMessage>();
		String[] outPutParameterArray = outPutParameterString.split(",");
		if (outPutParameterArray.length != 1) {
			throw new ServiceException("operation needs exactly one output parameter, found '"+outPutParameterString+"'", ServiceException.TOSCA_FILE_GENERATION_ERROR_CODE, ServiceException.TOSCA_FILE_GENERATION_ERROR_DESC);
		}
		for (int i = 0; i < outPutParameterArray.length; i++) {
			OutputMessage outputMessage = new OutputMessage();

			// handle stream output
			String[] parts = outPutParameterArray[i].split("[  \\t]+");
			if (parts.length == 1) {
				outputMessage.setStream(false);
				outputMessage.setOutPutMessageName(parts[0]);
			} else {
				if (parts.length == 2 && parts[0].equals("stream")) {
					outputMessage.setStream(true);
					outputMessage.setOutPutMessageName(parts[1]);
				} else {
					throw new ServiceException("service operation has invalid output parameter '"+outPutParameterArray[i]+"' parts="+parts.length+" parts[0]="+parts[0], ServiceException.TOSCA_FILE_GENERATION_ERROR_CODE, ServiceException.TOSCA_FILE_GENERATION_ERROR_DESC);
				}
			}

			listOfOputPutMessages.add(outputMessage);
			listOfInputAndOutputMessage.add(outputMessage.getOutPutMessageName());
		}
		logger.debug("constructOutputMessage() end");
		return listOfOputPutMessages;
	}

	private List<MessageBody> constructListOfMessages(String protoBufToJsonString) throws Exception {
		List<MessageBody> listOfMessages = new ArrayList<MessageBody>();
		Set<String> setOfMessageNames = new HashSet<String>();
		MessageBody messageBody = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		protoBufClass = mapper.readValue(protoBufToJsonString, ProtoBufClass.class);

		Service service = protoBufClass.getService();
		List<Operation> listOfOperations = new ArrayList<Operation>();
		if (service != null) {
			listOfOperations = service.getListOfOperations();
		}
		List<InputMessage> inputMessages = null;
		String inputMessageName = null;

		List<OutputMessage> outputMessages = null;
		String outputMessageName = null;
		for (Operation operation : listOfOperations) {
			inputMessages = operation.getListOfInputMessages();
			for (InputMessage inputmsg : inputMessages) {
				inputMessageName = inputmsg.getInputMessageName();
				if (!setOfMessageNames.contains(inputMessageName)) {
					// expanded
					messageBody = constructExpandedMessageBody(inputMessageName, protoBufClass.getListOfMessages(), "");
					if (null != messageBody) {
						listOfMessages.add(messageBody);
						setOfMessageNames.add(inputMessageName);
					}
				}
			}

			outputMessages = operation.getListOfOutputMessages();
			for (OutputMessage outputmsg : outputMessages) {
				outputMessageName = outputmsg.getOutPutMessageName();
				if (!setOfMessageNames.contains(outputMessageName)) {
					messageBody = constructExpandedMessageBody(outputMessageName, protoBufClass.getListOfMessages(), "");
					if (null != messageBody) {
						listOfMessages.add(messageBody);
						setOfMessageNames.add(outputMessageName);
					}
				}
			}
		}

		return listOfMessages;
	}

	private MessageBody constructExpandedMessageBody(String messageName, List<MessageBody> listOfMessages, String tag) {
		MessageBody result = null;

		String basicProtobufTypes = Properties.getProtobufBasicType();
		String[] basicProtobufTypesArray = basicProtobufTypes.split(",");
		List<String> basicProtobufTypesList = Arrays.asList(basicProtobufTypesArray);

		String parentTag = "";
		if (null != tag && !tag.trim().equals("")) {
			parentTag = tag + ".";
		}
		parentTag = parentTag.trim();
		// get MessageBody for messageName
		MessageBody sourceMessage = null;
		for (MessageBody msgBody : listOfMessages) {
			if (msgBody.getMessageName().equals(messageName)) {
				sourceMessage = msgBody;
				break;
			}
		}
		if (null != sourceMessage && sourceMessage.getMessageargumentList() != null) {
			result = new MessageBody();
			result.setMessageName(messageName);
			List<MessageargumentList> messageargumentList = new ArrayList<MessageargumentList>();
			MessageargumentList msgArgument = null;
			String sourceMsgArgumentType = null;
			List<MessageargumentList> sourceMessageargumentList = sourceMessage.getMessageargumentList();
			ComplexType complexType = null;

			for (MessageargumentList sourceMsgArgument : sourceMessageargumentList) {
				sourceMsgArgumentType = sourceMsgArgument.getType();
				msgArgument = new MessageargumentList();
				msgArgument.setRole(sourceMsgArgument.getRole());
				msgArgument.setType(sourceMsgArgumentType);
				msgArgument.setName(sourceMsgArgument.getName());
				msgArgument.setTag(parentTag + sourceMsgArgument.getTag());
				// check source message argument is not basic type then
				if (!basicProtobufTypesList.contains(sourceMsgArgumentType)) {
					complexType = new ComplexType();
					MessageBody complexBody = constructExpandedMessageBody(sourceMsgArgumentType, listOfMessages,
							parentTag + sourceMsgArgument.getTag());
					if (null != complexBody) {
						complexType.setMessageName(complexBody.getMessageName());
						complexType.setMessageargumentList(complexBody.getMessageargumentList());
					}
					msgArgument.setComplexType(complexType);
				}
				messageargumentList.add(msgArgument);
			}
			result.setMessageargumentList(messageargumentList);
		}
		return result;
	}
}
