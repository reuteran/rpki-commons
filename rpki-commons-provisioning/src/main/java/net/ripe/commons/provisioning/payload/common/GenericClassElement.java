package net.ripe.commons.provisioning.payload.common;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import net.ripe.commons.certification.x509cert.X509ResourceCertificate;
import net.ripe.commons.provisioning.serialization.CertificateUrlListConverter;
import net.ripe.commons.provisioning.serialization.IpResourceSetProvisioningConverter;
import net.ripe.ipresource.IpResource;
import net.ripe.ipresource.IpResourceSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


public class GenericClassElement {

    @XStreamAsAttribute
    @XStreamAlias("class_name")
    private String className;

    @XStreamAlias("cert_url")
    @XStreamAsAttribute
    @XStreamConverter(CertificateUrlListConverter.class)
    private List<URI> certificateAuthorityUri;

    @XStreamAlias("resource_set_as")
    @XStreamAsAttribute
    @XStreamConverter(IpResourceSetProvisioningConverter.class)
    private IpResourceSet resourceSetAs = new IpResourceSet();

    @XStreamAlias("resource_set_ipv4")
    @XStreamAsAttribute
    @XStreamConverter(IpResourceSetProvisioningConverter.class)
    private IpResourceSet resourceSetIpv4 = new IpResourceSet();

    @XStreamAlias("resource_set_ipv6")
    @XStreamAsAttribute
    @XStreamConverter(IpResourceSetProvisioningConverter.class)
    private IpResourceSet resourceSetIpv6 = new IpResourceSet();

    @XStreamAlias("certificate")
    @XStreamImplicit(itemFieldName = "certificate")
    private List<CertificateElement> certificateElements;

    @XStreamConverter(X509ResourceCertificateBase64Converter.class)
    @XStreamAlias("issuer")
    private X509ResourceCertificate issuer;

    @XStreamAlias("resource_set_notafter")
    @XStreamAsAttribute
    private DateTime validityNotAfter;

    @XStreamAlias("suggested_sia_head")
    @XStreamAsAttribute
    private String siaHeadUri;

    public DateTime getValidityNotAfter() {
        return validityNotAfter;
    }

    public void setValidityNotAfter(DateTime validityNotAfter) {
        this.validityNotAfter = validityNotAfter;
    }

    public String getSiaHeadUri() {
        return siaHeadUri;
    }

    public void setSiaHeadUri(String siaHeadUri) {
        this.siaHeadUri = siaHeadUri;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<URI> getCertificateAuthorityUri() {
        return certificateAuthorityUri;
    }

    public void setCertUris(List<URI> certUris) {
        this.certificateAuthorityUri = certUris;
    }

    public IpResourceSet getResourceSetAsn() {
        return resourceSetAs;
    }

    public IpResourceSet getResourceSetIpv4() {
        return resourceSetIpv4;
    }

    public IpResourceSet getResourceSetIpv6() {
        return resourceSetIpv6;
    }

    public void setIpResourceSet(IpResourceSet ipResourceSet) {
        IpResourceSet asns = new IpResourceSet();
        IpResourceSet ipv4 = new IpResourceSet();
        IpResourceSet ipv6 = new IpResourceSet();

        Iterator<IpResource> iterator = ipResourceSet.iterator();
        while (iterator.hasNext()) {
            IpResource resource = iterator.next();
            switch (resource.getType()) {
            case ASN:
                asns.add(resource);
                break;
            case IPv4:
                ipv4.add(resource);
                break;
            case IPv6:
                ipv6.add(resource);
                break;
            }
        }

        resourceSetAs = asns;
        resourceSetIpv4 = ipv4;
        resourceSetIpv6 = ipv6;
    }


    public X509ResourceCertificate getIssuer() {
        return issuer;
    }

    public void setIssuer(X509ResourceCertificate issuer) {
        this.issuer = issuer;
    }

    protected List<CertificateElement> getCertificateElements() {
        return certificateElements;
    }

    protected void setCertificateElements(List<CertificateElement> certificateElements) {
        this.certificateElements = certificateElements;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

