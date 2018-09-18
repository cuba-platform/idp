/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.addon.idp.web.security.idp;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;

/**
 * Configuration parameters interface used by IDP SSO login provider.
 */
@Source(type = SourceType.APP)
public interface WebIdpConfig extends Config {

    /**
     * @return whether IDP authentication for Web Client is enabled or not
     */
    @Property("cuba.web.idp.enabled")
    @DefaultBoolean(false)
    boolean getIdpEnabled();

    /**
     * @return Base URL of IDP server, e.g. http://localhost:8080/app/idp.
     */
    @Property("cuba.web.idp.baseUrl")
    String getIdpBaseURL();

    /**
     * @return Trusted password to access to IDP server.
     */
    @Property("cuba.web.idp.trustedServicePassword")
    String getIdpTrustedServicePassword();
}