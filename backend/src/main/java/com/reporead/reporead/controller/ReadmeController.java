package com.reporead.reporead.controller;

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

@Slf4j
@RestController
@RequestMapping("/api")
public class ReadmeController {

    @Autowired
    private GitService gitService;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate-readme")
    public ResponseEntity<String> generateReadme(@RequestBody String repoUrl) {
        try {
            // Get repo tree from GitService
            String repoDetails = gitService.fetchRepositoryTree(repoUrl);

            log.info("Repo Details: {}", repoDetails);

            // Call Gemini API with return info
            String readmeMarkdown = geminiService.generateReadme(repoDetails);
            log.info("Readme Markdown: {}", readmeMarkdown);

            // Step 3: Return the generated markdown
            return ResponseEntity.ok(readmeMarkdown);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
