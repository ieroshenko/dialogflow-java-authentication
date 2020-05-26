import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Dialogflow {
    private final String projectId;
    private PrivateKey privateKey;
    private final SessionsSettings sessionsSettings;
    private SessionsClient sessionClient;
    private SessionName session;

    public Dialogflow() throws Exception {
        // data from json ApiKey (Not the best practice probably...)
        this.projectId = "your data";
        String privateKeyId = "your data";
        String clientEmail = "your data";
        String clientId = "your data";
        String tokenServerURI = "https://oauth2.googleapis.com/token"; // also your data

        extractPrivateKey();

        // get credintials object
        Credentials myCredentials =
                ServiceAccountCredentials.newBuilder()
                        .setProjectId(projectId)
                        .setPrivateKeyId(privateKeyId)
                        .setPrivateKey(privateKey)
                        .setClientEmail(clientEmail)
                        .setClientId(clientId)
                        .setTokenServerUri(URI.create(tokenServerURI))
                        .build();

        // Create session settings
        sessionsSettings =
                SessionsSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
                        .build();
    }

    /**
     * Extracts private key from a txt file
     *
     * @throws Exception
     */
    private void extractPrivateKey() throws Exception {
        // Copy the private API key from JSON file into text file!

        String path =
                (new File("src/main/rest/of/the/path/to/privatekey.txt")).getAbsolutePath();

        String privKey = Files.readString(Paths.get(path));

        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(privKey));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        // Remove the "BEGIN" and "END" lines, as well as any whitespace

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        // Base64 decode the result

        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);

        // extract the private key

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("RSA");
            try {
                privateKey = kf.generatePrivate(keySpec);
            } catch (InvalidKeySpecException e) {
                throw new Exception("Something went wrong when setting key value");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Something went wrong when setting key value\"");
        }
    }

    /**
     * Function initializes session with a user. Think opening a dialog window with the chatbot
     *
     * @throws IOException
     */
    public void initializeSession() throws IOException {
        SessionsClient sessionClient = SessionsClient.create(sessionsSettings);
        // set the attribute for persistent storage
        this.sessionClient = sessionClient;
        this.session = SessionName.of(projectId, "123456789");
        System.out.println("Session Path: " + session.toString());
    }

    /**
     * Function closes session with a user. Think closing a chat-bot dialog window
     *
     * @throws IOException
     */
    public void closeSession() throws IOException {
        System.out.println("Session" + session.toString() + " has been closed.");
        sessionClient.close();
    }

    /**
     * Returns the result of detect intent with end-user's text as input.
     *
     * <p>Using the same `session_id` between requests allows continuation of the conversation.
     *
     * @param text The text intent to be detected based on what a user says.
     * @param languageCode Language code of the query. English = 'en-US'
     * @return The QueryResult for input text.
     */
    public QueryResult detectIntentTexts(String text, String languageCode) throws Exception {

        TextInput.Builder textInput =
                TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

        // Build the query with the TextInput
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

        // Performs the detect intent request
        DetectIntentResponse response = this.sessionClient.detectIntent(session, queryInput);

        // Get the query result
        QueryResult queryResult = response.getQueryResult();

        return queryResult;
    }
}