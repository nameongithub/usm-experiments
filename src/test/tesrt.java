package test;

import java.util.ArrayList;

public class tesrt {
	public static void main(String[] args) {
		Ob o1 = new Ob(1);
		Ob o2 = new Ob(2);
		ArrayList<Ob> ObList = new ArrayList<Ob>();
		ObList.add(o1);
		ObList.add(o2);
		System.out.println(ObList.add(o1));
		System.out.println("pause");
	}

}
