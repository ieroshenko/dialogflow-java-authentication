import com.google.cloud.dialogflow.v2.QueryResult;


public class Main {
    public static void main(String[] args) throws Exception {

        // Example
        Dialogflow dialogflow = new Dialogflow();
        dialogflow.initializeSession();

        // Test detecting intent
        String userText = "Hello!";
        QueryResult result = dialogflow.detectIntentTexts(userText, "en-US");

        // Print out the reply from chat-bot
        System.out.println(result.getFulfillmentText());

        dialogflow.closeSession();
    }
}
