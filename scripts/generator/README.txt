This directory contains 2 files that create a neural network trained on movie text
that learns English semantics and utterance types.

Preprocessor
This file organizes and prepares the training and test datasets from the given corpus.
The text file used is clean_dataset.txt located in resources/ from the home directory.

The preprocessor splits the data into train and test sets by taking every nth item from
the main dataset to be used as the test set while removing the test set items from the training set.

These files are saved as .pkl files in resources/ from the home directory. 

Currently, n = 10 but should likely be increased to have more training samples.


Processor
This file handles the creation and execution of the neural net using Keras.
To start it tokenizes and encodes (where it breaks on > 4000 item datasets)
Then it creates the model, compiles it, and runs the training.
After training, it evaluates the model using the test data.



CURRENT ISSUE
At the moment, jupyter notebook cannot handle the sheer size of the dataset we want to train on
and crashes during the encoding process of the data.

A possible fix is to encode the data and train the model in chunks to manage memory.
This would require looping and the overall reorganizing of some sections of code.

Unfortunately, we were not able to train the model with more than 3000 items,
so we are not sure how effective our current layering is.
Feel free to experiment. However it should remain an encoder-decorder LSTM model.
