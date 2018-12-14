from keras.models import Model
from keras.layers import Input, Dense
import os
os.environ["TF_CPP_MIN_LOG_LEVEL"]="3"
from keras.preprocessing.text import Tokenizer
'''user input, model
model call 
output response 
output to java or to a text file'''
def clean_data(utt):
    cleaned_data = list()
    clean_data.append(utt)
    cleaned_data.append(clean_pair)
    return array(cleaned_data)

def load_sentences(filename):
    return load(open(filename, "rb"))

def create_tokenizer(lines):
    tokenizer = Tokenizer()
    tokenizer.fit_on_texts(lines)
    return tokenizer

#we will need to perform this reverse-lookup for every word in a predicted sequence
#this method returns the prediction in words (not integers)
def get_prediction(model, tokenizer, source, input_string):

    prediction = model.predict(source, verbose=1)
    integers = [argmax(vector) for vector in prediction]
    target = list()
    for i in integers:
        word = get_word(i, tokenizer)
        if word is None:
            break
        target.append(word)
    return " ".join(target)


def main():
	model = open("model.test3")
	raw_dataset = load_sentences("utt-resp.pkl")
	dataset = raw_dataset[:n_sentences, :]
	utterance_tokenizer = create_tokenizer(dataset[:, 0])
	get_prediction(model, )
	test_utt = encode_sequences(utterance_tokenizer, utterance_length, train[:, 1])

if __name__ == "__main__": main()