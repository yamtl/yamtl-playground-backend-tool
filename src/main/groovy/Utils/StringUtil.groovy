package Utils

class StringUtil {
	def static String removeEscapeChars(String text) {
		text.replaceAll(/\\"/, '"')
				.replaceAll(/\\t/,'\t')
				.replaceAll(/\\r\\n|\\n/, System.lineSeparator()) // Modified this line
				.replaceAll(/^\"|\"$/, '')
	}
}
