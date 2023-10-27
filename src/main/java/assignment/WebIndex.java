package assignment;

import java.util.*;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 *
 * TODO: Implement this!
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;

    // TODO: Implement all of this! You may choose your own data structures an internal APIs.
    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).


    private HashMap<String, HashMap<Page, ArrayList<Integer>>> words = new HashMap<>();
    private HashMap<Page, ArrayList<String>> allPages = new HashMap<>();

    public void addWord(String word, Page page, int location){
        HashMap<Page, ArrayList<Integer>> wordLocations = new HashMap<>();
        ArrayList<Integer> locations = new ArrayList<>();

        if(!words.containsKey(word)){ //if the word doesn't exist in the index already
            locations.add(location);
            wordLocations.put(page, locations);
            words.put(word, wordLocations);
        }else{
            if(words.get(word).containsKey(page)){ //word exists in the index and url exists in the index already
                words.get(word).get(page).add(location); //update the locations of that word
            }else{
                locations.add(location);
                words.get(word).put(page, locations);
            }
        }
    }

    public void addPage(Page page, String word){
        ArrayList<String> wordsOfPage = new ArrayList<>();

        if(allPages.containsKey(page)){
            allPages.get(page).add(word);
        }else{
            wordsOfPage.add(word);
            allPages.put(page, wordsOfPage);
        }
    }


    public HashSet<Page> andSearch(Object wordOrPages, String word){
        HashSet<Page> andPages = new HashSet<>();

        if(!words.containsKey(word) && wordOrPages instanceof HashSet){
            return andPages;
        }else if((!words.containsKey(wordOrPages) || !words.containsKey(word)) && !(wordOrPages instanceof HashSet)){
            return andPages;
        }

        if(wordOrPages instanceof HashSet){
            HashSet<Page> pages = (HashSet<Page>) wordOrPages;

            if(pages.size() <= words.get(word).keySet().size()){
                for(Page page : pages){
                    if(words.get(word).keySet().contains(page)){
                        andPages.add(page); //finds intersection
                    }
                }
            }else{
                for(Page wordPage : words.get(word).keySet()){
                    if(pages.contains(wordPage)){
                        andPages.add(wordPage);
                    }
                }
            }
            return andPages;
        }else if(wordOrPages instanceof String){
            String word1 = (String) wordOrPages;

            if(words.get(word1).keySet().size() <= words.get(word).keySet().size()){
                //search word2 in word1's pages
                for(Page word1Page : words.get(word1).keySet()){
                    if(words.get(word).keySet().contains(word1Page)){
                        andPages.add(word1Page);
                    }
                }
            }else{
                for(Page word2Page : words.get(word).keySet()){
                    if(words.get(word1).keySet().contains(word2Page)){
                        andPages.add(word2Page);
                    }
                }
            }
            return andPages;
        }
        return andPages;
    }

    public HashSet<Page> andSearchHH(HashSet<Page> pages1, HashSet<Page> pages2){
        HashSet<Page> pagesIntersection = new HashSet<>();

        if(pages1.size() <= pages2.size()){
            for(Page page : pages1){ //O(pages1)
                if(pages2.contains(page)){
                    pagesIntersection.add(page);
                }
            }
        }else{
            for(Page page : pages2){
                if(pages1.contains(page)){
                    pagesIntersection.add(page);
                }
            }
        }
        return pagesIntersection;
    }

    public HashSet<Page> orSearch(Object wordOrPages, String word){
        HashSet<Page> orPages = new HashSet<>();

        if(!words.containsKey(word) && wordOrPages instanceof HashSet){
            return (HashSet<Page>) wordOrPages;
        }else if((!words.containsKey(wordOrPages) || !words.containsKey(word)) && !(wordOrPages instanceof HashSet)){
            return orPages;
        }

        if(wordOrPages instanceof HashSet){
            HashSet<Page> pages = (HashSet<Page>) wordOrPages;
            for(Page page : words.get(word).keySet()){
                pages.add(page);
            }
            orPages = pages;
        }else if(wordOrPages instanceof String){
            String word1 = (String) wordOrPages;
            for(Page page : words.get(word1).keySet()){
                orPages.add(page);
            }
            for(Page page : words.get(word).keySet()){
                orPages.add(page); //finds union
            }
        }
        return orPages;
    }

    public HashSet<Page> orSearchHH(HashSet<Page> pages1, HashSet<Page> pages2){
        HashSet<Page> orPages = new HashSet<>();

        for(Page page : pages1){
            orPages.add(page);
        }

        for(Page page : pages2){
            orPages.add(page);
        }

        return orPages;
    }

    public HashSet<Page> andNotSearch(Object wordOrPages, String word){
        HashSet<Page> andNotPages = new HashSet<>();

        if(!words.containsKey(word.substring(1)) && wordOrPages instanceof HashSet){ //!fewfwefwef & wordOrPages
            return (HashSet<Page>) wordOrPages;
        }else if(!(wordOrPages instanceof HashSet)){
            String word1 = (String) wordOrPages;
            if(word1.charAt(0) == '!' && word.charAt(0) == '!'){ //!word1 & !word
                if((!words.containsKey(word1.substring(1)) && !words.containsKey(word.substring(1)))) { //!the & !and
                    return entireIndex();
                }else if(!words.containsKey(word1.substring(1)) && words.containsKey(word.substring(1))){ //!wefhowejf & !the
                    return searchNotOneWord(word.substring(1));
                }else if(words.containsKey(word1.substring(1)) && !words.containsKey(word.substring(1))){ //!the & !fjskljfwef
                    return searchNotOneWord(word1.substring(1));
                }
            }else if(word1.charAt(0) == '!' && word.charAt(0) != '!'){
                if((!words.containsKey(word1.substring(1)) && !words.containsKey(word))) { //!joejfwefwf & wjeoifjwef
                    return andNotPages;
                }else if(!words.containsKey(word1.substring(1)) && words.containsKey(word)){ //!jwefwefw & the
                    return searchOneWord(word);
                } else if (words.containsKey(word1.substring(1)) && !words.containsKey(word)) { //the & !fjwefwf
                    return searchOneWord(word1);
                }
            }else if(word1.charAt(0) != '!' && word.charAt(0) == '!'){
                if((!words.containsKey(word.substring(1)) && !words.containsKey(word1))) {
                    return andNotPages;
                }else if(!words.containsKey(word.substring(1)) && words.containsKey(word1)){
                    return searchOneWord(word1);
                } else if (words.containsKey(word.substring(1)) && !words.containsKey(word1)) {
                    return searchOneWord(word);
                }
            }

        }

        //index has to contain both words or the one word

        if(wordOrPages instanceof HashSet){
            HashSet<Page> pages = (HashSet<Page>) wordOrPages;
            HashSet<Page> notWordPages = searchNotOneWord(word);

            if(pages.size() <= notWordPages.size()){
                for(Page page : pages){
                    if(notWordPages.contains(page)) andNotPages.add(page);
                }
            }else{
                for(Page page : notWordPages){
                    if(pages.contains(page)) andNotPages.add(page);
                }
            }

            return andNotPages;
        }

        if(wordOrPages instanceof String){
            String word1 = (String) wordOrPages;
            String word2 = word;

            if(word1.charAt(0) == '!' && word2.charAt(0) == '!'){
                HashSet<Page> notWord1;
                HashSet<Page> notWord2;

                notWord1 = searchNotOneWord(word1);
                notWord2 = searchNotOneWord(word2);

                if(notWord1.size() <= notWord2.size()){
                    for(Page page : notWord1){
                        if(notWord2.contains(page)){
                            andNotPages.add(page);
                        }
                    }
                }else{
                    for(Page page : notWord2){
                        if(notWord1.contains(page)){
                            andNotPages.add(page);
                        }
                    }
                }
            }else if(word1.charAt(0) == '!' && word2.charAt(0) != '!'){ //!word1 & word2
                HashSet<Page> notWord1 = searchNotOneWord(word1);

                for(Page page : notWord1){
                    if(words.get(word2).keySet().contains(page)){
                        andNotPages.add(page);
                    }
                }
            }else if(word1.charAt(0) != '!' && word2.charAt(0) == '!'){ //word1 & !word2
                HashSet<Page> notWord = searchNotOneWord(word2);

                for(Page page : notWord){
                    if(words.get(word1).keySet().contains(page)){
                        andNotPages.add(page);
                    }
                }
            }
            return andNotPages;
        }

        return andNotPages;
    }

    public HashSet<Page> orNotSearch(Object wordOrPages, String word){
        HashSet<Page> orNotPages = new HashSet();


        if(!words.containsKey(word.substring(1)) && wordOrPages instanceof HashSet){
            return entireIndex();
        }else if(!(wordOrPages instanceof HashSet)){ //first element is a word
            String word1 = (String) wordOrPages; //cast first element as word1
            if(word1.charAt(0) == '!' && word.charAt(0) == '!'){
                if((!words.containsKey(word1.substring(1)) && !words.containsKey(word.substring(1)))) { //doesn't contain either, want to return entire index (!werwjeof | !fsfef) = whole index
                    return entireIndex();
                }else if((!words.containsKey(word1.substring(1)) && words.containsKey(word.substring(1)))){ //contains the second word but not the first (!fweiojfowf | !the)
                    return entireIndex();
                }else if((words.containsKey(word1.substring(1)) && !words.containsKey(word.substring(1)))){ // (!the | !jfejwf)
                    return entireIndex();
                }
            }else if(word1.charAt(0) == '!' && word.charAt(0) != '!'){ //!word1 | word2
                if((!words.containsKey(word1.substring(1)) && !words.containsKey(word))) { //don't contain either !fjklweff | fsdfwef
                    return entireIndex();
                }else if((!words.containsKey(word1.substring(1))) && words.containsKey(word)){ //!fjwiofjwef | the
                    return entireIndex();
                }else if((words.containsKey(word1.substring(1)) && !words.containsKey(word))){ // !the | feaiojfiwf
                    return searchNotOneWord(word1);
                }
            }else if(word1.charAt(0) != '!' && word.charAt(0) == '!') { //same thing as above but switched word1 and word
                if ((!words.containsKey(word.substring(1)) && !words.containsKey(word1))) {
                    return entireIndex();
                } else if ((!words.containsKey(word.substring(1))) && words.containsKey(word1)) {
                    return entireIndex();
                } else if ((words.containsKey(word.substring(1)) && !words.containsKey(word1))) {
                    return searchNotOneWord(word);
                }
            }
        }

        //index contains the word(s) searching

        if(wordOrPages instanceof HashSet){ //hashset | !word
            HashSet<Page> pages = (HashSet<Page>) wordOrPages;
            HashSet<Page> notWord = searchNotOneWord(word.substring(1));

            for(Page page: pages){
                orNotPages.add(page);
            }
            for(Page page : notWord){
                orNotPages.add(page);
            }

            return orNotPages;
        }else if(wordOrPages instanceof String){
            String word1 = (String) wordOrPages;
            String word2 = word;

            if(word1.charAt(0) == '!' && word2.charAt(0) == '!'){
                HashSet<Page> notWord1 = searchNotOneWord(word1);
                HashSet<Page> notWord2 = searchNotOneWord(word2);

                for(Page page : notWord1){
                    orNotPages.add(page);
                }

                for(Page page: notWord2){
                    orNotPages.add(page);
                }
                return orNotPages;
            }else if(word1.charAt(0) == '!' && word2.charAt(0) != '!'){
                HashSet<Page> notWord1 = searchNotOneWord(word1);
                HashSet<Page> yesWord2 = searchOneWord(word2);

                for(Page page : notWord1){
                    orNotPages.add(page);
                }

                for(Page page : yesWord2){
                    orNotPages.add(page);
                }

                return orNotPages;
            }else if(word1.charAt(0) != '!' && word2.charAt(0) == '!'){
                HashSet<Page> notWord2 = searchNotOneWord(word2);
                HashSet<Page> yesWord1 = searchOneWord(word1);

                for(Page page : notWord2){
                    orNotPages.add(page);
                }

                for(Page page : yesWord1){
                    orNotPages.add(page);
                }

                return orNotPages;
            }

        }
        return orNotPages;
    }

    public HashSet<Page> phraseSearch(String[] phraseComponents) {
        ArrayList<String> phrase = new ArrayList<>();
        for(String string : phraseComponents){
            phrase.add(string);
        }

        Iterator<String> phraseIt = phrase.iterator();

        HashMap<Page, ArrayList<Integer>> pageMap = words.get(phraseIt.next());
        if (pageMap == null) {
            return new HashSet<>();
        }

        HashSet<Page> pageSet = new HashSet<>(pageMap.keySet());

        //finds intersection of pages
        while (phraseIt.hasNext()) {
            Map<Page, ArrayList<Integer>> nextMap = words.get(phraseIt.next());
            if (nextMap == null) {
                return new HashSet<>();
            }
            pageSet.retainAll(nextMap.keySet());
            if (pageSet.size() == 0) {
                return new HashSet<>();
            }
        }

        pageSet.removeIf((Page p) -> !hasPhrase(allPages.get(p), pageMap.get(p), phrase));

        return pageSet;
    }

    private boolean hasPhrase(ArrayList<String> wordList, ArrayList<Integer> indexList, ArrayList<String> phrase) {
        main: for(int index: indexList) {
            if(index + phrase.size() > wordList.size()) {
                return false;
            }

            index++;
            Iterator<String> it = phrase.iterator();
            it.next();

            while(it.hasNext()){
                String word = it.next();
                if(!wordList.get(index).equals(word)){
                    continue main;
                }
                index++;
            }
            return true;
        }
        return false;
    }

    public HashSet<Page> entireIndex(){
        HashSet<Page> allPages = new HashSet<>();

        for(String word : words.keySet()){
            for(Page page : words.get(word).keySet()){ //O(n^2)
                allPages.add(page);
            }
        }
        return allPages;
    }

    public HashSet<Page> searchOneWord(String word){
        HashSet<Page> oneWordPages = new HashSet<>();

        for(Page page : words.get(word).keySet()){
            oneWordPages.add(page);
        }

        return oneWordPages;
    }

    public HashSet<Page> searchNotOneWord(String wordWithNot){
        String dontWantWord = wordWithNot.substring(1);
        HashSet<Page> notOneWordPages = new HashSet<>();

        for(String word : words.keySet()){
            for(Page page: words.get(word).keySet()){ //O(n^2)
                notOneWordPages.add(page);
            }
        }

        for(Page page : words.get(dontWantWord).keySet()){
            notOneWordPages.remove(page);
        }
        return notOneWordPages;
    }
}
