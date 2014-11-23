package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.IDictionaryStorage;
import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.sql.SQLException;
import java.util.HashMap;

public class DictionaryPredictor implements WordPredictor {

    public static class DictionaryWords extends WordList {

        private Prediction prediction = null; // memo

        public Prediction predict() {
            if (prediction == null) {
                prediction = super.predict(null);
            }
            return prediction;
        }

    } // DictionaryWords

    private IDictionaryStorage store;

    public DictionaryPredictor(IDictionaryStorage store) {
        this.store = store;
    }

    @Override
    public void learn(String[] words) throws SQLException {
        for (String word : words) {
            store.add(word);
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) throws SQLException {
        if (partial == null) return null;
        return store.get(partial);
    }
}
