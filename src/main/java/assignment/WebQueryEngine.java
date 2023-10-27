package assignment;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.*;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 * TODO: Implement this!
 */
public class WebQueryEngine {
    /**
     * Returns a WebQueryEngine that uses the given Index to construct answers to queries.
     *
     * @param index The WebIndex this WebQueryEngine should use.
     * @return A WebQueryEngine ready to be queried.
     */

    private WebIndex index;

    public WebQueryEngine(WebIndex index){
        this.index = index;
    }

    public static WebQueryEngine fromIndex(WebIndex index) {
        // TODO: Implement this!
        return new WebQueryEngine(index);
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        // TODO: Implement this!
        //tokenize
        //shunting yard

        //splitting on all white space, phrases "word1      word2" should look for word1word2 , implicit ands when you split on white space, if it's a word, word in the array then there should be an and
        HashSet<Page> allPages;

        String caseInsensitiveQuery = query.toLowerCase();

        String[] tokenizedQueryBeforeImplicit = caseInsensitiveQuery.split("\\s+"); //splits on all white space, splits parenthesis but preserves
        boolean phraseStarted = false;
        StringBuilder phrase = new StringBuilder();

        ArrayList<String> tokenizedQueryWithParenthesis = new ArrayList<>();

        for(String token : tokenizedQueryBeforeImplicit){
            if(!token.isBlank()) tokenizedQueryWithParenthesis.add(token); //because there was a problem with splitting on all whitespace where it preserved some spaces
        }


        ArrayList<Integer> implicitIndices = new ArrayList<>();

        //System.out.println("tokenized query with parenthesis: " + tokenizedQueryWithParenthesis);
        boolean hasImplicit = false;
        boolean isPhrase = false;

        for(int i = 0; i < tokenizedQueryWithParenthesis.size()-1; i++){
            if(tokenizedQueryWithParenthesis.get(i).charAt(0) == '(') {
                if(tokenizedQueryWithParenthesis.get(i).charAt(1) == '\"'){
                    isPhrase = true;
                }
            } else if (tokenizedQueryWithParenthesis.get(i).charAt(0) == '\"') {
                isPhrase = true;
            }
            if(tokenizedQueryWithParenthesis.get(i).charAt(tokenizedQueryWithParenthesis.get(i).length()-1) == ')') {
                if(tokenizedQueryWithParenthesis.get(i).charAt(tokenizedQueryWithParenthesis.get(i).length()-2) == '\"'){
                    isPhrase = false;
                }
            } else if (tokenizedQueryWithParenthesis.get(i).charAt(tokenizedQueryWithParenthesis.get(i).length()-1) == '\"') {
                isPhrase = false;
            }
            if(!isOperator(tokenizedQueryWithParenthesis.get(i)) && !isOperator(tokenizedQueryWithParenthesis.get(i+1)) && !isPhrase){
                hasImplicit = true;
                implicitIndices.add(i+1);
            }
        }


        //System.out.println(implicitIndices);

        int numTimesAdded = 0;

        if(!implicitIndices.isEmpty()){
            for(Integer index : implicitIndices){
                tokenizedQueryWithParenthesis.add(index + numTimesAdded, "&");
                numTimesAdded++;
            }
        }

        StringBuilder needToSplitQuery = new StringBuilder();

        for(String string : tokenizedQueryWithParenthesis){
            needToSplitQuery.append(string);
            needToSplitQuery.append(" ");
        }

        //System.out.println("needToSplitQuery: " + needToSplitQuery.toString());

        String[] tokenizedQuery = needToSplitQuery.toString().split("\\s+|((?<=\\))|(?=\\))|((?<=\\()|(?=\\()))");

        //if there were implicit ands in the query, then add () surrounding the entire query, although now realizing this doesn't do anything pretty sure
        if(hasImplicit == true){
            ArrayList<String> queryNeedParenthesis = new ArrayList<>(List.of(tokenizedQuery));
            queryNeedParenthesis.add(0, "(");
            queryNeedParenthesis.add(queryNeedParenthesis.size(), ")");
            String[] temp = new String[queryNeedParenthesis.size()];

            for(int i = 0; i < temp.length; i++){
                temp[i] = queryNeedParenthesis.get(i);
            }

            tokenizedQuery = temp;
        }

        //System.out.println("tokenized query: " +  tokenizedQuery.toString());

        Queue<String> output = new LinkedList<>();
        Stack<String> operators = new Stack<>();

        for(String component : tokenizedQuery) {
            if (component.equals("(") || component.equals(")") || component.equals("&") || component.equals("|")) {
                switch (component) {
                    case ("&"):
                    case ("|"):
                        while (!operators.isEmpty() && hasHigherPrec(component, operators.peek())) {
                            output.add(operators.pop());
                        }
                        operators.push(component);
                        break;
                    case ("("):
                        operators.push(component);
                        break;
                    case (")"):
                        while (!operators.isEmpty() && !operators.peek().equals("(")) {
                            output.add(operators.pop());
                        }
                        operators.pop(); //removes (
                        break;
                }
            } else {
                if (!component.isBlank()) { //for some reasons some spaces remain after splitting on regex?
                    if (component.startsWith("\"") && !component.endsWith("\"")) { //beginning of phrase, "word
                        phraseStarted = true;
                        phrase.append(component.substring(1));
                    } else if (component.startsWith("\"") && component.endsWith("\"")) {
                        if(!component.equals("\"")){
                            output.add(component.substring(1, component.length()-1));
                        }
                    } else if (phraseStarted == true && !component.endsWith("\"")) { //middle of a phrase, "1 word 3"
                        phrase.append(" ");
                        phrase.append(component);
                    } else if (phraseStarted == true && component.endsWith("\"")) { //end of a phrase, "1 2 word"
                        phraseStarted = false;
                        phrase.append(" ");
                        phrase.append(component, 0, component.length() - 1);
                        output.add(phrase.toString());
                        phrase = new StringBuilder();
                    } else {
                        output.add(component);
                    }
                }
            }

            //System.out.println("component: " + component);
            //System.out.println("operator: " + operators);
            //System.out.println("output: " + output);
        }

        allPages = evaluateOutput(output);
        //System.out.println("number of pages: " + allPages.size());
        return allPages;
        //return new HashSet<>();
        //test: create a webcrawler object, object.main(), to get index do
    }

    private boolean hasHigherPrec(String operator, String topOfStack){
        if((operator.equals("&") && topOfStack.equals("&")) || (operator.equals("|") && topOfStack.equals("&"))){
            return true;
        }else{
            return false;
        }
    }

    private HashSet<Page> evaluateOutput(Queue<String> outputQ){
        ArrayList<String> output = new ArrayList<>(outputQ);
        HashSet<Page> URLs = new HashSet<>();
        Stack<String> operands = new Stack<>();
        ArrayList<HashSet<Page>> Results = new ArrayList<>();

        if(output.size() == 0) return URLs;
        if(output.size() == 1 && (output.get(0).charAt(0) != '!') && !output.get(0).contains(" ")) return index.searchOneWord(output.get(0));
        if(output.size() == 1 && (output.get(0).charAt(0) == '!') && !output.get(0).contains(" ")) return index.searchNotOneWord(output.get(0));
        if(output.size() == 1 && output.get(0).contains(" ")) return index.phraseSearch(output.get(0).split(" "));

        for (String str : output) {
            if (isOperator(str)) {
                switch (str) {
                    case ("&"):
                        if(operands.size() == 1){ //single operand
                            if(operands.peek().contains(" ")){
                                HashSet<Page> phrase = index.phraseSearch(operands.pop().split(" "));
                                if(!URLs.isEmpty()){ //there have been previous operations
                                    URLs = index.andSearchHH(URLs, phrase);
                                    Results.add(URLs);
                                }else{
                                    URLs = phrase;
                                    Results.add(phrase);
                                }
                            }else{
                                if(operands.peek().charAt(0) == '!'){
                                    URLs = index.andNotSearch(URLs, operands.pop());
                                    Results.add(URLs);
                                }else{
                                    URLs = index.andSearch(URLs, operands.pop());
                                    Results.add(URLs);
                                }
                            }
                        }else if(operands.size() == 0) {
                            URLs = index.andSearchHH(Results.get(Results.size()-2), Results.get(Results.size()-1));
                            Results.add(URLs);
                        }else {
                            String word2 = operands.pop(); //second added word would be at the top of the stack (most recent word)
                            String word1 = operands.pop();

                            if(word1.contains(" ") || word2.contains(" ")){
                                if(word1.contains(" ") && !word2.contains(" ")){ //"phrase1 phrase2" & word2
                                    String[] phraseComponents = word1.split(" ");
                                    HashSet<Page> word1URLs = index.phraseSearch(phraseComponents);
                                    URLs = index.andSearch(word1URLs, word2);
                                    Results.add(URLs);
                                }else if(!word1.contains(" ") && word2.contains(" ")){
                                    String[] phraseComponents = word2.split(" ");
                                    HashSet<Page> word2URLs = index.phraseSearch(phraseComponents);
                                    URLs = index.andSearch(word2URLs, word1);
                                    Results.add(URLs);
                                }else{
                                    String[] phraseComponents1 = word1.split(" ");
                                    String[] phraseComponents2 = word2.split(" ");
                                    HashSet<Page> word1URLs = index.phraseSearch(phraseComponents1);
                                    HashSet<Page> word2URLs = index.phraseSearch(phraseComponents2);
                                    URLs = index.andSearchHH(word1URLs, word2URLs);
                                    Results.add(URLs);
                                }
                            }else{
                                if(word1.charAt(0) == '!' || word2.charAt(0) == '!'){
                                    URLs = index.andNotSearch(word1, word2);
                                    Results.add(URLs);
                                }else{
                                    URLs = index.andSearch(word1, word2);
                                    Results.add(URLs);
                                }
                            }
                        }
                        break;
                    case("|"):
                        if(operands.size() == 1){
                            if(operands.peek().contains(" ")){
                                HashSet<Page> phrase = index.phraseSearch(operands.pop().split(" "));
                                if(!URLs.isEmpty()){ //there have been previous operations
                                    URLs = index.orSearchHH(URLs, phrase);
                                    Results.add(URLs);
                                }else{
                                    URLs = phrase;
                                    Results.add(phrase);
                                }
                            }else{
                                if(operands.peek().charAt(0) == '!'){
                                    URLs = index.orNotSearch(URLs, operands.pop());
                                    Results.add(URLs);
                                }else{
                                    URLs = index.orSearch(URLs, operands.pop());
                                    Results.add(URLs);
                                }
                            }
                        }else if(operands.size() == 0) {
                            URLs = index.orSearchHH(Results.get(Results.size()-2), Results.get(Results.size()-1));
                            Results.add(URLs);
                        } else {
                            String word2 = operands.pop(); //second added word would be at the top of the stack (most recent word)
                            String word1 = operands.pop();

                            if(word1.contains(" ") || word2.contains(" ")){
                                if(word1.contains(" ") && !word2.contains(" ")){ //"phrase1 phrase2" & word2
                                    String[] phraseComponents = word1.split(" ");
                                    HashSet<Page> word1URLs = index.phraseSearch(phraseComponents);
                                    URLs = index.orSearch(word1URLs, word2);
                                    Results.add(URLs);
                                }else if(!word1.contains(" ") && word2.contains(" ")){
                                    String[] phraseComponents = word2.split(" ");
                                    HashSet<Page> word2URLs = index.phraseSearch(phraseComponents);
                                    URLs = index.orSearch(word2URLs, word1);
                                    Results.add(URLs);
                                }else{
                                    String[] phraseComponents1 = word1.split(" ");
                                    String[] phraseComponents2 = word2.split(" ");
                                    HashSet<Page> word1URLs = index.phraseSearch(phraseComponents1);
                                    HashSet<Page> word2URLs = index.phraseSearch(phraseComponents2);
                                    URLs = index.orSearchHH(word1URLs, word2URLs);
                                    Results.add(URLs);
                                }
                            }else{
                                if(word1.charAt(0) == '!' || word2.charAt(0) == '!'){
                                    URLs = index.orNotSearch(word1, word2);
                                    Results.add(URLs);
                                }else{
                                    URLs = index.orSearch(word1, word2);
                                    Results.add(URLs);
                                }
                            }
                        }
                        break;
                    }
            }else{
                operands.push(str);
            }

        }
        return URLs;
    }

    private boolean isOperator(String str){
        if(str.equals("&") || str.equals("|")) return true;
        return false;
    }





}
