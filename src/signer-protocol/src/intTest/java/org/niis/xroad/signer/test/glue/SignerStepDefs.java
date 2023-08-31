/*
 * The MIT License
 *
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.niis.xroad.signer.test.glue;

import ee.ria.xroad.common.CodedException;
import ee.ria.xroad.common.OcspTestUtils;
import ee.ria.xroad.common.TestCertUtil;
import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.util.CryptoUtils;
import ee.ria.xroad.signer.SignerProxy;
import ee.ria.xroad.signer.protocol.dto.CertificateInfo;
import ee.ria.xroad.signer.protocol.dto.KeyInfo;
import ee.ria.xroad.signer.protocol.dto.KeyUsageInfo;
import ee.ria.xroad.signer.protocol.dto.TokenInfo;
import ee.ria.xroad.signer.protocol.dto.TokenInfoAndKeyId;
import ee.ria.xroad.signer.protocol.dto.TokenStatusInfo;

import com.nortal.test.core.report.TestReportService;
import io.cucumber.java.en.Step;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.niis.xroad.signer.proto.CertificateRequestFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ee.ria.xroad.common.util.CryptoUtils.SHA256WITHRSA_ID;
import static ee.ria.xroad.common.util.CryptoUtils.SHA256_ID;
import static ee.ria.xroad.common.util.CryptoUtils.calculateCertHexHash;
import static ee.ria.xroad.common.util.CryptoUtils.calculateDigest;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class SignerStepDefs {
    @Autowired
    private TestReportService testReportService;

    private String keyId;
    private String csrId;
    private String certHash;
    private CertificateInfo certInfo;
    private byte[] cert;

    @Step("signer is initialized with pin {string}")
    public void signerIsInitializedWithPin(String pin) throws Exception {
        SignerProxy.initSoftwareToken(pin.toCharArray());
    }

    @Step("token {string} is not active")
    public void tokenIsNotActive(String tokenId) throws Exception {
        final TokenInfo tokenInfo = SignerProxy.getToken(tokenId);

        Assertions.assertFalse(tokenInfo.isActive());
    }

    @Step("token {string} status is {string}")
    public void assertTokenStatus(String tokenId, String status) throws Exception {
        final TokenInfo token = SignerProxy.getToken(tokenId);
        assertThat(token.getStatus()).isEqualTo(TokenStatusInfo.valueOf(status));
    }

    @Step("tokens list contains token {string}")
    public void tokensListContainsToken(String tokenId) throws Exception {
        var tokens = SignerProxy.getTokens();
        testReportService.attachText("Tokens", Arrays.toString(tokens.toArray()));
        final TokenInfo tokenInfo = tokens.stream()
                .filter(token -> token.getId().equals(tokenId))
                .findFirst()
                .orElseThrow();
        assertThat(tokenInfo).isNotNull();
    }

    @Step("token {string} is logged in with pin {string}")
    public void tokenIsActivatedWithPin(String tokenId, String pin) throws Exception {
        SignerProxy.activateToken(tokenId, pin.toCharArray());
    }

    @Step("token {string} is logged out")
    public void tokenIsLoggedOut(String tokenId) throws Exception {
        SignerProxy.deactivateToken(tokenId);
    }

    @SneakyThrows
    @Step("token {string} is active")
    public void tokenIsActive(String tokenId) throws Exception {
        var tokenInfo = SignerProxy.getToken(tokenId);

        testReportService.attachText("TokenInfo", tokenInfo.toString());
        assertThat(tokenInfo.isActive()).isTrue();
    }

    @Step("token {string} pin is updated from {string} to {string}")
    public void tokenPinIsUpdatedFromTo(String tokenId, String oldPin, String newPin) throws Exception {
        SignerProxy.updateTokenPin(tokenId, oldPin.toCharArray(), newPin.toCharArray());
    }

    @Step("name {string} is set for token {string}")
    public void nameIsSetForToken(String name, String tokenId) throws Exception {
        SignerProxy.setTokenFriendlyName(tokenId, name);
    }

    @Step("token {string} name is {string}")
    public void tokenNameIs(String tokenId, String name) throws Exception {
        assertThat(SignerProxy.getToken(tokenId).getFriendlyName()).isEqualTo(name);
    }

    @Step("new key {string} generated for token {string}")
    public void newKeyGeneratedForToken(String keyLabel, String tokenId) throws Exception {
        final KeyInfo keyInfo = SignerProxy.generateKey(tokenId, keyLabel);
        this.keyId = keyInfo.getId();
    }

    @Step("name {string} is set for generated key")
    public void nameIsSetForGeneratedKey(String keyFriendlyName) throws Exception {
        SignerProxy.setKeyFriendlyName(this.keyId, keyFriendlyName);
    }

    @Step("token {string} has exact keys {string}")
    public void tokenHasKeys(String tokenId, String keyNames) throws Exception {
        final List<String> keys = Arrays.asList(keyNames.split(","));
        final TokenInfo token = SignerProxy.getToken(tokenId);

        assertThat(token.getKeyInfo().size()).isEqualTo(keys.size());

        final List<String> tokenKeyNames = token.getKeyInfo().stream()
                .map(KeyInfo::getFriendlyName)
                .collect(Collectors.toList());

        assertThat(tokenKeyNames).containsExactlyInAnyOrderElementsOf(keys);
    }

    @Step("key {string} is deleted from token {string}")
    public void keyIsDeletedFromToken(String keyName, String tokenId) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyName);
        SignerProxy.deleteKey(key.getId(), true);
    }

    private KeyInfo findKeyInToken(String tokenId, String keyName) throws Exception {
        var foundKeyInfo = SignerProxy.getToken(tokenId).getKeyInfo().stream()
                .filter(keyInfo -> keyInfo.getFriendlyName().equals(keyName))
                .findFirst()
                .orElseThrow();
        testReportService.attachText("Key [" + keyName + "]", foundKeyInfo.toString());
        return foundKeyInfo;
    }

    @Step("Certificate is imported for client {string}")
    public void certificateIsImported(String client) throws Exception {
        keyId = SignerProxy.importCert(cert, CertificateInfo.STATUS_REGISTERED, getClientId(client));
    }

    @Step("Wrong Certificate is not imported for client {string}")
    public void certImportFails(String client) throws Exception {
        byte[] certBytes = fileToBytes("src/intTest/resources/cert-01.pem");
        try {
            SignerProxy.importCert(certBytes, CertificateInfo.STATUS_REGISTERED, getClientId(client));
        } catch (CodedException codedException) {
            assertException("Signer.KeyNotFound", "key_not_found_for_certificate",
                    "Signer.KeyNotFound: Could not find key that has public key that matches the public key of certificate", codedException);
        }
    }

    private byte[] fileToBytes(String fileName) throws Exception {
        try (FileInputStream in = new FileInputStream(fileName)) {
            return IOUtils.toByteArray(in);
        }
    }

    @Step("self signed cert generated for token {string} key {string}, client {string}")
    public void selfSignedCertGeneratedForTokenKeyForClient(String tokenId, String keyName, String client) throws Exception {
        final KeyInfo keyInToken = findKeyInToken(tokenId, keyName);

        cert = SignerProxy.generateSelfSignedCert(keyInToken.getId(), getClientId(client), KeyUsageInfo.SIGNING,
                "CN=" + client, Date.from(now().minus(5, DAYS)), Date.from(now().plus(5, DAYS)));
        this.certHash = CryptoUtils.calculateCertHexHash(cert);
    }

    private ClientId.Conf getClientId(String client) {
        final String[] parts = client.split(":");
        return ClientId.Conf.create(parts[0], parts[1], parts[2]);
    }

    @Step("cert request is generated for token {string} key {string} for client {string}")
    public void certRequestIsGeneratedForTokenKey(String tokenId, String keyName, String client) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyName);
        final ClientId.Conf clientId = getClientId(client);
        final SignerProxy.GeneratedCertRequestInfo csrInfo =
                SignerProxy.generateCertRequest(key.getId(), clientId, KeyUsageInfo.SIGNING,
                        "CN=key-" + keyName, CertificateRequestFormat.DER);

        this.csrId = csrInfo.getCertReqId();
    }

    @Step("cert request is regenerated")
    public void certRequestIsRegenerated() throws Exception {
        SignerProxy.regenerateCertRequest(this.csrId, CertificateRequestFormat.DER);
    }

    @Step("token {string} key {string} has {int} certificates")
    public void tokenKeyHasCertificates(String tokenId, String keyName, int certCount) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyName);

        assertThat(key.getCerts()).hasSize(certCount);
    }

    @Step("sign mechanism for token {string} key {string} is not null")
    public void signMechanismForTokenKeyIsNotNull(String tokenId, String keyName) throws Exception {
        final KeyInfo keyInToken = findKeyInToken(tokenId, keyName);
        final String signMechanism = SignerProxy.getSignMechanism(keyInToken.getId());

        assertThat(signMechanism).isNotBlank();
    }

    @Step("member {string} has {int} certificate")
    public void memberHasCertificate(String memberId, int certCount) throws Exception {
        final List<CertificateInfo> memberCerts = SignerProxy.getMemberCerts(getClientId(memberId));
        assertThat(memberCerts).hasSize(certCount);
    }

    @Step("check token {string} key {string} batch signing enabled")
    public void checkTokenBatchSigningEnabled(String tokenId, String keyname) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyname);

        assertThat(SignerProxy.isTokenBatchSigningEnabled(key.getId())).isNotNull();
    }

    @Step("cert request can be deleted")
    public void certRequestCanBeDeleted() throws Exception {
        SignerProxy.deleteCertRequest(this.csrId);
    }

    @Step("certificate info can be retrieved by cert hash")
    public void certificateInfoCanBeRetrievedByHash() throws Exception {
        final CertificateInfo certInfoResponse = SignerProxy.getCertForHash(this.certHash);
        assertThat(certInfoResponse).isNotNull();
        this.certInfo = certInfoResponse;
    }

    @Step("keyId can be retrieved by cert hash")
    public void keyidCanBeRetrievedByCertHash() throws Exception {
        final SignerProxy.KeyIdInfo keyIdForCertHash = SignerProxy.getKeyIdForCertHash(this.certHash);
        assertThat(keyIdForCertHash).isNotNull();
    }

    @Step("token and keyId can be retrieved by cert hash")
    public void tokenAndKeyIdCanBeRetrievedByCertHash() {
        final TokenInfoAndKeyId tokenAndKeyIdForCertHash = SignerProxy.getTokenAndKeyIdForCertHash(this.certHash);
        assertThat(tokenAndKeyIdForCertHash).isNotNull();
    }

    @Step("token and key can be retrieved by cert request")
    public void tokenAndKeyCanBeRetrievedByCertRequest() throws Exception {
        final TokenInfoAndKeyId tokenAndKeyIdForCertRequestId = SignerProxy.getTokenAndKeyIdForCertRequestId(this.csrId);
        assertThat(tokenAndKeyIdForCertRequestId).isNotNull();
    }

    @Step("token info can be retrieved by key id")
    public void tokenInfoCanBeRetrievedByKeyId() throws Exception {
        final TokenInfo tokenForKeyId = SignerProxy.getTokenForKeyId(this.keyId);
        assertThat(tokenForKeyId).isNotNull();
    }

    @Step("digest can be signed using key {string} from token {string}")
    public void digestCanBeSignedUsingKeyFromToken(String keyName, String tokenId) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyName);

        SignerProxy.sign(key.getId(), SHA256WITHRSA_ID, calculateDigest(SHA256_ID, "digest".getBytes(UTF_8)));
    }

    @Step("certificate can be deactivated")
    public void certificateCanBeDeactivated() throws Exception {
        SignerProxy.deactivateCert(this.certInfo.getId());
    }

    @Step("certificate can be activated")
    public void certificateCanBeActivated() throws Exception {
        SignerProxy.activateCert(this.certInfo.getId());
    }

    @Step("certificate can be deleted")
    public void certificateCanBeDeleted() throws Exception {
        SignerProxy.deleteCert(this.certInfo.getId());
    }

    @Step("certificate status can be changed to {string}")
    public void certificateStatusCanBeChangedTo(String status) throws Exception {
        SignerProxy.setCertStatus(this.certInfo.getId(), status);
    }

    @Step("certificate can be signed using key {string} from token {string}")
    public void certificateCanBeSignedUsingKeyFromToken(String keyName, String tokenId) throws Exception {
        final KeyInfo key = findKeyInToken(tokenId, keyName);
        byte[] keyBytes = Base64.decode(key.getPublicKey().getBytes());
        X509EncodedKeySpec x509publicKey = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(x509publicKey);

        final byte[] bytes = SignerProxy.signCertificate(key.getId(), SHA256WITHRSA_ID, "CN=cs", publicKey);
        assertThat(bytes).isNotEmpty();
    }

    @Step("Set token name fails with TokenNotFound exception when token does not exist")
    public void setTokenNameFail() throws Exception {
        String tokenId = randomUUID().toString();
        try {
            SignerProxy.setTokenFriendlyName(tokenId, randomUUID().toString());
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.TokenNotFound", "token_not_found",
                    "Signer.TokenNotFound: Token '" + tokenId + "' not found", codedException);
        }
    }

    @Step("Deleting not existing certificate from token fails")
    public void failOnDeleteCert() throws Exception {
        String cerId = randomUUID().toString();
        try {
            SignerProxy.deleteCert(cerId);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.CertNotFound", "cert_with_id_not_found",
                    "Signer.CertNotFound: Certificate with id '" + cerId + "' not found", codedException);
        }
    }

    @Step("Retrieving token info by not existing key fails")
    public void retrievingTokenInfoCanByNotExistingKeyFails() throws Exception {
        String keyId = randomUUID().toString();
        try {
            SignerProxy.getTokenForKeyId(keyId);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.KeyNotFound", "key_not_found",
                    "Signer.KeyNotFound: Key '" + keyId + "' not found", codedException);
        }
    }

    @Step("Deleting not existing certRequest fails")
    public void deletingCertRequestFails() throws Exception {
        String csrId = randomUUID().toString();
        try {
            SignerProxy.deleteCertRequest(csrId);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.CsrNotFound", "csr_not_found",
                    "Signer.CsrNotFound: Certificate request '" + csrId + "' not found", codedException);
        }
    }

    @Step("Signing with unknown key fails")
    public void signKeyFail() throws Exception {
        String keyId = randomUUID().toString();
        try {
            SignerProxy.sign(keyId, randomUUID().toString(), new byte[0]);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.KeyNotFound", "key_not_found",
                    "Signer.KeyNotFound: Key '" + keyId + "' not found", codedException);
        }
    }

    @Step("Signing with unknown algorithm fails using key {string} from token {string}")
    public void signAlgorithmFail(String keyName, String tokenId) throws Exception {
        try {
            final KeyInfo key = findKeyInToken(tokenId, keyName);
            SignerProxy.sign(key.getId(), "NOT-ALGORITHM-ID", calculateDigest(SHA256_ID, "digest".getBytes(UTF_8)));

            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.CannotSign.InternalError", "",
                    "Signer.CannotSign.InternalError: Unknown sign algorithm id: NOT-ALGORITHM-ID", codedException);
        }
    }

    @Step("Getting key by not existing cert hash fails")
    public void getKeyIdByHashFail() throws Exception {
        String hash = randomUUID().toString();
        try {
            SignerProxy.getKeyIdForCertHash(hash);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.CertNotFound", "certificate_with_hash_not_found",
                    "Signer.CertNotFound: Certificate with hash '" + hash + "' not found", codedException);
        }
    }

    @Step("Not existing certificate can not be activated")
    public void notExistingCertActivateFail() throws Exception {
        String certId = randomUUID().toString();
        try {
            SignerProxy.activateCert(certId);
            fail("Exception expected");
        } catch (CodedException codedException) {
            assertException("Signer.CertNotFound", "cert_with_id_not_found",
                    "Signer.CertNotFound: Certificate with id '" + certId + "' not found", codedException);
        }
    }

    @Step("Member signing info for client {string} is retrieved")
    public void getMemberSigningInfo(String client) throws Exception {
        var memberInfo = SignerProxy.getMemberSigningInfo(getClientId(client));
        testReportService.attachText("MemberSigningInfo", memberInfo.toString());
    }

    @Step("HSM is operational")
    public void hsmIsNotOperational() throws Exception {
        assertTrue(SignerProxy.isHSMOperational());
    }

    private void assertException(String faultCode, String translationCode, String message, CodedException codedException) {
        assertEquals(faultCode, codedException.getFaultCode());
        assertEquals(translationCode, codedException.getTranslationCode());
        assertEquals(message, codedException.getMessage());
    }


    @Step("ocsp responses are set")
    public void ocspResponsesAreSet() throws Exception {
        X509Certificate subject = TestCertUtil.getConsumer().certChain[0];
        final OCSPResp ocspResponse = OcspTestUtils.createOCSPResponse(subject, TestCertUtil.getCaCert(), TestCertUtil.getOcspSigner().certChain[0],
                TestCertUtil.getOcspSigner().key, CertificateStatus.GOOD);

        SignerProxy.setOcspResponses(new String[]{calculateCertHexHash(subject)},
                new String[]{Base64.toBase64String(ocspResponse.getEncoded())});
    }

    @Step("ocsp responses can be retrieved")
    public void ocspResponsesCanBeRetrieved() throws Exception {
        X509Certificate subject = TestCertUtil.getConsumer().certChain[0];
        final String hash = calculateCertHexHash(subject);

        final String[] ocspResponses = SignerProxy.getOcspResponses(new String[]{hash});
        assertThat(ocspResponses).isNotEmpty();
    }

    @Step("null ocsp response is returned for unknown certificate")
    public void emptyOcspResponseIsReturnedForUnknownCertificate() throws Exception {
        final String[] ocspResponses = SignerProxy
                .getOcspResponses(new String[]{calculateCertHexHash("not a cert".getBytes())});
        assertThat(ocspResponses).hasSize(1);
        assertThat(ocspResponses[0]).isNull();
    }

}
