public class Caesar {

    private final static int START_NUM = 65;
    private final static int SIZE = 26;

    public static void main(String[] args) {
        Caesar caesar = new Caesar();

        String key = caesar.generateKey(3);
        String message = "ATTACK";
        String cipher = caesar.encrypt(key, message);

        System.out.println(cipher);
    }

    public String encrypt(String key, String message) {
        String cipher = "";
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (IsContain(message,c)) {
                int idx = c - START_NUM;
                cipher += key.charAt(idx);
            } else {
                cipher += c;
            }

        }
        return cipher;
    }

    private static boolean IsContain(String message, char c) {
        for (int i = 0; i < message.length(); i++) {
            if(message.charAt(i) == c){
                return true;
            }
        }
        return false;

    }

    public String generateKey(int shift) {
        String key = "";

        for (int i = 0; i < SIZE; i++) {
            int w = (START_NUM + shift + i) % (START_NUM + SIZE);
            key = key + (char) w;
        }
        // A..Z // A=65

        return key;
    }
}
