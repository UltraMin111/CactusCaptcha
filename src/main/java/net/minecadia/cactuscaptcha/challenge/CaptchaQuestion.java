package net.minecadia.cactuscaptcha.challenge;

import java.util.List;

/**
 * Represents a single text-logic captcha question with multiple choice answers.
 * Each question has a prompt, exactly 4 choices, and one correct answer.
 */
public class CaptchaQuestion {
    
    private final String prompt;
    private final List<String> choices;
    private final int correctIndex;
    
    /**
     * Creates a new captcha question.
     * 
     * @param prompt The question text to display to the player
     * @param choices List of exactly 4 answer choices
     * @param correctIndex The index (0-3) of the correct answer in the choices list
     * @throws IllegalArgumentException if choices doesn't contain exactly 4 elements
     * @throws IllegalArgumentException if correctIndex is not between 0-3
     */
    public CaptchaQuestion(String prompt, List<String> choices, int correctIndex) {
        if (choices == null || choices.size() != 4) {
            throw new IllegalArgumentException("Choices must contain exactly 4 elements");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("Correct index must be between 0-3");
        }
        
        this.prompt = prompt;
        this.choices = choices;
        this.correctIndex = correctIndex;
    }
    
    /**
     * Gets the question prompt text.
     * 
     * @return The question prompt
     */
    public String getPrompt() {
        return prompt;
    }
    
    /**
     * Gets the list of answer choices.
     * 
     * @return List of 4 answer choices
     */
    public List<String> getChoices() {
        return choices;
    }
    
    /**
     * Gets the index of the correct answer.
     * 
     * @return Index (0-3) of the correct answer
     */
    public int getCorrectIndex() {
        return correctIndex;
    }
    
    /**
     * Gets the correct answer text.
     * 
     * @return The correct answer string
     */
    public String getCorrectAnswer() {
        return choices.get(correctIndex);
    }
    
    /**
     * Checks if the given choice index is correct.
     * 
     * @param choiceIndex The index to check (0-3)
     * @return true if the choice is correct, false otherwise
     */
    public boolean isCorrect(int choiceIndex) {
        return choiceIndex == correctIndex;
    }
    
    @Override
    public String toString() {
        return "CaptchaQuestion{" +
                "prompt='" + prompt + '\'' +
                ", choices=" + choices +
                ", correctIndex=" + correctIndex +
                '}';
    }
}