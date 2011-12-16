package ke.go.moh.oec.pisinterfaces.beans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Natacha {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int[][] a = new int[3][5];
		int[][] ab = new int[3][6];
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String x = br.readLine();
		if (a.length == ab.length) {
			System.out.println("x" +fib( Integer.parseInt(x)));
		}
		System.out.println(a.length);

	}

	private static int fib(int n) {
		int y;
		if (n == 1 || n == 0)
			return n;
		y = fib(n - 1) + fib(n - 2);
		return y;
	}

}
