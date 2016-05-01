import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class MainDriver {
    private static Sentence currentQuery;
    private static String sqlStatement;
    private static String answer;
    private static StanfordCoreNLP pipeline;
    private static DatabaseAccess database = new DatabaseAccess();
  
    // Initialize the Stanford NLP pipeline
    private static void initPipeline() {
        System.out.println("Waking up...");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        pipeline = new StanfordCoreNLP(props);
    }
    
    public static void main(String[] args) throws SQLException {
        //Initialize Stanford NLP tools
        initPipeline();
        
        // Greeting
        System.out.println("Hello! My name is Watson Jr.\n");
        
        // Capture input and process query
        String input;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Ask a question or type 'q' to quit.");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("You forgot to type something.");
                continue;
            }
            if (!input.equalsIgnoreCase("q")) {
                currentQuery = new Sentence(pipeline, input);
                createSQLStatement();
                if (!sqlStatement.equals(";")) {
                    createAnswer();
                    printOutputs();
                }
            }
        } while(!input.equalsIgnoreCase("q"));
        
        scanner.close();
        System.out.println("Goodbye.");
    } // end main()
    
    public static void createSQLStatement() throws SQLException {
        SqlStatementBuilder ssb = new SqlStatementBuilder(currentQuery);
        sqlStatement = ssb.createSqlStatement();
    } // end createSQLStatement()
    
    public static void createAnswer() {
        String result = null;
        try {
            // Check if sql statement is valid
            if (sqlStatement.equals("")){
                result = sqlStatement;
            } else{
                result = database.processQuery(sqlStatement);
            }
        }
        catch (ClassNotFoundException e) {
            System.out.println(e.getMessage()); 
        }
        
        // If true/false answer        
        if(currentQuery.isClosedQuestion()){
            try {
                if (Integer.parseInt(result.trim()) > 0){
                    answer = "Yes";
                } else{
                    answer = "No";
                }
            } catch (Exception e){
                answer = "Unable to find answer.";
            } 

        } else{ // Wh- answer
            try {
                answer = result.trim();
            } catch (Exception e) {
                answer = "Unable to find answer.";
            }
        }
    } // end createAnswer()
    
    public static void printOutputs(){
        System.out.println("<QUERY>\n" + currentQuery.getRawSentence());
        System.out.println("<SQL>\n" + sqlStatement);
        System.out.println("<ANSWER>\n" + answer);
        System.out.println();
    } // end printOutputs()
}
