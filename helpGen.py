#Imports
import numpy as np
from numpy import array
from numpy.random import rand
from numpy.random import shuffle
from pickle import load
from pickle import dump
import re
import os, sys, glob

#Don't run these imports on your local machine!
import tensorflow as tf
#Keras imports
from keras.layers import LSTM, Dense, Activation, Input
from keras import optimizers
from keras.models import Sequential
from keras.layers.embeddings import Embedding
from keras.preprocessing import sequence
from keras.preprocessing.text import Tokenizer

def load_sentences(filename):
    return load(open(filename, "rb"))

######################
# Tokenizer methods
######################
#create and fit a tokenizer on the given lines
def create_tokenizer(lines):
    tokenizer = Tokenizer()
    tokenizer.fit_on_texts(lines)
    return tokenizer

#get the max length of all phrases
def max_length(lines):
    return max(len(line.split()) for line in lines)



######################
# Evaluation methods
######################
#reverse-lookup a word in the tokenizer 
def get_word(integer, tokenizer):
	for word, index in tokenizer.word_index.items():
		if index == integer:
			return word
	return None
#we will need to perform this reverse-lookup for every word in a predicted sequence
#this method returns the prediction in words (not integers)
def get_prediction(model, tokenizer, source):
	prediction = model.predict(source, verbose=0)[0]
	integers = [argmax(vector) for vector in prediction]
	target = list()
	for i in integers:
		word = get_word(i, tokenizer)
		if word is None:
			break
		target.append(word)
	return " ".join(target)
#we need to repeat the prediction for every utterance in the test dataset
#we then compare our prediction to the actual response
#I'm using a BLEU score to compare these quantitatively, but if we get a low BLEU score I wouldn't be surprised.
def evaluate_model(model, tokenizer, sources, raw_dataset):
	actual, predicted = list(), list()
	for i, source in enumerate(sources):
		source = source.reshape((1, source.shape[0]))
		translation = get_prediction(model, utterance_tokenizer, source)
		raw_target, raw_source = raw_dataset[i]
		if i < 10:
			print('src=[%s], target=[%s], predicted=[%s]' % (raw_src, raw_target, translation))
		actual.append(raw_target.split())
		predicted.append(translation.split())
		# calculate BLEU score

	print('BLEU-1: %f' % corpus_bleu(actual, predicted, weights=(1.0, 0, 0, 0)))
	print('BLEU-2: %f' % corpus_bleu(actual, predicted, weights=(0.5, 0.5, 0, 0)))
	print('BLEU-3: %f' % corpus_bleu(actual, predicted, weights=(0.3, 0.3, 0.3, 0)))
	print('BLEU-4: %f' % corpus_bleu(actual, predicted, weights=(0.25, 0.25, 0.25, 0.25)))



######################
# Evaluate
######################
def main():
	#reload the datasets (just in case)
	dataset = load_sentences("utt-resp-both.pkl")
	train = load_sentences("utt-resp-train.pkl")
	test = load_sentences("utt-resp-train.pkl")
	#create tokenizers
	utterance_tokenizer = create_tokenizer(dataset[:, 0])
	response_tokenizer = create_tokenizer(dataset[: 1])
	#define vocabulary sizes
	utterance_vocab_size = len(utterance_tokenizer.word_index) + 1
	response_vocab_size = len(response_tokenizer.word_index) + 1
	#define max_lengths
	utterance_length = max_length(dataset[:, 0])
	response_length = max_length(dataset[:, 1])
	#print some statistics
	print("Utterance vocabulary size: %d" % utterance_vocab_size)
	print("Utterance max length: %d" % utterance_length)
	print("Response vocabulary size: %d" % response_vocab_size)
	print("Utterance max length: %d" % response_length)

	#datasets
	train_utt = encode_sequences(utterance_tokenizer, utterance_length, train[:, 1])
	test_utt = encode_sequences(utterance_tokenizer, utterance_length, train[:, 1])

	model = load_model("model.test1")
	#evalute on training data (this should be pretty good)
	evaluate_model(model, utterance_tokenizer, train_utt, train)
	#evaluate on test data
	evalute_model(model, utterance_tokenizer, test_utt, test)


if __name__ == "__main__": main()