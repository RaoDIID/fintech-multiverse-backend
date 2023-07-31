package com.fintech.multiverse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.multiverse.domain.Account;
import com.fintech.multiverse.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Service
public class FintechService {

    @Autowired
    private WebClient webClient;

    public String getToken(String callType, String authCode) {
        MultiValueMap<String, String> req = new LinkedMultiValueMap<>();
        req.add("client_id", "n0AkrRXwEYUbGq7PJZA-yCd9twTTschPeBIB99ZcroU=");
        req.add("client_secret", "ftsByifvwpMeQ-w8amUjpoIb-A9Xc1jCimLoLiBdKmo=");
        if(callType.equalsIgnoreCase("exchange")) {
            req.add("grant_type", "authorization_code");
            req.add("redirect_uri", "https://3f379b1a-abcd-4b3f-8b6c-a95913806725.example.org/redirect");
            req.add("code", authCode);
        }
        else {
            req.add("scope", "accounts");
            req.add("grant_type", "client_credentials");
        }

        Map<String, String> res = webClient
                .post()
                .uri("https://ob.sandbox.natwest.com/token")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromFormData(req))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        System.out.println("xxxx = "+ res.get("access_token"));
        return res.get("access_token");

    }

    public String postAccountRequest(String token) {
        String req = "{\n" +
                "  \"Data\": {\n" +
                "    \"Permissions\": [\n" +
                "      \"ReadAccountsDetail\",\n" +
                "      \"ReadBalances\",\n" +
                "      \"ReadTransactionsCredits\",\n" +
                "      \"ReadTransactionsDebits\",\n" +
                "      \"ReadTransactionsDetail\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"Risk\": {}\n" +
                "}";

        Map block1 = webClient
                .post()
                .uri("https://ob.sandbox.natwest.com/open-banking/v3.1/aisp/account-access-consents")
                .header("Authorization", "Bearer "+token)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(req))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        System.out.println(block1);

        Map data = (Map) block1.get("Data");

        return String.valueOf(data.get("ConsentId"));
    }

    public String approveConsent(String consentId) throws URISyntaxException, JsonProcessingException {

        URI uri = new URI("https://api.sandbox.natwest.com/authorize?client_id=n0AkrRXwEYUbGq7PJZA-yCd9twTTschPeBIB99ZcroU=&response_type=code%20id_token&scope=openid%20accounts&redirect_uri=https%3A%2F%2F3f379b1a-abcd-4b3f-8b6c-a95913806725.example.org%2Fredirect&state=ABC&request="+consentId+"&authorization_mode=AUTO_POSTMAN&authorization_username=123456789012@3f379b1a-abcd-4b3f-8b6c-a95913806725.example.org");

        Map block = webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        System.out.println(block);
        String redirectUri = String.valueOf(block.get("redirectUri"));
        String authCode = redirectUri.split("#code=")[1].split("&id_token=")[0];

        System.out.println(authCode);

        return authCode;
    }

    public List<Account> getAccounts() throws URISyntaxException, JsonProcessingException {
        String token = getToken("token", null);
        String consentId = postAccountRequest(token);
        String authCode = approveConsent(consentId);
        String newToken = getToken("exchange", authCode);

        Map block1 = webClient
                .get()
                .uri("https://ob.sandbox.natwest.com/open-banking/v3.1/aisp/accounts")
                .header("Authorization", "Bearer "+newToken)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        clientResponse.body((clientHttpResponse, context) -> {
                            System.out.println(clientHttpResponse.getBody());
                            return clientHttpResponse.getBody();
                        });
                        System.out.println(clientResponse.bodyToMono(Map.class));
                        return clientResponse.bodyToMono(Map.class);
                    } else
                        System.out.println(clientResponse.bodyToMono(Map.class));
                    return clientResponse.bodyToMono(Map.class);
                })
                .block();
        Map data = (Map) block1.get("Data");
        List<Account> accounts = (List<Account>) data.get("Account");

        System.out.println(accounts);
        String s = new ObjectMapper().writeValueAsString(block1);
        System.out.println(s);
        return accounts;


    }

    public List<Transaction> getTransactions(String accountId) throws URISyntaxException, JsonProcessingException {
        String token = getToken("token", null);
        String consentId = postAccountRequest(token);
        String authCode = approveConsent(consentId);
        String newToken = getToken("exchange", authCode);

        URI uri = new URI("https://ob.sandbox.natwest.com/open-banking/v3.1/aisp/accounts/"+accountId+"/transactions");

        Map block1 = webClient
                .get()
                .uri(uri)
                .header("Authorization", "Bearer "+newToken)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is5xxServerError()) {
                        clientResponse.body((clientHttpResponse, context) -> {
                            System.out.println(clientHttpResponse.getBody());
                            return clientHttpResponse.getBody();
                        });
                        System.out.println(clientResponse.bodyToMono(Map.class));
                        return clientResponse.bodyToMono(Map.class);
                    } else
                        System.out.println(clientResponse.bodyToMono(Map.class));
                    return clientResponse.bodyToMono(Map.class);
                })
                .block();
        Map data = (Map) block1.get("Data");
        List<Transaction> transactions = (List<Transaction>) data.get("Transaction");
        System.out.println(block1.get("Data"));
        String s = new ObjectMapper().writeValueAsString(block1);
        System.out.println(s);
        return transactions;
    }
}
