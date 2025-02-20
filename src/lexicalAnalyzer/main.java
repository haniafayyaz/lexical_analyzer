package lexicalAnalyzer;

import java.io.*;
import java.util.*;

public class main {
    public static void main(String[] args) {
        List<NFA> nfas = new ArrayList<>();
        Map<String, String> regexMap = new HashMap<>();
        Map<String, NFA> nfaMap = new HashMap<>();

        regexMap.put("Identifier", RE.IDENTIFIER_REGEX);
        regexMap.put("Number", RE.NUMBER_REGEX);
        regexMap.put("Keyword", RE.KEYWORD_REGEX);
        regexMap.put("Operator", RE.OPERATOR_REGEX);
        regexMap.put("Punctuator", RE.PUNCTUATOR_REGEX);

        for (Map.Entry<String, String> entry : regexMap.entrySet()) {
            NFA nfa = new NFA();
            nfa.buildNFAFromRegex(entry.getKey(), entry.getValue());
            nfas.add(nfa);
            nfaMap.put(entry.getKey(), nfa);
        }

        NFA combinedNFA = NFA.mergeNFAs(nfas);
        if (combinedNFA == null) {
            System.err.println("Error: Combined NFA is null.");
            return;
        }
      
        combinedNFA.printNFA("Combined");
        combinedNFA.convertNFAtoDFA();
        String code = readCodeFromFile("code.txt");
        if (code.isEmpty()) {
            System.err.println("Error: Code file is empty or not found.");
            return;
        }
        combinedNFA.detectLexemeErrors(code, nfaMap);
        combinedNFA.tokenizeCode(code, nfaMap);
        combinedNFA.createSymbolTable(code, nfaMap);
    }

    private static String readCodeFromFile(String filename) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return sb.toString().trim();
    }
}