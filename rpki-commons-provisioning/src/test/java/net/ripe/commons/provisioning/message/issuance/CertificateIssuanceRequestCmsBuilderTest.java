package net.ripe.commons.provisioning.message.issuance;

import net.ripe.commons.provisioning.ProvisioningObjectMother;
import net.ripe.commons.provisioning.cms.ProvisioningCmsObject;
import net.ripe.commons.provisioning.cms.ProvisioningCmsObjectParser;
import net.ripe.commons.provisioning.x509.ProvisioningIdentityCertificateBuilderTest;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.junit.Test;

import static net.ripe.commons.provisioning.x509.ProvisioningCmsCertificateBuilderTest.EE_KEYPAIR;
import static net.ripe.commons.provisioning.x509.ProvisioningCmsCertificateBuilderTest.TEST_CMS_CERT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CertificateIssuanceRequestCmsBuilderTest {
    @Test
    public void shouldBuildValidListResponsePayload() throws Exception {
        // given
        PKCS10CertificationRequest pkcs10Request = ProvisioningObjectMother.generatePkcs10CertificationRequest(512, "RSA", "SHA1withRSA", "BC");

        CertificateIssuanceRequestCmsBuilder builder = new CertificateIssuanceRequestCmsBuilder();
        builder.withClassName("a classname");
        builder.withCmsCertificate(TEST_CMS_CERT.getCertificate()).withCrl(ProvisioningObjectMother.CRL);
        builder.withRecipient("recipient");
        builder.withAllocatedAsn("1234", "456");
        builder.withIpv4ResourceSet("10.0.0.0/8");
        builder.withIpv6ResourceSet("2001:0DB8::/48", "2001:0DB8:002::-2001:0DB8:005::");
        builder.withCertificateRequest(pkcs10Request);
        builder.withCaCertificate(ProvisioningIdentityCertificateBuilderTest.TEST_IDENTITY_CERT.getCertificate());

        // when
        ProvisioningCmsObject cmsObject = builder.build(EE_KEYPAIR.getPrivate());

        // then
        ProvisioningCmsObjectParser parser = new ProvisioningCmsObjectParser();
        parser.parseCms("/tmp/", cmsObject.getEncoded());

        CertificateIssuanceRequestPayloadWrapper payloadWrapper = (CertificateIssuanceRequestPayloadWrapper) parser.getPayloadWrapper();

        assertEquals("CN=test", payloadWrapper.getSender());
        assertEquals("recipient", payloadWrapper.getRecipient());

        CertificateIssuanceRequestPayload payloadContent = payloadWrapper.getPayloadContent();
        assertEquals("a classname", payloadContent.getClassName());
        assertEquals("1234", payloadContent.getAllocatedAsn()[0]);
        assertArrayEquals(pkcs10Request.getEncoded(), payloadContent.getCertificate().getEncoded());
        assertEquals("10.0.0.0/8", payloadContent.getAllocatedIpv4()[0]);
        assertEquals("2001:0DB8:002::-2001:0DB8:005::", payloadContent.getAllocatedIpv6()[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBuildWithoutCertificate() throws Exception {
        // given
        CertificateIssuanceRequestCmsBuilder builder = new CertificateIssuanceRequestCmsBuilder();
        builder.withClassName("a classname");
        builder.withCmsCertificate(TEST_CMS_CERT.getCertificate()).withCrl(ProvisioningObjectMother.CRL);
        builder.withRecipient("recipient");
        builder.withAllocatedAsn("1234", "456");

        // when
        builder.build(EE_KEYPAIR.getPrivate());
    }
}