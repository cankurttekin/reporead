package com.reporead.reporead.controller;

import com.reporead.reporead.model.ReadmeRequest;
import com.reporead.reporead.service.GeminiService;
import com.reporead.reporead.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ReadmeController {

    @Autowired
    private GitService gitService;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate-readme")
    public ResponseEntity<String> generateReadme(@RequestBody ReadmeRequest request) {
        try {
            // Get repo tree from GitService
            String repoDetails = gitService.fetchRepositoryTree(request.getRepoUrl());
            //log.info("Repo Details: {}", repoDetails);

            // Prepare options map
            Map<String, String> options = new HashMap<>();
            if (request.getTone() != null) {
                options.put("tone", request.getTone());
            }
            if (request.getLanguage() != null) {
                options.put("language", request.getLanguage());
            }
            if (request.getBadges() != null) {
                options.put("badges", request.getBadges().toString());
            }

            // Call Gemini API with return info
            String readmeMarkdown = geminiService.generateReadme(repoDetails, options);
            //log.info("Generated README Markdown: {}", readmeMarkdown);


            log.info("Generated README for repo: {}, user: {}", request.getRepoUrl(), gitService.extractUsernameFromUrl(request.getRepoUrl()));
            // Return the generated markdown
            return ResponseEntity.ok(readmeMarkdown);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
