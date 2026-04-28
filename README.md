# LOGO Language Server

A Java-based implementation of the Language Server Protocol for the LOGO programming language. This server provides auto-completion, go-to-definition, and semantic syntax highlighting.

## Build & Execution

Building this project requires JDK 25+ and Maven 3.8.7+.

#### Build
Generate the executable JAR with all dependencies:
```bash
mvn clean generate-sources package
```

#### Run
The server communicates via Standard Input/Output (Stdio):
```bash
java -cp target/logo-lsp-1.0-SNAPSHOT-jar-with-dependencies.jar com.lalke.LspLauncher
```


## Client connection in IntelliJ IDEA


1.  Install the **LSP4IJ** plugin.
2.  Navigate to `Settings` > `Language Server Protocol` > `Server Definitions`.
3.  Add a new **Executable** definition:
    *   **Extension**: `.logo`
    *   **Command**: `java -cp /absolute/path/to/logo-lsp-1.0-SNAPSHOT-jar-with-dependencies.jar com.lalke.LspLauncher`

Server will now start up every time you open .logo file.


## Project Structure

```text
src/main/
├── antlr4/com/lalke/antler/
│   └── Logo.g4                 
└── java/com/lalke/
    ├── LspLauncher.java 
    ├── LogoKeywords.java       
    ├── parser/
    │   ├── CaseChangingCharStream.java 
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

### Component Descriptions

*   **`Logo.g4`**: The core ANTLR4 grammar file defining the entire LOGO language syntax. It specifies lexical and parser rules. Grammar is based on [this](https://github.com/antlr/grammars-v4/blob/master/logo/logo/logo.g4) file. This version lacked most of features present in Turtle Academy LOGO dialect, wich I added on top of it.
*   **`LspLauncher.java`**: The entry point for the Language Server.
*   **`LogoLanguageServer.java`**: Implements the main `LanguageServer` interface from `lsp4j`. It's responsible for declaring the server's capabilities during initialization.
*   **`LogoTextDocumentService.java`**: Handles text document-related operations, implementing the `TextDocumentService` interface. It processes `didOpen`, `didChange`, `didClose`, and `didSave` notifications from the client. On document changes, it triggers parsing, updates the internal `SymbolTable`, and orchestrates calls to `CompletionHelper`, `DefinitionHelper`, and `SemanticsListener` to provide language features.
*   **`LogoKeywords.java`**: A utility class that rovides all LOGO keywords and command names extracted from the ANTLR Lexer's vocabulary. This is used for autocompletion and syntax highlighting.
*   **`CaseChangingCharStream.java`**: A custom `CharStream` implementation that wraps the underlying character stream. Its primary function is to convert all incoming characters to lowercase when requested. This ensures LOGO's case-insensitivity.
*   **`SemanticsListener.java`**: An ANTLR `LogoBaseListener` extension responsible for generating semantic tokens. It traverses the parse tree, identifies different language elements, and translates them into LSP semantic tokens, enabling syntax highlighting.
*   **`SymbolListener.java`**: Another ANTLR `LogoBaseListener` extension used to build the `SymbolTable`. It walks the parse tree, specifically listening for entry and exit points of procedure declarations (`to ... end`), variable assignments (`make`, `localmake`, `name`), and loop variables (`for`, `dotimes`). It extracts names, their defining ranges, and their scopes, then registers them in the `SymbolTable`.
*   **`SymbolTable.java`**: A central data structure that stores information about all procedures and variables within a document. It maintains mappings of procedure names to their global `Location` and a list of `VariableInfo` objects (name, `Location` and `Range` scope) for variables. It provides methods to retrieve definitions, supporting go-to-definition, and to list variables visible at a given position for completion.
*   **`CompletionHelper.java`**: Contains the logic for providing auto-completion suggestions. It analyzes the current document content and cursor position to determine the text prefix being typed. It then queries the `SymbolTable` for relevant procedures and variables, and cross-references with `LogoKeywords`.
*   **`DefinitionHelper.java`**: Implements the "go-to-definition" functionality. Given a cursor position, it identifies the word under the cursor. It then consults the `SymbolTable` to find the `Location` of the definition for that procedure or variable (considering scope for variables) and returns it to the client.
*   **`TreeUtil.java`**: A utility class for printing ANTLR parse trees. Used for testing.