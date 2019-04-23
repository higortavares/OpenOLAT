/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.doceditor.onlyoffice;

import java.security.Key;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.security.Keys;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String ONLYOFFICE_ENABLED = "onlyoffice.enabled";
	private static final String ONLYOFFICE_API_URL = "onlyoffice.apiUrl";
	private static final String ONLYOFFICE_JWT_SECRET = "onlyoffice.jwt.secret";
	
	@Value("${onlyoffice.enabled:false}")
	private boolean enabled;
	@Value("${onlyoffice.apiUrl}")
	private String apiUrl;
	private String jwtSecret;
	private Key jwtSignKey;
	
	@Autowired
	private OnlyOfficeModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(ONLYOFFICE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String apiUrlObj = getStringPropertyValue(ONLYOFFICE_API_URL, true);
		if(StringHelper.containsNonWhitespace(apiUrlObj)) {
			apiUrl = apiUrlObj;
		}
		
		String jwtSecretObj = getStringPropertyValue(ONLYOFFICE_JWT_SECRET, true);
		if(StringHelper.containsNonWhitespace(jwtSecretObj)) {
			jwtSecret = jwtSecretObj;
			jwtSignKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(ONLYOFFICE_ENABLED, Boolean.toString(enabled), true);
	}
	
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
		setStringProperty(ONLYOFFICE_API_URL, apiUrl, true);
	}

	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
		this.jwtSignKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		setStringProperty(ONLYOFFICE_JWT_SECRET, jwtSecret, true);
	}

	public Key getJwtSignKey() {
		return jwtSignKey;
	}

}
