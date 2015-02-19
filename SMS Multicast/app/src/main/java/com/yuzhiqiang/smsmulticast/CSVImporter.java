package com.yuzhiqiang.smsmulticast;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

public class CSVImporter {

	public static ArrayList<Message> fromFile(String filePath) throws Exception {
		final FileReader fileReader = new FileReader(filePath);
		return importCSV(fileReader);
	}

	public static ArrayList<Message> fromString(String CSVString) throws Exception {
		final StringReader stringReader = new StringReader(CSVString);
		return importCSV(stringReader);
	}

	private static ArrayList<Message> importCSV(Reader reader) throws Exception {
		final CSVReader csvReader = new CSVReader(reader);
		ArrayList<Message> list = new ArrayList<>();
		while (true) {
			String[] line = csvReader.readNext();
			if (line == null)
				break;
			if (line.length != 2)
				throw new Exception();
			final String phoneNumber = line[0];
			final String message = line[1];
			list.add(new Message(phoneNumber, message));
		}
		return list;
	}

}
