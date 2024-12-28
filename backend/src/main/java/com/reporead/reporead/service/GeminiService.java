package com.reporead.reporead.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final OkHttpClient client = new OkHttpClient();


    public String generateReadme(String repoInfo) {
        WebClient webClient = WebClient.builder()
                .build();

        String prompt = generatePrompt(repoInfo);

        // Create the JSON body
        //log.info(jsonBody.toString());
        String uri = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + geminiApiKey;
        log.info(geminiApiKey);
        log.info(uri);

        try {
            // Create the JSON body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("contents", new JSONArray()
                    .put(new JSONObject()
                            .put("parts", new JSONArray()
                                    .put(new JSONObject()
                                            .put("text", prompt)))));

            RequestBody body = RequestBody.create(
                    //jsonBody.toString(),
                    generateJsonBody(prompt).toString(),
                    MediaType.get("application/json"));

            // Build the request
            Request request = new Request.Builder()
                    //.url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + geminiApiKey)
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + geminiApiKey)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute the request
            Response response = client.newCall(request).execute();

            // Log the response body
            String responseBody = response.body().string();
            System.out.println("Gemini API Response: " + responseBody); // Log the full response

            // Parse the response to extract the questions
            JSONObject responseJson = new JSONObject(responseBody);

            String contents = parseResponse(responseJson);
            return contents;

        } catch (Exception e) {
            // Exception handling
            return "Error generating markdown.";
        }
    }



    public String generatePrompt(String repoInfo) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a technical documentation expert. " +
                "Analyze the provided GitHub repository and generate a high-quality, " +
                "professional and comprehensive README.md file that will help developers understand and work with this project. " +
                "The README must include a centered project title and brief description " +
                "followed by relevant badges sourced from valid platforms but do not add license badge if license is not available in repository. " +
                "Write a concise project description introducing the repository's " +
                "purpose and functionality, followed by an " +
                "engaging overview of its main features. " +
                "Include a well-structured, clickable table of contents to aid navigation. " +
                "Provide detailed and accurate instructions for installation and setup, " +
                "specifying where environment variables (if any) " +
                "should be placed and their required formats. " +
                "If no environment variables are needed, " +
                "omit this section entirely. " +
                "Clearly explain how to run the project with examples " +
                "of commands or configurations when applicable. " +
                "List and describe all major dependencies " +
                "and tools required for the project. " +
                "Include a contribution guide explaining " +
                "how developers can participate, " +
                "along with inferred or placeholder License information (do not add any License if not found in the repository codebase). " +
                //"Add a contact section with placeholder details for maintainers or contributors. " +
                "Format the README visually and structurally using appropriate Markdown elements, " +
                "such as headings, subheadings, bullet points, and code blocks, " +
                "ensuring the output is aesthetically pleasing and optimized for readability. " +
                "Ensure all information is accurate and non-fabricated, " +
                "based solely on the repository's structure and content. " +
                "If there are scripts in package.json or requirements.txt, explain their purposes " +
                "If there are configuration files, explain their options " +
                "Keep the tone professional but friendly. " +
                "If there are API endpoints in codebase, generate api reference section with endpoints that are placed in table with parameters, description and type " +
                "If a README is already present, " +
                "extract and adapt relevant details without copying or mimicking " +
                "its content directly. Deliver the output strictly in valid Markdown format " +
                "without any additional commentary. ");

        prompt.append(repoInfo);

        return prompt.toString();
    }

    private String parseResponse(JSONObject responseJson) {
        if (responseJson.has("candidates") && !responseJson.getJSONArray("candidates").isEmpty()) {
            JSONArray parts = responseJson.getJSONArray("candidates")
                    .getJSONObject(0) // Get the first candidate
                    .getJSONObject("content") // Access content
                    .getJSONArray("parts"); // Get the parts

            StringBuilder questions = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                questions.append(parts.getJSONObject(i).getString("text")).append("\n");
            }

            return questions.toString();
        } else {
            return "No interview questions were generated.";
        }
    }

    public JSONObject generateJsonBody(String prompt) {

        // Create the JSON body
        JSONObject jsonBody = new JSONObject();

        // Create the contents array with the role and parts
        JSONArray contentsArray = new JSONArray();
        JSONObject contentObject = new JSONObject();
        contentObject.put("role", "user");

        // Create the parts array containing the prompt text
        JSONArray partsArray = new JSONArray();
        JSONObject partsObject = new JSONObject();
        partsObject.put("text", prompt);  // Insert the prompt here
        partsArray.put(partsObject);

        // Add the parts array to the content object
        contentObject.put("parts", partsArray);
        contentsArray.put(contentObject);

        // Add contents to the jsonBody
        jsonBody.put("contents", contentsArray);

        // Create the generationConfig object with the specified parameters
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 1);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 8192);
        generationConfig.put("responseMimeType", "text/plain");

        // Add generationConfig to the jsonBody
        jsonBody.put("generationConfig", generationConfig);

        return jsonBody;
    }


}

/*
API_KEY="YOUR_API_KEY"

curl \
  -X POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=${API_KEY} \
  -H 'Content-Type: application/json' \
  -d @<(echo '{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": "INSERT_INPUT_HERE"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 1,
    "topK": 40,
    "topP": 0.95,
    "maxOutputTokens": 8192,
    "responseMimeType": "text/plain"
  }
}')
 */