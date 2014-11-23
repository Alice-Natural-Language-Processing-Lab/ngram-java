package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;
import java.util.HashMap;

public class TrigramPredictor implements WordPredictor {

    private ITrigramStorage store;

    public TrigramPredictor(ITrigramStorage store) {
        this.store = store;
    }

    @Override
    public void learn(String[] words) throws SQLException {
        if (words.length < 3) return;
        for (int i = 2, l = words.length; i < l; i++) {
            store.add(words[i-2], words[i-1], words[i]);
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) throws SQLException {
        if (words.length < 2) return null;
        String first = words[words.length - 2], second = words[words.length - 1];
        return store.get(first, second, partial);
    }
}
