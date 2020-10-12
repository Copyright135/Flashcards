package flashcards;

public class Flashcard {
    String term;
    String definition;
    int wrongAnswers;

    public Flashcard(String term, String definition) {
        this.term = term;
        this.definition = definition;
        this.wrongAnswers = 0;
    }

    public String getTerm() { return term; }

    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }

    public void setDefinition(String definition) { this.definition = definition; }

    public int getWrongAnswers() { return wrongAnswers; }

    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }
}
