package com.reporead.reporead.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final String geminiApiBaseUrlV2 = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=";
    //private final String geminiApiBaseUrlV15 = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";

    //private final OkHttpClient client = new OkHttpClient();
    // Custom OkHttpClient with increased timeouts
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build();

    public String generateReadme(String repoInfo, Map<String, String> options) {
        String prompt = generatePrompt(repoInfo, options);
        log.info("Generated prompt with options {}", options);

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
                    .url(geminiApiBaseUrlV2 + geminiApiKey)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute the request
            Response response = client.newCall(request).execute();
            log.info("Gemini API Request");

            // Log the response body
            String responseBody = response.body().string();
            //System.out.println("Gemini API Response: " + responseBody);

            // Parse the response to extract readme
            JSONObject responseJson = new JSONObject(responseBody);

            return parseResponse(responseJson);

        } catch (Exception e) {
            log.error(e.getMessage());
            return "Error generating markdown. " + e;
        }
    }

    public String generatePrompt(String repoInfo, Map<String, String> options) {
        StringBuilder prompt = new StringBuilder();

        // Language
        if (options.containsKey("language")) {
            prompt.append("Generate the response in ").append(options.get("language")).append(". ");
        }

        // Tone
        if (options.containsKey("tone")) {
            prompt.append("Use a ").append(options.get("tone")).append(" tone. ");
        }

        prompt.append("Generate a comprehensive README.md file for the given GitHub repository information. ");

        // Badges
        if (options.containsKey("badges") && Boolean.parseBoolean(options.get("badges"))) {
            prompt.append("Include relevant badges from valid platforms. ");
        }

        prompt.append("The README must include: \n");
        prompt.append("   - A project title and brief description.\n");
        prompt.append("   - A concise project description of the repository's purpose and main features.\n");
        prompt.append("   - A well-structured, clickable table of contents. \n");
        prompt.append("   - Detailed installation and setup instructions (include environment variable locations/formats, if any. Omit this if none exist.).\n");
        prompt.append("   - Clear instructions on how to run the project with examples of commands or configurations etc., if applicable.\n");
        prompt.append("   - A list and description of major dependencies and tools required for the project.\n");
        prompt.append("   - A contribution guide. \n");
        prompt.append("   - Inferred or placeholder License information (do not include any license if license is not already present in the repository).\n");
        prompt.append("   - An explanation of the purpose of the scripts in package.json and the files in requirements.txt.\n");
        prompt.append("   - An explanation of options in configuration files.\n");
        prompt.append("  If there are API endpoints in codebase, generate an API reference section with endpoints in a table (with parameters, description, and types). \n");
        prompt.append("If a README is present, extract and adapt relevant details. ");
        prompt.append("Format using markdown elements (headings, bullets, code blocks). Do not include additional commentary. Ensure information is accurate. Do not fabricate. Stick solely on the repository's structure and content. \n");

        prompt.append("Output strictly in valid Markdown format. ");
        prompt.append("Repository Information:\n");
        prompt.append(repoInfo);

        return prompt.toString();
    }

    private String parseResponse(JSONObject responseJson) {
        if (responseJson.has("candidates") && !responseJson.getJSONArray("candidates").isEmpty()) {
            JSONArray parts = responseJson.getJSONArray("candidates")
                    .getJSONObject(0) // Get the first candidate
                    .getJSONObject("content") // Access content
                    .getJSONArray("parts"); // Get the parts

            StringBuilder markdown = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                markdown.append(parts.getJSONObject(i).getString("text")).append("\n");
            }

            // Remove ```markdown at the start and ``` at the end
            String cleanedMarkdown = markdown.toString()
                    .replaceFirst("^```markdown\\s*", "") // Remove the starting ```markdown
                    .replaceFirst("\\s*```\\s*$", "");    // Remove the ending ```

            return cleanedMarkdown;
        } else {
            return "No markdown were generated.";
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