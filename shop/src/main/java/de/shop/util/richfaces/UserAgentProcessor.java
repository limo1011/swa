/*
 * https://raw.github.com/richfaces/showcase/develop/src/main/java/org/richfaces/demo/ui/UserAgentProcessor.java
 */

/**
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 **/
package de.shop.util.richfaces;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import de.shop.util.mobileesp.UAgentInfo;

/**
 * @author jbalunas@redhat.com
 * @author <a href="http://community.jboss.org/people/bleathem">Brian Leathem</a>
 * @author Juergen.Zimmermann@HS-Karlsruhe.de
 */
@Named("userAgent")
@SessionScoped
public class UserAgentProcessor implements Serializable {
	private static final long serialVersionUID = -1469351849761688348L;

	private transient UAgentInfo uAgentInfo;
    
	@Inject
	private transient HttpServletRequest request;

    @PostConstruct
    public void init() {
        final String userAgentStr = request.getHeader("user-agent");
        final String httpAccept = request.getHeader("accept");
        uAgentInfo = new UAgentInfo(userAgentStr, httpAccept);
    }

    public boolean isPhone() {
        // Detects a whole tier of phones that support similar functionality as the iphone
        return uAgentInfo.detectTierIphone();
    }

    public boolean isTablet() {
        // Will detect iPads, Xooms, Blackberry tablets, but not Galaxy - they use a strange user-agent
        return uAgentInfo.detectTierTablet();
    }

    public boolean isMobile() {
        return isPhone() || isTablet();
    }
}
