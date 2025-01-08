# REPOREAD

A tool to generate README.md files for GitHub repositories using AI. This readme generated using REPOREAD.

## Project Description

This repository contains the source code for a web application that automatically generates comprehensive `README.md` files for GitHub repositories. The application consists of a React-based frontend and a Spring Boot-based backend. It leverages the Gemini API to generate the README content based on the repository's structure and files.

## Table of Contents

- [Installation and Setup](#installation-and-setup)
- [Running the Project](#running-the-project)
- [Dependencies and Tools](#dependencies-and-tools)
- [Configuration File Options](#configuration-file-options)
- [API Reference](#api-reference)
- [License](#license)


## Installation and Setup

### Backend

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/cankurttekin/reporead.git
    ```
2.  **Navigate to the backend directory:**
    ```bash
    cd reporead/backend
    ```
3.  **Set up environment variables:**
    *   Create a `.env` file in the `backend` directory.
    *   Add your Gemini API key:
        ```
        GEMINI_API_KEY=your_gemini_api_key
        ```

4.  **Build the backend application**
   ```bash
     ./mvnw clean install
   ```
### Frontend

1.  **Navigate to the frontend directory:**
    ```bash
    cd ../frontend
    ```
2.  **Install dependencies:**
    ```bash
    npm install
    ```

## Running the Project

### Backend

1. **Navigate to the backend directory**
    ```bash
    cd reporead/backend
    ```
2.  **Run the Spring Boot application:**
    ```bash
    ./mvnw spring-boot:run
    ```
    The backend server will start on the port specified in `backend/src/main/resources/application.properties` (default `8443`).

### Frontend

1.  **Navigate to the frontend directory:**
    ```bash
     cd reporead/frontend
    ```
2.  **Start the React application:**
    ```bash
    npm start
    ```
    The frontend application will start in development mode, accessible at `http://localhost:3000`.

## Dependencies and Tools

### Backend

-   **Java 17**: Required for running the Spring Boot application.
-   **Maven**: Used for project management and dependency resolution.
-   **Spring Boot**: Framework for building the backend application.
-   **Spring Web**: Module for building web applications with Spring.
-   **Spring Webflux**: For building reactive web applications.
-   **Lombok**: Library to reduce boilerplate code.
-   **JSON**: Java library for handling JSON data.
-   **OkHttp**: HTTP client for making requests to the Gemini API.
-   **Spring Security**: To configure security for the backend API.

### Frontend

-   **Node.js**: JavaScript runtime for running the frontend application.
-   **npm**: Package manager for installing frontend dependencies.
-   **React**: JavaScript library for building user interfaces.
-   **Axios**: HTTP client for making requests to the backend API.
-   **React Markdown**: Library for rendering Markdown content in React.
-    **Rehype Raw**: Plugin to allow raw HTML in react markdown.
-   **Remark Gfm**: Plugin to support GitHub Flavored Markdown syntax.
-   **React Ace**:  Code editor component for markdown editing
-   **Styled Components**: Library for styling React components with CSS-in-JS.
-  **Web Vitals**: For measuring web performance metrics


## Configuration File Options

### `backend/.env`

-   `GEMINI_API_KEY`: Your API key for accessing the Gemini API.

### `backend/src/main/resources/application.properties`

-   `spring.application.name`: Sets the name of the application.
-   `gemini.api.key`: Holds the Gemini API key, typically loaded from environment variables.
   -  `ALLOWED_ORIGINS` Configures the CORS configuration for the backend application

## API Reference

### `POST /api/generate-readme`

| Parameter | Type    | Description                                         |
| :-------- | :------ | :-------------------------------------------------- |
| `repoUrl` | String  | The URL of the GitHub repository to generate a README for. |
| `tone`   | String | The tone of the generated README (e.g., neutral, professional, casual).|
| `language`   | String | The language of the generated README (e.g., English, Turkish).|
|`badges` | Boolean | Optional field that determines if badges should be included|

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).


