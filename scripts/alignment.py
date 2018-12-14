import os
from os.path import isfile, join

#I think we should keep three tabs worth of info from each line:
# (1) the type of utterance
# (2) A's utterance
# (3) B's response utterance
def text_align(utterance_file_name, dataset_file_name):
	last_utt = []
	last_utt_person = ""
	utt1 = ""
	utt2 = ""
	text = open(utterance_file_name, "r")
	dataset = open(dataset_file_name, "a")
	for line in text:
		print(utterance_file_name)
		str_array = line.split("\t")
		str_array_person = str_array[1].split(".")[0]
		str_array_utt = str_array[1].split(".")[1]
		#if we are currently switching to A or B's response
		if last_utt_person == "A" and str_array_person == "B" and last_utt != []:
			#create tab separated strings including the info
			utt1 = last_utt[0] + "\t" + last_utt[2]
			utt1 = utt1.strip("\n")
			utt2 = "\t" + str_array[0] + "\t" + str_array[2] + "\n"
			utt = utt1 + utt2
			#write these strings to the output file
			dataset.write(utt)
		last_utt = str_array
		last_utt_person = str_array_person
		last_utt_utt = str_array_utt
	text.close()
	dataset.close()

def main():
	file_path_array = []

	#create path to switcboard folder
	cwd = os.getcwd()
	dir_path = cwd + "/../resources/switchboard_scrubbed3/"
	#create output file in resources folder
	dataset_path = cwd + "/../resources/dataset.txt"
	dataset = open(dataset_path,"w+")

	#iterate through all files in all subfolders, print them all
	#specify files to skip
	doc = dir_path + "doc"
	readme = dir_path + "README"
	small_test = dir_path + "dataset_small_test_file.txt"
	ignore_list = [doc, readme, small_test]
	for subdir, dirs, files in os.walk(dir_path):
		for file in files:
			utt_filename = os.path.join(subdir, file)
			if subdir in ignore_list:
				continue
			if utt_filename in ignore_list:
				continue
			text_align(utt_filename, dataset_path)
		



if __name__ == "__main__": main()

