package indexer;

public class WordTokenizer {

	private String content;
	private int length;
	private int index;
	private String nextToken;

	public WordTokenizer(String content) {
		this.content = content;
		this.length = content.length();
		this.index = 0;
		this.nextToken = "";
	}

	public boolean hasMoreTokens() {
		nextToken = extractNextToken();
		return nextToken.length() != 0;
	}
	
	public String nextToken() {
		return nextToken;
	}

	private String extractNextToken() {
		while (index < length && !Character.isLetterOrDigit(content.charAt(index))) {
			index++;
		}
		if (index >= length) {
			return "";
		}
		int startIndex = index++;
		while (index < length && Character.isLetterOrDigit(content.charAt(index))) {
			index++;
		}
		return content.substring(startIndex, index);
	}

	public static void main(String[] args) {
		WordTokenizer tokenizer = new WordTokenizer("你好、、yeah ");
		while (tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
	}

}
