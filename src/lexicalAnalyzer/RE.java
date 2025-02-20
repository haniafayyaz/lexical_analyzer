package lexicalAnalyzer;
import java.util.regex.*;

public class RE {
    
    public static final String IDENTIFIER_REGEX = "^[a-z]+$";
    public static final String NUMBER_REGEX = "^?\\d+(\\.\\d{1,5})?$";
    public static final String KEYWORD_REGEX = "^(for|if|else|return|void|main|int|bool|float|char)$";
    public static final String OPERATOR_REGEX = "^[+\\-*/%=]$";
    public static final String PUNCTUATOR_REGEX = "^[,;{}\\[\\]()]$";
}
