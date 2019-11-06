package com.sunflow.examples;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.sunflow.util.Log;

public class Serialization {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new Serialization();
		File file = new File("students.txt");
		ArrayList<Student> students = new ArrayList<Student>();
		students.add(new Student("Tom", 3.921));
		students.add(new Student("Dave", 4.0));
		students.add(new Student("Bill", 2.0));

		// serialize
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		for (Student s : students) {
			oos.writeObject(s);
		}
		oos.close();
		fos.close();

		ArrayList<Student> students2 = new ArrayList<Student>();

		// deserialize
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			while (true) {
				Object o = ois.readObject();
				Log.info(o);
				Log.info(o instanceof Student);
				Student s = (Student) o;
				students2.add(s);
			}
		} catch (EOFException e) {}
		ois.close();
		fis.close();

		for (Student s : students2) {
			Log.info(s);
		}
	}

	private static class Student implements Serializable {

		private String name;
		private double val;

		public Student(String name, double val) {
			this.name = name;
			this.val = val;
		}

		@Override
		public String toString() {
			return String.format("%s\t%f", name, val);
		}
	}
}
