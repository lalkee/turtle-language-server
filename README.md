# LOGO Language Server

A Java-based implementation of the Language Server Protocol for the LOGO programming language. This server provides auto-completion, go-to-definition, and semantic syntax highlighting.

<img width="572" height="386" alt="Screencast from 2026-04-28 19-18-30" src="https://github.com/user-attachments/assets/85934ff0-7286-42eb-a47c-9c2a02ef7ea9" />

## Building
Building this project requires JDK 25+ and Maven 3.8.7+.

Generate the executable JAR with all dependencies:
```bash
mvn clean generate-sources package
```
   * `mvn generate-sources` will use the `antlr4-maven-plugin` to generate lexer and parser files based on Logo.g4. These generated
     files are essential for the project's compilation.
   * `mvn package` will compile the project's source code (including the
     ANTLR-generated files) and then bundle everything into a
     distributable format. This command is configured via the
     `maven-assembly-plugin` to create a "fat jar". This fat jar,
     named logo-lsp-1.0-SNAPSHOT-jar-with-dependencies.jar, contains all of the
     project's compiled classes along with all its runtime dependencies, allowing the LSP server to be run
     with a single JAR file.


To use use server, you need client connected to it.

## Client connection in IntelliJ IDEA

1.  Build the project.
2.  Install the **LSP4IJ** plugin.
3.  Navigate to `Settings` > `Languages & Frameworks` > `Language Servers`.
4.  Click on **Add Language server**:
    *   **Name**: `logo language server`
    *   **Command**: `java -cp [absolute path]/logo-lsp-1.0-SNAPSHOT-jar-with-dependencies.jar`
    example: `/home/lalke/Documents/logo-lsp/target/logo-lsp-1.0-SNAPSHOT-jar-with-dependencies.jar`
5.  Navigate to `Mappings` > `File name patterns`
6.  Click on add **Add**:
    *   **File name patterns**: `*.logo`
    *   **Language id**: `logo`


Server will now start up every time you open .logo file.


## Project structure

```text
src/main/
├── antlr4/com/lalke/antler/
│   └── Logo.g4                 
└── java/com/lalke/
    ├── LsLauncher.java 
    ├── LogoKeywords.java       
    ├── parser/
    │   ├── LowercaseCharStream.java 
    │   ├── SemanticsListener.java      
    │   ├── SymbolListener.java         
    │   ├── SymbolTable.java
    |   └── TreeUtil.java            
    └── server/
        ├── CompletionHelper.java       
        ├── DefinitionHelper.java       
        ├── LogoLanguageServer.java     
        └── LogoTextDocumentService.java
```

### Component descriptions

*   **`Logo.g4`**: The core ANTLR4 grammar file defining the entire LOGO language syntax. It specifies lexical and parser rules.
*   **`LsLauncher.java`**: The entry point for the Language Server.
*   **`LogoLanguageServer.java`**: Implements the main `LanguageServer` interface from `lsp4j`. It's responsible for declaring the server's capabilities during initialization.
*   **`LogoTextDocumentService.java`**: Handles text document-related operations, implementing the `TextDocumentService` interface. It processes `didOpen`, `didChange`, `didClose`, and `didSave` notifications from the client. On document changes, it triggers parsing, updates the internal `SymbolTable`, and orchestrates calls to `CompletionHelper`, `DefinitionHelper`, and `SemanticsListener` to provide language features. 
*   **`LogoKeywords.java`**: A utility class that rovides all LOGO keywords and command names extracted from the ANTLR Lexer's vocabulary. This is used for autocompletion and syntax highlighting.
*   **`LowercaseCharStream.java`**: A custom `CharStream` implementation that wraps the underlying character stream. Its primary function is to convert all incoming characters to lowercase when requested. This ensures LOGO's case-insensitivity.
*   **`SemanticsListener.java`**: An ANTLR `LogoBaseListener` extension responsible for generating semantic tokens. It traverses the parse tree, identifies different language elements, and translates them into LSP semantic tokens, enabling syntax highlighting.
*   **`SymbolListener.java`**: Another ANTLR `LogoBaseListener` extension used to build the `SymbolTable`. It walks the parse tree, specifically listening for entry and exit points of procedure declarations (`to ... end`), variable assignments (`make`, `localmake`, `name`), and loop variables (`for`, `dotimes`). It extracts names, their defining ranges, and their scopes, then registers them in the `SymbolTable`.
*   **`SymbolTable.java`**: A central data structure that stores information about all procedures and variables within a document. It maintains mappings of procedure names to their global `Location` and a list of `VariableInfo` objects (name, `Location` and `Range` scope) for variables. It provides methods to retrieve definitions, supporting go-to-definition, and to list variables visible at a given position for completion.
*   **`CompletionHelper.java`**: Contains the logic for providing auto-completion suggestions. It analyzes the current document content and cursor position to determine the text prefix being typed. It then queries the `SymbolTable` for relevant procedures and variables, and cross-references with `LogoKeywords`.
*   **`DefinitionHelper.java`**: Implements the "go-to-definition" functionality. Given a cursor position, it identifies the word under the cursor. It then consults the `SymbolTable` to find the `Location` of the definition for that procedure or variable (considering scope for variables) and returns it to the client.
*   **`TreeUtil.java`**: A utility class for printing ANTLR parse trees. I used it for testing.

### Libraries used

*   **ANTLR4 (ANother Tool for Language Recognition)**
    
    A lexer and parser generator that reads a grammar describing a language and generates lexer/parser for that language. My grammar is based on [this](https://github.com/antlr/grammars-v4/blob/master/logo/logo/logo.g4) file. This version lacked most of features present in Turtle Academy LOGO dialect, which I added on top of it. Besides Lexer and Parser classes generated by ANTLR can also generate helper classes used for traversing parse tree (following listener or visitor design patterns).`SymbolListener` and `SemanticsListener` use this feature to extract symbols for go-to-definition and generate semantic tokens for syntax highlighting, respectively.

*   **LSP4J (Language Server Protocol for Java)**
    
    A library that provides bindings for the Language Server Protocol. It abstracts away the complexities of JSON-RPC communication, allowing developers to focus on implementing the language server's logic rather than the protocol itself.
    The `LspLauncher` class uses LSP4J to set up the standard I/O communication channel. `LogoLanguageServer` implements LSP4J's `LanguageServer` interface to define server capabilities, and `LogoTextDocumentService` implements `TextDocumentService` to handle document-specific events (like opening, changing, saving files) and requests (like completion and definition queries).
