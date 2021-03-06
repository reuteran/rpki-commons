/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.rpki.commons.crypto.cms;

import net.ripe.rpki.commons.crypto.CertificateRepositoryObject;
import net.ripe.rpki.commons.crypto.ValidityPeriod;
import net.ripe.rpki.commons.crypto.crl.CrlLocator;
import net.ripe.rpki.commons.crypto.crl.X509Crl;
import net.ripe.rpki.commons.crypto.x509cert.X509ResourceCertificate;
import net.ripe.rpki.commons.validation.ValidationLocation;
import net.ripe.rpki.commons.validation.ValidationOptions;
import net.ripe.rpki.commons.validation.ValidationResult;
import net.ripe.rpki.commons.validation.ValidationString;
import net.ripe.rpki.commons.validation.objectvalidators.CertificateRepositoryObjectValidationContext;
import net.ripe.rpki.commons.validation.objectvalidators.ResourceValidatorFactory;
import net.ripe.rpki.commons.validation.objectvalidators.X509ResourceCertificateValidator;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.joda.time.DateTime;

import javax.security.auth.x500.X500Principal;
import java.net.URI;
import java.util.Arrays;

public abstract class RpkiSignedObject implements CertificateRepositoryObject {

    private static final long serialVersionUID = 1L;

    public static final String RSA_ENCRYPTION_OID = CMSSignedDataGenerator.ENCRYPTION_RSA;

    // The "sha256WithRsa" Object Id is defined in RFC6485 but no existing implementations, at least bouncy castle and
    // openssl support this. There is a plan to issue an erratum for RFC6485 to just go with plain "rsa" as existing
    // implementations are doing. Until that time, we had better accept both when doing validation.
    public static final String SHA256WITHRSA_ENCRYPTION_OID = PKCSObjectIdentifiers.sha256WithRSAEncryption.getId();

    /**
     * The digestAlgorithms set MUST include only SHA-256, the OID for which is
     * 2.16.840.1.101.3.4.2.1. [RFC4055] It MUST NOT contain any other
     * algorithms.
     */
    public static final String DIGEST_ALGORITHM_OID = CMSSignedDataGenerator.DIGEST_SHA256;

    private byte[] encoded;

    private X509ResourceCertificate certificate;

    private String oid; // Storing oid as String  so that this class is serializable

    private DateTime signingTime;

    protected RpkiSignedObject(RpkiSignedObjectInfo cmsObjectData) {
        this(cmsObjectData.getEncoded(), cmsObjectData.getCertificate(), cmsObjectData.getContentType(), cmsObjectData.getSigningTime());
    }

    protected RpkiSignedObject(byte[] encoded, X509ResourceCertificate certificate, ASN1ObjectIdentifier oid, DateTime signingTime) { //NOPMD - ArrayIsStoredDirectly
        this.encoded = encoded;
        this.certificate = certificate;
        this.oid = oid.getId();
        this.signingTime = signingTime;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    public DateTime getSigningTime() {
        return signingTime;
    }

    public ASN1ObjectIdentifier getContentType() {
        return new ASN1ObjectIdentifier(oid);
    }

    public X509ResourceCertificate getCertificate() {
        return certificate;
    }

    public boolean signedBy(X509ResourceCertificate certificate) {
        return this.certificate.equals(certificate);
    }

    public ValidityPeriod getValidityPeriod() {
        return certificate.getValidityPeriod();
    }

    public DateTime getNotValidBefore() {
        return certificate.getValidityPeriod().getNotValidBefore();
    }

    public DateTime getNotValidAfter() {
        return certificate.getValidityPeriod().getNotValidAfter();
    }

    public X500Principal getCertificateIssuer() {
        return getCertificate().getIssuer();
    }

    public X500Principal getCertificateSubject() {
        return getCertificate().getSubject();
    }

    @Override
    public URI getCrlUri() {
        return getCertificate().findFirstRsyncCrlDistributionPoint();
    }

    @Override
    public void validate(String location, CertificateRepositoryObjectValidationContext context, CrlLocator crlLocator, ValidationOptions options, ValidationResult result) {
        ValidationLocation savedCurrentLocation = result.getCurrentLocation();
        result.setLocation(new ValidationLocation(getCrlUri()));

        X509Crl crl = crlLocator.getCrl(getCrlUri(), context, result);

        result.setLocation(savedCurrentLocation);
        result.rejectIfNull(crl, ValidationString.OBJECTS_CRL_VALID, getCrlUri().toString());
        if (crl != null) {
            X509ResourceCertificateValidator validator = ResourceValidatorFactory.getX509ResourceCertificateStrictValidator(context, options, result, crl);
            validator.validate(location, getCertificate());
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getEncoded());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RpkiSignedObject other = (RpkiSignedObject) obj;
        return Arrays.equals(getEncoded(), other.getEncoded());
    }
}
