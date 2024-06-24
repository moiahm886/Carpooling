package org.example;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

@Service
public class CarpoolingService {

    private final HFClient client;
    private final Channel channel;

    public CarpoolingService() throws Exception {
        this.client = HFClient.createNewInstance();
        this.channel = client.newChannel("Carpooling");

        // Add peers to the channel
        this.channel.addPeer(client.newPeer("peer0.org1.example.com", "grpc://localhost:7051"));
        this.channel.addPeer(client.newPeer("peer1.org1.example.com", "grpc://localhost:7056"));

        // Initialize the channel
        this.channel.initialize();

        // Generate key pair and certificate for user context
        KeyPair kp = generateKeyPair();
        Certificate certificate = generateCertificate(kp);
        FabricUser user = new FabricUser("user1", "org1", "Org1MSP", certificate, kp.getPrivate());
        client.setUserContext(user);
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048); // Adjust key size as necessary
        return kpg.generateKeyPair();
    }

    private Certificate generateCertificate(KeyPair keyPair) throws CertificateException, OperatorCreationException {
        X500Name x500Name = new X500Name("CN=example.com, OU=Security&Defense, O=Example Crypto., L=Ottawa, ST=Ontario, C=CA");
        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        Date startDate = new Date();
        Date expiryDate = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name, serialNumber, startDate, expiryDate, x500Name, pubKeyInfo);
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certificateBuilder.build(contentSigner));
    }

    public String initCarpooling(String carpoolingId, String owner, String origin, String destination,
                                 int nSlot, int price, long startTime) throws ProposalException, InvalidArgumentException {
        return invokeChaincode("initCarpooling", carpoolingId, owner, origin, destination, String.valueOf(nSlot), String.valueOf(price), String.valueOf(startTime));
    }

    public String bookSlots(String carpoolingId, String user, int nSlotBooked, int amount) throws ProposalException, InvalidArgumentException {
        return invokeChaincode("bookSlots", carpoolingId, user, String.valueOf(nSlotBooked), String.valueOf(amount));
    }

    public String requestRefund(String carpoolingId, String user, int nSlotAskRefund) throws ProposalException, InvalidArgumentException {
        return invokeChaincode("requestRefund", carpoolingId, user, String.valueOf(nSlotAskRefund));
    }

    public String validateBooking(String carpoolingId, String owner) throws ProposalException, InvalidArgumentException {
        return invokeChaincode("validateBooking", carpoolingId, owner);
    }

    public String settleBooking(String carpoolingId, String from, String to, int amountRefunded) throws ProposalException, InvalidArgumentException {
        return invokeChaincode("settleBooking", carpoolingId, from, to, String.valueOf(amountRefunded));
    }

    private String invokeChaincode(String fcn, String... args) throws ProposalException, InvalidArgumentException {
        try {
            TransactionProposalRequest request = client.newTransactionProposalRequest();
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("carpooling").build();
            request.setChaincodeID(chaincodeID);
            request.setFcn(fcn);
            request.setArgs(args);
            request.setProposalWaitTime(1000);

            Collection<ProposalResponse> responses = channel.sendTransactionProposal(request);

            StringBuilder responseBuilder = new StringBuilder();
            for (ProposalResponse response : responses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    responseBuilder.append("Successful response from peer ").append(response.getPeer().getName()).append("\n");
                } else {
                    responseBuilder.append("Failed response from peer ").append(response.getPeer().getName()).append(": ").append(response.getMessage()).append("\n");
                }
            }

            // Send transaction to the orderer
            channel.sendTransaction(responses);

            return "Transaction " + fcn + " completed successfully!\n" + responseBuilder.toString();
        } catch (ProposalException | InvalidArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ProposalException("Failed to send transaction proposal: " + e.getMessage());
        }
    }
}
