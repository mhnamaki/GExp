package quickTests;

public class LeftShift {

	public static void main(String[] args) {

		Long n = 1l;

		for (int i = 0; i < 64; i++) {
			n = n << 1L;
			System.out.println("i: " + i + ", n:" + n + ", 2n:" + ((long) (2*n)));
		}
		
		
	}

}
