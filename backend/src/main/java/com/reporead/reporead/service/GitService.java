package com.reporead.reporead.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GitService {
    // GET https://uithub.com/{owner}/{repo}/tree/{branch}/{path}

    public String fetchRepositoryTree(String githubUrl) {
        // Transforming GitHub URL to UIThub URL
        if (!githubUrl.startsWith("https://github.com/") || githubUrl.startsWith("github.com/")) {
            throw new IllegalArgumentException("Invalid GitHub URL " + githubUrl);
        }

        String username = extractUsernameFromUrl(githubUrl);
        log.info("Fetching repository tree for GitHub user: {}", username);

        githubUrl = githubUrl.replaceFirst("g", "u");

        //log.info("Fetching repository tree for {}", githubUrl);

        WebClient webClient = WebClient.builder()
                .defaultHeader("Accept", "text/plain")
                .build();

        try {
            // Make the GET request to the UIThub API
            Mono<String> response = webClient.get()
                    .uri(githubUrl)
                    .retrieve()
                    .bodyToFlux(String.class) // This returns Flux<String>, a stream of parts of the body
                    .collectList() // Collect all the parts into a List<String>
                    .map(parts -> String.join("\n", parts)); // Join the parts into a single String

            String result = response.block(); // Get the full response as a single String

            // Prepend the username to the response
            String finalResponse = "Github Username: " + username + "\n" + result;

            log.info("Fetched repository tree for: {}",username);
            return finalResponse;


        } catch (WebClientResponseException e) {
            // Log details of the response exception (status, body, etc.)
            log.error("WebClientResponseException: Status code: {}, Response body: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch repository tree from API", e);

        } catch (Exception e) {
            // Log any other exceptions
            log.error("Exception occurred while fetching repository tree: ", e);
            throw new RuntimeException("Failed to fetch repository tree from API", e);
        }
    }

    public void processRepositoryTree(String githubUrl) {
        String response = fetchRepositoryTree(githubUrl);
        //log.info("Repository tree {}", response);

        // REFACTOR THIS METHOD TO ADD custom logic later
    }

    /**
     * Extracts the GitHub username (owner) from the URL.
     *
     * @param githubUrl GitHub URL (e.g., https://github.com/{owner}/{repo}/...)
     * @return The username (owner) part of the URL.
     */
    public static String extractUsernameFromUrl(String githubUrl) {
        // Strip the "https://github.com/" part of the URL and split by "/"
        String[] parts = githubUrl.replace("https://github.com/", "").split("/");
        if (parts.length >= 2) {
            return parts[0]; // The first part after "github.com/" is the username (owner)
        } else {
            throw new IllegalArgumentException("Invalid GitHub URL: " + githubUrl);
        }

    }
}













