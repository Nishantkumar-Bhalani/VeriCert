/****************************************************** 
 *  Copyright 2018 IBM Corporation 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */       
package com.trustcert.blockchain.client;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.trustcert.blockchain.user.UserContext;
import com.trustcert.blockchain.util.Util;
import com.trustcert.utility.UserRolesEnum;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;


public class CAClient {

	String caUrl;
	Properties caProperties;

	HFCAClient instance;

	UserContext adminContext;

	public UserContext getAdminUserContext() {
		return adminContext;
	}

	/**
	 * Set the admin user context for registering and enrolling users.
	 * 
	 * @param userContext
	 */
	public void setAdminUserContext(UserContext userContext) {
		this.adminContext = userContext;
	}

	/**
	 * Constructor
	 * 
	 * @param caUrl 
	 * @param caProperties
	 * @throws MalformedURLException
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws InvalidArgumentException 
	 * @throws CryptoException 
	 * @throws ClassNotFoundException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 */
	public CAClient(String caUrl, Properties caProperties) throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		this.caUrl = caUrl;
		this.caProperties = caProperties;
		init();
	}

	public void init() throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException {
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		instance = HFCAClient.createNewInstance(caUrl, caProperties);
		instance.setCryptoSuite(cryptoSuite);
	}

	public HFCAClient getInstance() {
		return instance;
	}

	/**
	 * Enroll admin user.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public UserContext enrollAdminUser(String username, String password) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			Logger.getLogger(CAClient.class.getName()).log(Level.WARNING, "CA -" + caUrl + " admin is already enrolled.");
			return userContext;
		}
		Enrollment adminEnrollment = instance.enroll(username, password);
		adminContext.setEnrollment(adminEnrollment);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl + " Enrolled Admin.");
		Util.writeUserContext(adminContext);
		return adminContext;
	}

	/**
	 * Register user.
	 * 
	 * @param username
	 * @param organization
	 * @return
	 * @throws Exception
	 */
	public String registerUser(String username, String organization) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), username);
		if (userContext != null) {
			Logger.getLogger(CAClient.class.getName()).log(Level.WARNING, "CA -" + caUrl +" User " + username+ " is already registered.");
			return null;
		}
		RegistrationRequest rr = new RegistrationRequest(username, organization);
		String enrollmentSecret = instance.register(rr, adminContext);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl + " Registered User - " + username);
		return enrollmentSecret;
	}

	/**
	 * Register user with attributes.
	 *
	 * @param primaryEmailId
	 * @param organization
	 * @param role
	 * @return enrollmentSecret
	 * @throws Exception
	 */
	public String registerUserCAClientWrapper(String primaryEmailId, String organization, UserRolesEnum role) throws Exception {
		UserContext userContext = Util.readUserContext(adminContext.getAffiliation(), primaryEmailId);
		if (userContext != null) {
			Logger.getLogger(CAClient.class.getName()).log(Level.WARNING, "CA -" + caUrl +" User " + primaryEmailId+ " is already registered.");
			return null;
		}

		RegistrationRequest rr = new RegistrationRequest(primaryEmailId, organization);
		Attribute emailAttribute = new Attribute("emailId",primaryEmailId,Boolean.TRUE);
		Attribute roleAttribute = new Attribute("role",role.toString(),Boolean.TRUE);

		rr.addAttribute(emailAttribute);
		rr.addAttribute(roleAttribute);

		String enrollmentSecret = instance.register(rr, adminContext);
		Logger.getLogger(CAClient.class.getName()).log(Level.INFO, "CA -" + caUrl + " Registered User - " + primaryEmailId);
		return enrollmentSecret;
	}


}
