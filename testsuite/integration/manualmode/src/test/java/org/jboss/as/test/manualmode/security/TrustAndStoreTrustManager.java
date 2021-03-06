/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
 */

package org.jboss.as.test.manualmode.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Josef Cacek
 */
public class TrustAndStoreTrustManager implements X509TrustManager {

    /**
     * Typically the owning test class name for debug purposes.
     */
    private final String ownerName;

    private static final X509Certificate[] EMPTY_ACCEPTED_ISSUERS = new X509Certificate[0];

    private final List<X509Certificate> clientCertsList = new ArrayList<X509Certificate>();

    public TrustAndStoreTrustManager(String ownerName) {
        super();
        this.ownerName = ownerName;
    }

    /**
     * Trust all certificates and add them to the {@link #clientCertsList}.
     *
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain != null && chain.length > 0) {
            // if the CLIENT_CERTS_LIST is getting too long, clear it
            synchronized (clientCertsList) {
                if (clientCertsList.size() > 50) {
                    clientCertsList.clear();
                }
                Collections.addAll(clientCertsList, chain);
            }
        }
    }

    /**
     * Trust all server certificates.
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // nothing to do here
    }

    /**
     * Returns empty array.
     *
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EMPTY_ACCEPTED_ISSUERS;
    }

    public boolean isSubjectInClientCertChain(String rfc2253Name) {
        if (rfc2253Name != null) {
            synchronized (clientCertsList) {
                for (X509Certificate cert : clientCertsList) {
                    if (rfc2253Name.equals(cert.getSubjectX500Principal().getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void clear() {
        synchronized (clientCertsList) {
            clientCertsList.clear();
        }
    }

    public int getCertCount() {
        synchronized (clientCertsList) {
            return clientCertsList.size();
        }
    }
}
