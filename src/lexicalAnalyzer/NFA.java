package lexicalAnalyzer;

import java.io.*;
import java.util.*;

class NFA {
    public Set<Integer> states;
    private Map<Integer, Map<Character, Set<Integer>>> transitions;
    public int startState;
    public Set<Integer> finalStates;
    private static int stateCounter = 0; // Unique state counter
    private static final Map<String, NFA> nfaMap = new HashMap<>();

    public NFA() {
        states = new HashSet<>();
        transitions = new HashMap<>();
        finalStates = new HashSet<>();
    }

    public void buildNFAFromRegex(String regexName, String pattern) {
        int start = stateCounter++;
        states.add(start);

        switch (regexName) {
            case "Identifier":
                addSingleFinalStateTransition(start, 'a', 'z');
                break;
            case "Number":
                int numberState = stateCounter++;
                states.add(numberState);
                addCharacterRangeTransition(start, numberState, '0', '9', true);
                finalStates.add(numberState); 

                int decimalPointState = stateCounter++;
                states.add(decimalPointState);
                addTransition(numberState, decimalPointState, '.'); 
                
                int decimalState = stateCounter++;
                states.add(decimalState);
                addCharacterRangeTransition(decimalPointState, decimalState, '0', '9', true);
                finalStates.add(decimalState); 
                break;
            case "Keyword":
                addWordTransition(start, new String[]{"for", "if", "else", "return", "void", "main", "int", "bool", "char", "float"});
                break;
            case "Operator":
                int operatorFinalState = stateCounter++;
                states.add(operatorFinalState);
                addTransition(start, operatorFinalState, '+', '-', '*', '/', '%', '=');
                finalStates.add(operatorFinalState); 
                break;
            case "Punctuator":
                int punctuatorFinalState = stateCounter++;
                states.add(punctuatorFinalState);
                addTransition(start, punctuatorFinalState, ',', ';', '{', '}', '[', ']', '(', ')');
                finalStates.add(punctuatorFinalState); 
                break;
        }

        startState = start;
    }

    private void addWordTransition(int start, String[] words) {
        for (String word : words) {
            int current = start;
            for (char c : word.toCharArray()) {
                int next = stateCounter++;
                states.add(next);
                addTransition(current, next, c);
                current = next;
            }
            finalStates.add(current); 
        }
    }

    private void addSingleFinalStateTransition(int from, char start, char end) {
        int finalState = stateCounter++;
        states.add(finalState);
        finalStates.add(finalState);

        for (char c = start; c <= end; c++) {
            addTransition(from, finalState, c);
            addTransition(finalState, finalState, c); 
        }
    }

    private void addCharacterRangeTransition(int from, int to, char start, char end, boolean repeat) {
        for (char c = start; c <= end; c++) {
            addTransition(from, to, c);
            if (repeat) {
                addTransition(to, to, c); 
            }
        }
    }

    private void addTransition(int from, int to, char... symbols) {
        states.add(to);
        transitions.putIfAbsent(from, new HashMap<>());

        for (char symbol : symbols) {
            transitions.get(from).computeIfAbsent(symbol, k -> new HashSet<>()).add(to);
        }
    }

    public boolean isAccepted(String token) {
        Set<Integer> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (char c : token.toCharArray()) {
            Set<Integer> nextStates = new HashSet<>();
            for (int state : currentStates) {
                if (transitions.containsKey(state) && transitions.get(state).containsKey(c)) {
                    nextStates.addAll(transitions.get(state).get(c));
                }
            }
            currentStates = nextStates;
        }

        for (int state : currentStates) {
            if (finalStates.contains(state)) {
                return true;
            }
        }

        return false;
    }

    public String[] tokenizeCode(String code, Map<String, NFA> nfaMap) {
        code = code.replaceAll("//.*", " "); 
        code = code.replaceAll("(?s)/\\.?\\*/", " ");
        String[] tokens = code.split("\\s+|(?=[{}()=;,])|(?<=[{}()=;,])");
        List<String> validTokens = new ArrayList<>();
        int totalTokens = 0;

        System.out.println("\nTokenized Code:");
        System.out.println("==============================");

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            String tokenType = null;

            if (nfaMap.get("Keyword").isAccepted(token)) {
                tokenType = "Keyword";
            } else if (nfaMap.get("Number").isAccepted(token)) {
                tokenType = "Number";
            } else if (nfaMap.get("Operator").isAccepted(token)) {
                tokenType = "Operator";
            } else if (nfaMap.get("Punctuator").isAccepted(token)) {
                tokenType = "Punctuator";
            } else if (nfaMap.get("Identifier").isAccepted(token)) {
                tokenType = "Identifier";  
            }

            if (tokenType != null) {
                System.out.println("Token: " + token + ", Type: " + tokenType);
                validTokens.add(token);
                totalTokens++;
            }
        }

        System.out.println("==============================");
        System.out.println("Total Number of Tokens: " + totalTokens);
        return validTokens.toArray(new String[0]);
    }


    public void printNFA(String regexName) {
        System.out.println("\n==============================");
        System.out.println("  NFA for: " + regexName);
        System.out.println("==============================");
        System.out.println("Total States: " + states.size());
        System.out.println("Start State: " + startState);
        System.out.println("Final States: " + finalStates);
        System.out.println("------------------------------");
        System.out.println("Transitions:");

        List<Integer> sortedStates = new ArrayList<>(states);
        Collections.sort(sortedStates);

        for (int state : sortedStates) {
            if (transitions.containsKey(state)) {
                for (var trans : transitions.get(state).entrySet()) {
                    char symbol = trans.getKey();
                    List<Integer> targets = new ArrayList<>(trans.getValue());
                    Collections.sort(targets);
                    
                    System.out.println("  State " + state + " --(" + symbol + ")--> " + targets);
                }
            }
        }

        System.out.println("==============================\n");
    }

    public static NFA mergeNFAs(List<NFA> nfas) {
        NFA combinedNFA = new NFA();
        int newStart = stateCounter++;

        combinedNFA.states.add(newStart);
        combinedNFA.startState = newStart;

        for (NFA nfa : nfas) {
            combinedNFA.states.addAll(nfa.states);
            combinedNFA.finalStates.addAll(nfa.finalStates);
            combinedNFA.transitions.putAll(nfa.transitions);

            combinedNFA.transitions.putIfAbsent(newStart, new HashMap<>());
            combinedNFA.transitions.get(newStart).computeIfAbsent('ε', k -> new HashSet<>()).add(nfa.startState);
        }

        return combinedNFA;
    }
    

    public void createSymbolTable(String code, Map<String, NFA> nfaMap) {
        String[] lines = code.split("\n");
        boolean isLocal = false;
        boolean foundVariables = false;
        boolean inMultiLineComment = false;

        System.out.println("\nSymbol Table:");
        System.out.println("==========================================");
        System.out.printf("%-15s %-10s %-10s%n", "Identifier", "Datatype", "Scope");
        System.out.println("------------------------------------------");

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("/*")) {
                inMultiLineComment = true;
                System.out.println("Multi-line Comment: " + line);
                continue;
            }
            
            if (inMultiLineComment) {
                System.out.println(line);
                if (line.endsWith("*/")) {
                    inMultiLineComment = false;
                }
                continue;
            }
            
            if (line.startsWith("//")) {
                System.out.println("Single-line Comment: " + line);
                continue;
            }
            
            String[] tokens = line.split("\\s+|(?=[{}()=;,])|(?<=[{}()=;,])");

            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];

                if (token.equals("main") && i + 1 < tokens.length && tokens[i + 1].equals("(")) {
                    isLocal = true;
                }

                if (token.equals("int") || token.equals("float") || token.equals("bool") || token.equals("char")) {
                    String datatype = token;
                    int j = i + 1;

                    while (j < tokens.length) {
                        String nextToken = tokens[j];
                        if (nextToken.equals(";")) break;
                        if (nextToken.equals("=") || nextToken.equals(",")) {
                            j++;
                            continue;
                        }

                        if (nfaMap.get("Identifier").isAccepted(nextToken) && !nextToken.equals("main")) {
                            foundVariables = true;
                            String scope = isLocal ? "Local" : "Global";
                            System.out.printf("%-15s %-10s %-10s%n", nextToken, datatype, scope);
                        }
                        j++;
                    }
                }
            }
        }

        if (!foundVariables) {
            System.out.println("No variables found.");
        }

        System.out.println("==========================================");
    }

    
    public void detectLexemeErrors(String code, Map<String, NFA> nfaMap) {
        String[] lines = code.split("\\n"); // Split code into lines

        System.out.println("\nLexeme Errors:");
        System.out.println("==========================================");
        System.out.printf("%-10s %-15s %-20s%n", "Line No", "Unidentified Token", "Reason");
        System.out.println("------------------------------------------");

        boolean foundErrors = false;
        boolean insideBlockComment = false; 

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim(); 

            if (line.startsWith("//")) {
                continue;
            }
            if (line.contains("/*")) {
                insideBlockComment = true;
            }

            if (insideBlockComment) {
                if (line.contains("*/")) {
                    insideBlockComment = false; 
                }
                continue;
            }

            String[] tokens = line.split("\\s+|(?=[{}()=;,])|(?<=[{}()=;,])");

            for (String token : tokens) {
                if (token.isEmpty()) continue;

                boolean matched = false;

                if (nfaMap.get("Keyword").isAccepted(token) ||
                    nfaMap.get("Number").isAccepted(token) ||
                    nfaMap.get("Operator").isAccepted(token) ||
                    nfaMap.get("Punctuator").isAccepted(token) ||
                    nfaMap.get("Identifier").isAccepted(token)) {
                    matched = true;
                }

                if (!matched) {
                    foundErrors = true;
                    System.out.printf("%-10d %-15s %-20s%n", (lineNumber + 1), token, "Unrecognized token");
                }
            }
        }

        if (!foundErrors) {
            System.out.println("No lexeme errors found.");
        }

        System.out.println("==========================================");
    }

    private Set<Integer> epsilonClosure(Set<Integer> states) {
        Stack<Integer> stack = new Stack<>();
        Set<Integer> closure = new HashSet<>(states);
        
        for (int state : states) {
            stack.push(state);
        }
        
        while (!stack.isEmpty()) {
            int state = stack.pop();
            if (transitions.containsKey(state) && transitions.get(state).containsKey('ε')) {
                for (int nextState : transitions.get(state).get('ε')) {
                    if (closure.add(nextState)) {
                        stack.push(nextState);
                    }
                }
            }
        }
        return closure;
    }

    public void convertNFAtoDFA() {
       
        Map<Set<Integer>, Integer> dfaStates = new HashMap<>();
        Queue<Set<Integer>> queue = new LinkedList<>();
        int dfaStateCounter = 0;
        
        Set<Integer> startSet = epsilonClosure(Set.of(startState));
        dfaStates.put(startSet, dfaStateCounter++);
        queue.add(startSet);
        
        Map<Integer, Map<Character, Integer>> dfaTransitions = new HashMap<>();
        Set<Integer> dfaFinalStates = new HashSet<>();
        
        if (!Collections.disjoint(startSet, finalStates)) {
            dfaFinalStates.add(0);
        }
        
        while (!queue.isEmpty()) {
            Set<Integer> currentSet = queue.poll();
            int dfaState = dfaStates.get(currentSet);
            dfaTransitions.putIfAbsent(dfaState, new HashMap<>());
            
            Map<Character, Set<Integer>> transitionMap = new HashMap<>();
            
            for (int state : currentSet) {
                if (transitions.containsKey(state)) {
                    for (var entry : transitions.get(state).entrySet()) {
                        char symbol = entry.getKey();
                        Set<Integer> targetStates = entry.getValue();
                        
                        transitionMap.putIfAbsent(symbol, new HashSet<>());
                        transitionMap.get(symbol).addAll(targetStates);
                    }
                }
            }
            
            for (var entry : transitionMap.entrySet()) {
                char symbol = entry.getKey();
                Set<Integer> nextStateSet = epsilonClosure(entry.getValue());
                
                if (!dfaStates.containsKey(nextStateSet)) {
                    dfaStates.put(nextStateSet, dfaStateCounter);
                    queue.add(nextStateSet);
                    
                    if (!Collections.disjoint(nextStateSet, finalStates)) {
                        dfaFinalStates.add(dfaStateCounter);
                    }
                    
                    dfaStateCounter++;
                }
                
                dfaTransitions.get(dfaState).put(symbol, dfaStates.get(nextStateSet));
            }
        }

        minimizeDFA(dfaTransitions, dfaFinalStates);
    }

    
    private void printDFA(Map<Integer, Integer> stateMapping, Map<Integer, Map<Character, Integer>> dfaTransitions, Set<Integer> dfaFinalStates) {
        System.out.println("Total DFA States: " + stateMapping.size());
       /* System.out.println("Start State: 0");
        System.out.println("Final States: " + dfaFinalStates);
        System.out.println("------------------------------");
        System.out.println("DFA Transitions:");
        
        for (var entry : dfaTransitions.entrySet()) {
            int fromState = entry.getKey();
            for (var trans : entry.getValue().entrySet()) {
                char symbol = trans.getKey();
                int toState = trans.getValue();
                System.out.println("  State " + fromState + " --(" + symbol + ")--> " + toState);
            }
        }
        */
        System.out.println("==============================\n");
    }

    private void minimizeDFA(Map<Integer, Map<Character, Integer>> dfaTransitions, Set<Integer> dfaFinalStates) {
        System.out.println("\n==============================");
        System.out.println("        DFA Minimization       ");
        System.out.println("==============================");
        
        Set<Integer> allStates = new HashSet<>(dfaTransitions.keySet());
        Set<Integer> nonFinalStates = new HashSet<>(allStates);
        nonFinalStates.removeAll(dfaFinalStates);

        List<Set<Integer>> partitions = new ArrayList<>();
        partitions.add(new HashSet<>(dfaFinalStates));
        partitions.add(nonFinalStates);

        boolean changed = true;

        while (changed) {
            changed = false;
            List<Set<Integer>> newPartitions = new ArrayList<>();

            for (Set<Integer> group : partitions) {
                Map<Map<Character, Integer>, Set<Integer>> transitionGroups = new HashMap<>();

                for (int state : group) {
                    Map<Character, Integer> transitions = dfaTransitions.getOrDefault(state, new HashMap<>());

                    transitionGroups.putIfAbsent(transitions, new HashSet<>());
                    transitionGroups.get(transitions).add(state);
                }

                newPartitions.addAll(transitionGroups.values());
            }

            if (newPartitions.size() > partitions.size()) {
                partitions = newPartitions;
                changed = true;
            }
        }

        Map<Integer, Integer> stateMapping = new HashMap<>();
        int newStateCounter = 0;

        for (Set<Integer> group : partitions) {
            int representative = group.iterator().next();
            for (int state : group) {
                stateMapping.put(state, newStateCounter);
            }
            newStateCounter++;
        }

        Map<Integer, Map<Character, Integer>> minimizedTransitions = new HashMap<>();
        Set<Integer> minimizedFinalStates = new HashSet<>();

        for (var entry : dfaTransitions.entrySet()) {
            int originalState = entry.getKey();
            int newState = stateMapping.get(originalState);

            minimizedTransitions.putIfAbsent(newState, new HashMap<>());

            for (var trans : entry.getValue().entrySet()) {
                char symbol = trans.getKey();
                int targetState = stateMapping.get(trans.getValue());

                minimizedTransitions.get(newState).put(symbol, targetState);
            }

            if (dfaFinalStates.contains(originalState)) {
                minimizedFinalStates.add(newState);
            }
        }

        System.out.println("\nAfter Minimization:");
        printDFA(stateMapping, minimizedTransitions, minimizedFinalStates);
    }

    

}