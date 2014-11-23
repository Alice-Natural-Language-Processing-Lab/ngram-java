package org.rickosborne.bigram;

import org.rickosborne.bigram.predictor.BigramPredictor;
import org.rickosborne.bigram.predictor.DictionaryPredictor;
import org.rickosborne.bigram.predictor.TrigramPredictor;
import org.rickosborne.bigram.predictor.WordPredictor;
import org.rickosborne.bigram.storage.*;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

public class BigramModel2 {

    private String[] names = { "dictionary", "bigram", "trigram" };
    private double[] weights;
    private WordPredictor[] predictors;
    private OutputStreamWriter logger = null;

    public BigramModel2(Config config) throws SQLException, ClassNotFoundException {
        predictors = new WordPredictor[3];
        IDictionaryStorage dictionaryStorage;
        IBigramStorage bigramStorage;
        ITrigramStorage trigramStorage;
        switch (config.get("storageType", "sqlite")) {
            case "memory":
                dictionaryStorage = new MemoryDictionaryStorage();
                bigramStorage = new MemoryBigramStorage();
                trigramStorage = new MemoryTrigramStorage();
                break;
            default:
                dictionaryStorage = new SqliteDictionaryStorage(config.get("dictionarySqliteFile", "words-dict.sqlite"));
                bigramStorage = new SqliteBigramStorage(config.get("bigramSqliteFile", "words-bi.sqlite"));
                trigramStorage = new SqliteTrigramStorage(config.get("trigramSqliteFile", "words-tri.sqlite"));
        }
        predictors[0] = new DictionaryPredictor(dictionaryStorage);
        predictors[1] = new BigramPredictor(bigramStorage);
        predictors[2] = new TrigramPredictor(trigramStorage);
        weights = new double[3];
        weights[0] = config.get("dictionaryWeight", 200);
        weights[1] = config.get("bigramWeight", 220);
        weights[2] = config.get("trigramWeight", 240);
    }

    private class WeightedWordList extends WordList {
        public Prediction predict() { return this.predict(null); }
    }

    private void log(String message) throws IOException {
        this.logger.write(message);
    }

    public void setLogger(OutputStreamWriter logger) throws IOException {
        this.logger = logger;
        for (String name : names) {
            log(name + " Guess\t" + name + " Correct?\t" + name + " Seen\t" + name + " Size\t" + name + " Vote\t");
        }
    }

    public void learn(String[] words) throws SQLException {
        for (WordPredictor predictor : predictors) {
            predictor.learn(words);
        }
    }

    public String predict(String[] words, String partial, String answer) throws IOException, SQLException {
        WeightedWordList guesses = new WeightedWordList();
        for (int i = 0, predictorCount = predictors.length; i < predictorCount; i++) {
            Prediction guess = predictors[i].predict(words, partial);
            if ((guess != null) && (guess.getWord() != null)) {
                int seen = guess.getSeen();
                int size = guess.getSize();
                int vote = (int) Math.round(seen * this.weights[i] / size);
                String word = guess.getWord();
                boolean correct = word.equals(answer);
                log(String.format(
                    "%s\t%s\t%d\t%d\t%d\t",
                    word,
                    correct ? "Y" : "N",
                    seen,
                    size,
                    vote
                ));
                guesses.learn(word, vote);
            }
            else {
                log("\t\t\t\t\t");
            }
        }
        Prediction prediction = guesses.predict();
        if (prediction == null) return null;
        return prediction.getWord();
    }

}
