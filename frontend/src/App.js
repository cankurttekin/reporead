import React, { useState, useEffect } from "react";
import styled, { keyframes } from "styled-components"; // Import keyframes for animations
import axios from "axios";
import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import remarkGfm from "remark-gfm"; // GitHub Flavored Markdown for tables, task lists, etc.
import AceEditor from "react-ace";
import "ace-builds/src-noconflict/mode-markdown";
import "ace-builds/src-noconflict/theme-github"; // GitHub theme for Ace Editor

// Styled components for layout
const Container = styled.div`
  font-family: Josefin Sans, sans-serif;
  color: #1f2328;
  text-align: center;
  background-color: #212d63;
  min-height: 100vh; /* Ensure it fills the viewport height */
  display: flex;
  flex-direction: column; /* Stack items vertically */
  justify-content: flex-start; /* Start at the top */
`;

const Title = styled.h1`
  font-size: 3.7em;
  color: #ffffff;
  margin-top: 200px;
  margin-bottom: 0;
  align-self: center;
  //text-shadow: 6px 6px 6px rgba(51, 51, 51, 1);
`;

const SubTitle = styled.h3`
  font-size: 1.5em;
  color: #ffffff;
  margin-bottom: 20px;
  margin-top: 0;
  width: 36%;
  align-self: center;
`;

const InputContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2px;
  padding: 20px;
  border-radius: 12px;
`;

const Download = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 12px;
`;

const TextInput = styled.input`
  width: 30%;
  padding: 10px;
  font-size: 1em;
  
  border: 2px solid #000000;
  border-bottom: 8px solid #000000;
  border-right: 8px solid #000000;
  border-radius: 12px;
`;

const Dropdown = styled.select`
  padding: 10px;
  font-size: 1em;
  border: 2px solid #000000;
  border-bottom: 8px solid #000000;
  border-right: 8px solid #000000;
  border-radius: 12px;
  background-color: #ffffff;
`;

const DropdownContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px; /* Space between dropdowns */
  margin: 20px 0;
  color: white;
`;


const DropdownLabel = styled.label`
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 1em;
  gap: 5px;
`;

const CheckboxContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
`;

const CheckboxLabel = styled.label`
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 1em;
  gap: 5px;
`;

const Button = styled.button`
  padding: 10px 20px;
  background-color: white;
  color: black;
  font-size: 1em;
  font-weight: bold;
  border: 2px solid #000000;
  border-bottom: 8px solid #000000;
  border-right: 8px solid #000000;
  border-radius: 12px;
  cursor: pointer;
  margin-left: 10px;
  &:hover {
    background-color: #f5f5f5;
  }
`;

const ExampleRepos = styled.div`
  margin-top: 20px;
  font-size: 1.2em;
  color: #767e8b;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px; /* Space between buttons */
  flex-wrap: wrap; /* Wrap buttons if screen is narrow */
`;

const ExampleButton = styled.button`
  padding: 10px 15px;
  background-color: white;
  color: black;
  font-size: 0.7em;
  font-weight: bold;
  border: 2px solid #000000;
  border-bottom: 8px solid #000000;
  border-right: 8px solid #000000;
  border-radius: 12px;
  cursor: pointer;

  &:hover {
    background-color: #f5f5f5;
  }
`;

const LoadingContainer = styled.div`
  margin-top: 20px;
  font-size: 1.2em;
  color: #33cc7f;
  white-space: pre-line;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const LoadingIcon = styled.div`
  margin-top: 10px;
  border: 8px solid #f3f3f3; /* Light gray background */
  border-top: 8px solid orange; /* Blue color for the top */
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: ${keyframes`
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  `} 1s linear infinite; /* Rotate infinitely */
`;

const MarkdownContainer = styled.div`
  display: flex;
  justify-content: space-around;
  margin-top: 40px;
  margin-right: 40px;
  margin-left: 40px;
  border-radius: 16px;
  text-align: left;
`;

const EditorWrapper = styled.div`
  width: 48%;
  //height: 70vh;
`;

const MarkdownPreview = styled.div`
  width: 48%;
  height: 70vh;
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 20px;
  overflow: auto;
  border-radius: 16px;
`;

const Footer = styled.footer`
  font-size: 0.9em;
  color: darkorange;
  padding: 2px 0;
  text-align: center;
  margin-top: auto; /* Push footer to the bottom */
`;

function App() {
  const [githubUrl, setGithubUrl] = useState("");
  const [generatedReadme, setGeneratedReadme] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingMessageIndex, setLoadingMessageIndex] = useState(0);

  const [language, setLanguage] = useState("English"); // Default language
  const [tone, setTone] = useState("Neutral"); // Default tone
  const [badges, setBadges] = useState(true); // State for badges

  // Function to trigger file download
  const downloadFile = () => {
    const blob = new Blob([generatedReadme], { type: "text/markdown" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "README.md"; // File name for the download
    link.click();
    URL.revokeObjectURL(url); // Clean up the URL object after download
  };

  const loadingMessages = [
    "Checking if it's cached...",
    "Gathering info about repository...",
    "Analyzing repository...",
    "Generating prompt for you <3",
    "Prompting Gemini to do its job...",
    "F, this takes too long...",
    "Prompt engineers needed: dm me on linkedin/in/cankurttekin",
    "Harder, Better, Faster, Stronger",
    "K I am gonna give you your damn markdown, relax aq...",
    "Guess not Im not in the mood rn...",
    "Wait, wait I'm coming.."
  ];

  // Handle user input for GitHub URL
  const handleUrlChange = (e) => {
    setGithubUrl(e.target.value);
  };

  // Handle button click to fetch README data
  const handleGenerateReadme = async () => {
    if (!githubUrl) {
      alert("Please enter a valid GitHub URL!");
      return;
    }

    setLoading(true);

    // Start displaying the loading messages
    let messageIndex = 0;
    const interval = setInterval(() => {
      if (messageIndex < loadingMessages.length) {
        setLoadingMessageIndex(messageIndex);
        messageIndex++;
      } else {
        clearInterval(interval);
      }
    }, 5000); // Display a new message every 5 seconds

    try {
      // Call the backend API to generate the README
      const response = await axios.post(
          "http://localhost:8080/api/generate-readme",
          {
            repoUrl: githubUrl,
            language,
            tone,
            badges
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
      );

      setGeneratedReadme(response.data);
    } catch (error) {
      console.error("Error fetching README:", error);
      alert("Failed to generate README. Please try again.");
    } finally {
      setLoading(false);
      clearInterval(interval); // Stop displaying loading messages after the response is received
    }
  };

  // Predefined example GitHub URLs
  const exampleUrls = [
      "https://github.com/spring-projects/spring-boot",
    "https://github.com/facebook/react",
    "https://github.com/neovim/neovim",
    "https://github.com/cankurttekin/job-application-tracker",
  ];

  const getRepoName = (url) => {
    // Extract the repository name from the URL
    const parts = url.split("/");
    return parts[parts.length - 1]; // Last part of the URL is the repo name
  };

  return (
      <Container>
        <Title>repository to README</Title>
        <SubTitle>generate README markdowns for any github repository.</SubTitle>

        <InputContainer>
          <TextInput
              type="text"
              value={githubUrl}
              onChange={handleUrlChange}
              placeholder="Enter GitHub repository URL"
          />
          <Button onClick={handleGenerateReadme} disabled={loading}>
            {loading ? "generating" : "generate readme"}
          </Button>


        </InputContainer>
        <div style={{color: "white"}}>try these repositories:</div>
        <ExampleRepos>
          {exampleUrls.map((url, index) => (
              <ExampleButton key={index} onClick={() => setGithubUrl(url)}>
                {getRepoName(url)}
              </ExampleButton>
          ))}
        </ExampleRepos>


        {/* Customize Response Section */}
        <div style={{marginTop:"20px", color: "white"}}>customize:</div>
        <DropdownContainer>
          <DropdownLabel>
            <span>Language:</span>
            <Dropdown
                id="language"
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
            >
              <option value="English">English</option>
              <option value="Turkish">Turkish</option>
              <option value="Azerbaijani">Azerbaijani</option>
              <option value="Armenian">Armenian</option>
              <option value="Ukrainian">Ukrainian</option>
              <option value="Greek">Greek</option>
            </Dropdown>
          </DropdownLabel>

          <DropdownLabel>
            <span>Tone:</span>
            <Dropdown
                id="tone"
                value={tone}
                onChange={(e) => setTone(e.target.value)}
            >
              <option value="Neutral">Neutral</option>
              <option value="Professional">Professional</option>
              <option value="Casual">Casual</option>
            </Dropdown>
          </DropdownLabel>
          <CheckboxContainer>
            <CheckboxLabel>
              <span>Badges:</span>
              <input
                  type="checkbox"
                  checked={badges}
                  onChange={(e) => setBadges(e.target.checked)}
              />
            </CheckboxLabel>
          </CheckboxContainer>
        </DropdownContainer>


        {/* Loading message animation */}
        {loading && (
            <LoadingContainer>
              <div>{loadingMessages[loadingMessageIndex]}</div>
              <LoadingIcon/> {/* Add the spinning loader */}
            </LoadingContainer>
        )}

        {generatedReadme && (
            <MarkdownContainer>
              <EditorWrapper>
                <AceEditor
                    mode="markdown"
                    theme="github" // Updated to GitHub theme
                    value={generatedReadme}
                    onChange={(newValue) => setGeneratedReadme(newValue)}
                    name="markdown-editor"
                    editorProps={{$blockScrolling: true}}
                    width="100%"
                    height="100%"
                />
              </EditorWrapper>
              <MarkdownPreview>
                {/* Enable raw HTML rendering and support for tables and other GitHub-flavored Markdown features */}
                <ReactMarkdown rehypePlugins={[rehypeRaw]} remarkPlugins={[remarkGfm]}>
                  {generatedReadme}
                </ReactMarkdown>
              </MarkdownPreview>
            </MarkdownContainer>

        )}
        <Download>
          {/* Add a button to download the generated README.md file */}
          {generatedReadme && (
              <Button onClick={downloadFile}>download README.md</Button>
          )}
        </Download>
        {/* Footer */}
        <Footer>
          <p>reporead by cankurttekin</p>
        </Footer>
      </Container>
  );
}

export default App;
