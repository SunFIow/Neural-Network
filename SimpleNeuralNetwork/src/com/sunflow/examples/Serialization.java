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
		students.add(new Student("Tom", 3.921, new Data(1, 2)));
		students.add(new Student("Dave", 4.0, new Data(3, 4)));
		students.add(new Student("Bill", 2.0, new Data(5, 6)));

		// serialize
//		FileOutputStream fos = new FileOutputStream(file);
//		ObjectOutputStream oos = new ObjectOutputStream(fos);
//
//		for (Student s : students) {
//			oos.writeObject(s);
//		}
//		oos.close();
//		fos.close();

		serialize(file, students);

		ArrayList<Student> students2 = new ArrayList<Student>();

		// deserialize
//		FileInputStream fis = new FileInputStream(file);
//		ObjectInputStream ois = new ObjectInputStream(fis);
//
//		try {
//			while (true) {
//				Object o = ois.readObject();
//				Log.info(o);
//				Log.info(o instanceof Student);
//				Student s = (Student) o;
//				students2.add(s);
//			}
//		} catch (EOFException e) {}
//		ois.close();
//		fis.close();

		students2 = (ArrayList<Student>) deserialize(file);

		Log.err("haha");

		for (Student s : students2) {
			Log.err(s);
		}
		Log.err("haha1");
	}

	private static void serialize(File file, Serializable obj) {
		// serialize
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(obj);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Serializable deserialize(File file) {
		Serializable obj = null;
		// deserialize
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);

			try {
				while (true) {
					Object o = ois.readObject();
//					Log.info(o);
					obj = (Serializable) o;
				}
			} catch (EOFException e) {} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}

	private static class Student implements Serializable {

		private String name;
		private double val;
		private Data data;

		public Student(String name, double val, Data data) {
			this.name = name;
			this.val = val;
			this.data = data;
		}

		@Override
		public String toString() {
			return String.format("%s\t%f", name, val) + "\tData{" + data + "}";
		}
	}

	private static class Data implements Serializable {
		private double d1;
		private int d2;

		public Data(int d1, int d2) {
			this.d1 = d1;
			this.d2 = d2;
		}

		@Override
		public String toString() {
			return String.format("%s %s", d1, d2);
		}
	}
}
