package com.arphen.dontblink.app

import java.util.*


fun tokenize(words: Array<String>): LinkedList<String> {
    var cursor = 0
    var tokens = LinkedList<String>()
    var candidate = "";
    while (cursor < words.size) {
        var bigram = "";
        var trigram = "";
        candidate = words[cursor]
        if (cursor + 1 < words.size)
            bigram = """$candidate ${words[cursor + 1]}"""

        if (cursor + 2 < words.size)
            trigram = """$candidate ${words[cursor + 1]} ${words[cursor + 2]}"""

//        if (candidate.any { "!?-.,".contains(it) }) {
//            tokens.add(words[cursor]);
//            cursor++
//            continue
//        }
//        if (bigram.any { "!?-.,".contains(it) }) {
//            tokens.add(bigram)
//            cursor += 2;
//            continue
//        }
        if (Symbols.trigrams_1.contains(trigram.toLowerCase()) || Symbols.trigrams_2.contains(trigram.toLowerCase())) {
            tokens.add(trigram)
            cursor += 3;
            continue
        }
        if (Symbols.bigrams_1.contains(bigram.toLowerCase()) || Symbols.bigrams_2.contains(bigram.toLowerCase())) {
            tokens.add(bigram)
            cursor += 2;
            continue
        }
        tokens.add(words[cursor]);
        cursor++
    }
    return tokens;
}
